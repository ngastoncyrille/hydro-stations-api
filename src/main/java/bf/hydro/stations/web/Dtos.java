package bf.hydro.stations.web;

import java.time.Instant;

import bf.hydro.stations.model.Mesure;
import bf.hydro.stations.model.NiveauAlerte;
import bf.hydro.stations.model.Station;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

/** Objets échangés en JSON par l'API. */
public final class Dtos {

    private Dtos() {
    }

    public record StationEntree(
            @NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{2,40}") String code,
            @NotBlank String nom,
            String planEau,
            @DecimalMin("-90") @DecimalMax("90") Double latitude,
            @DecimalMin("-180") @DecimalMax("180") Double longitude,
            @Positive Double seuilVigilanceCm,
            @Positive Double seuilAlerteCm,
            @Positive Double tensionBatterieMinV) {
    }

    public record StationSortie(String code, String nom, String planEau, Double latitude, Double longitude,
                                Double seuilVigilanceCm, Double seuilAlerteCm, Double tensionBatterieMinV) {
        static StationSortie de(Station s) {
            return new StationSortie(s.getCode(), s.getNom(), s.getPlanEau(), s.getLatitude(), s.getLongitude(),
                    s.getSeuilVigilanceCm(), s.getSeuilAlerteCm(), s.getTensionBatterieMinV());
        }
    }

    public record MesureEntree(
            @NotNull @PastOrPresent Instant horodatage,
            @NotBlank String parametre,
            @NotNull Double valeur,
            String unite,
            String qualite) {
    }

    public record MesureSortie(Instant horodatage, String parametre, Double valeur, String unite, String qualite) {
        static MesureSortie de(Mesure m) {
            return new MesureSortie(m.getHorodatage(), m.getParametre(), m.getValeur(), m.getUnite(), m.getQualite());
        }
    }

    /** Bilan d'un envoi de mesures : nouvelles valeurs enregistrées et doublons ignorés. */
    public record ResultatImport(int recues, int enregistrees, int doublonsIgnores) {
    }

    /** État courant d'une station, prêt à alimenter un tableau de bord ou un bulletin. */
    public record EtatStation(String code, String nom, Double derniereHauteurCm, Instant dateDerniereHauteur,
                              NiveauAlerte niveau, Double derniereTensionV, boolean batterieFaible,
                              Instant derniereTransmission, boolean silencieuse) {
    }
}
