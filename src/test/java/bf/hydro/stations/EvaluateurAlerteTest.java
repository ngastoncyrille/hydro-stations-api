package bf.hydro.stations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import bf.hydro.stations.model.NiveauAlerte;
import bf.hydro.stations.service.EvaluateurAlerte;

class EvaluateurAlerteTest {

    @Test
    void niveauSelonLesSeuils() {
        assertEquals(NiveauAlerte.NORMAL, EvaluateurAlerte.niveau(120.0, 300.0, 400.0));
        assertEquals(NiveauAlerte.VIGILANCE, EvaluateurAlerte.niveau(300.0, 300.0, 400.0));
        assertEquals(NiveauAlerte.ALERTE, EvaluateurAlerte.niveau(410.0, 300.0, 400.0));
    }

    @Test
    void niveauInconnuSansHauteurOuSansSeuil() {
        assertEquals(NiveauAlerte.INCONNU, EvaluateurAlerte.niveau(null, 300.0, 400.0));
        assertEquals(NiveauAlerte.INCONNU, EvaluateurAlerte.niveau(250.0, null, 400.0));
    }

    @Test
    void batterieFaibleSousLeMinimum() {
        assertTrue(EvaluateurAlerte.batterieFaible(11.2, 11.5));
        assertFalse(EvaluateurAlerte.batterieFaible(12.7, 11.5));
        assertFalse(EvaluateurAlerte.batterieFaible(null, 11.5));
    }

    @Test
    void stationSilencieuseAuDelaDuDelai() {
        Instant maintenant = Instant.parse("2025-08-06T12:00:00Z");
        Duration delai = Duration.ofHours(6);
        assertFalse(EvaluateurAlerte.silencieuse(Instant.parse("2025-08-06T08:00:00Z"), maintenant, delai));
        assertTrue(EvaluateurAlerte.silencieuse(Instant.parse("2025-08-06T05:00:00Z"), maintenant, delai));
        assertTrue(EvaluateurAlerte.silencieuse(null, maintenant, delai));
    }
}
