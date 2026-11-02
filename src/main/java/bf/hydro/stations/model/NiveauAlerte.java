package bf.hydro.stations.model;

/** Niveau d'alerte d'une station, déduit de la dernière hauteur d'eau mesurée. */
public enum NiveauAlerte {
    /** Aucune hauteur récente, ou seuils non renseignés. */
    INCONNU,
    NORMAL,
    VIGILANCE,
    ALERTE
}
