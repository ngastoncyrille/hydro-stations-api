package bf.hydro.stations.service;

import java.time.Duration;
import java.time.Instant;

import bf.hydro.stations.model.NiveauAlerte;

/**
 * Règles de qualification de l'état d'une station. Classe sans dépendance à Spring
 * ni à la base, pour pouvoir être testée isolément.
 */
public final class EvaluateurAlerte {

    private EvaluateurAlerte() {
    }

    /** Niveau d'alerte d'une hauteur d'eau (cm) au regard des seuils de la station. */
    public static NiveauAlerte niveau(Double hauteurCm, Double seuilVigilanceCm, Double seuilAlerteCm) {
        if (hauteurCm == null || seuilVigilanceCm == null || seuilAlerteCm == null) {
            return NiveauAlerte.INCONNU;
        }
        if (hauteurCm >= seuilAlerteCm) {
            return NiveauAlerte.ALERTE;
        }
        if (hauteurCm >= seuilVigilanceCm) {
            return NiveauAlerte.VIGILANCE;
        }
        return NiveauAlerte.NORMAL;
    }

    /** Vrai si la tension mesurée passe sous le minimum de la station. */
    public static boolean batterieFaible(Double tensionV, Double tensionMinV) {
        return tensionV != null && tensionMinV != null && tensionV < tensionMinV;
    }

    /**
     * Vrai si la station n'a rien transmis depuis plus que le délai toléré.
     * Une station sans aucune mesure est considérée comme silencieuse.
     */
    public static boolean silencieuse(Instant derniereMesure, Instant maintenant, Duration delaiTolere) {
        return derniereMesure == null || Duration.between(derniereMesure, maintenant).compareTo(delaiTolere) > 0;
    }
}
