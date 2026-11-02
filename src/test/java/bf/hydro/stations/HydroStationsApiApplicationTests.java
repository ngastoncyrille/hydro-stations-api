package bf.hydro.stations;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/** Parcours complet de l'API : réception de mesures, dédoublonnage, série, état d'alerte. */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class HydroStationsApiApplicationTests {

    /** « Maintenant » est figé pour rendre le test de station silencieuse reproductible. */
    @TestConfiguration
    static class HorlogeFigee {
        @Bean
        @Primary
        Clock horlogeTest() {
            return Clock.fixed(Instant.parse("2025-08-06T12:00:00Z"), ZoneOffset.UTC);
        }
    }

    @Autowired
    WebApplicationContext contexte;

    MockMvc mvc;

    @BeforeEach
    void preparer() {
        mvc = MockMvcBuilders.webAppContextSetup(contexte).build();
    }

    private static final String LOT = """
            [
              {"horodatage": "2025-08-06T09:00:00Z", "parametre": "hauteur", "valeur": 250.0, "unite": "cm", "qualite": "G"},
              {"horodatage": "2025-08-06T10:00:00Z", "parametre": "hauteur", "valeur": 320.0, "unite": "cm", "qualite": "G"},
              {"horodatage": "2025-08-06T10:00:00Z", "parametre": "Batt", "valeur": 11.2, "unite": "V", "qualite": "G"}
            ]
            """;

    @Test
    void laStationDeDemonstrationEstChargee() throws Exception {
        mvc.perform(get("/api/stations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code").value("LOUMBILA"));
    }

    @Test
    void receptionDunLotPuisRenvoiSansDoublon() throws Exception {
        mvc.perform(post("/api/stations/LOUMBILA/mesures").contentType(MediaType.APPLICATION_JSON).content(LOT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enregistrees").value(3))
                .andExpect(jsonPath("$.doublonsIgnores").value(0));

        mvc.perform(post("/api/stations/loumbila/mesures").contentType(MediaType.APPLICATION_JSON).content(LOT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enregistrees").value(0))
                .andExpect(jsonPath("$.doublonsIgnores").value(3));
    }

    @Test
    void etatAvecVigilanceEtBatterieFaible() throws Exception {
        mvc.perform(post("/api/stations/LOUMBILA/mesures").contentType(MediaType.APPLICATION_JSON).content(LOT))
                .andExpect(status().isOk());

        mvc.perform(get("/api/stations/LOUMBILA/etat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.derniereHauteurCm").value(320.0))
                .andExpect(jsonPath("$.niveau").value("VIGILANCE"))
                .andExpect(jsonPath("$.batterieFaible").value(true))
                .andExpect(jsonPath("$.silencieuse").value(false));
    }

    @Test
    void serieFiltreeParPeriode() throws Exception {
        mvc.perform(post("/api/stations/LOUMBILA/mesures").contentType(MediaType.APPLICATION_JSON).content(LOT))
                .andExpect(status().isOk());

        mvc.perform(get("/api/stations/LOUMBILA/mesures")
                        .param("debut", "2025-08-06T09:30:00Z").param("fin", "2025-08-06T12:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].valeur").value(320.0));
    }

    @Test
    void erreursExplicites() throws Exception {
        mvc.perform(get("/api/stations/INCONNUE/etat")).andExpect(status().isNotFound());

        mvc.perform(post("/api/stations").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\": \"LOUMBILA\", \"nom\": \"Doublon\"}"))
                .andExpect(status().isConflict());

        mvc.perform(post("/api/stations/LOUMBILA/mesures").contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"horodatage\": \"2025-08-06T09:00:00Z\", \"parametre\": \"hauteur\"}]"))
                .andExpect(status().isBadRequest());
    }
}
