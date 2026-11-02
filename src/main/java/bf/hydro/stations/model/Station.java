package bf.hydro.stations.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Station hydrométrique télétransmise (limnimètre, enregistreur de hauteur d'eau).
 * Les seuils servent à qualifier le niveau d'alerte à partir de la dernière hauteur mesurée.
 */
@Entity
public class Station {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Code court et unique de la station, utilisé dans les URL (ex. LOUMBILA). */
    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @Column(nullable = false)
    private String nom;

    /** Cours d'eau ou retenue suivi. */
    private String planEau;

    private Double latitude;
    private Double longitude;

    /** Hauteur d'eau (cm) à partir de laquelle la station passe en vigilance. */
    private Double seuilVigilanceCm;

    /** Hauteur d'eau (cm) à partir de laquelle la station passe en alerte. */
    private Double seuilAlerteCm;

    /** Tension (V) sous laquelle la batterie de l'enregistreur est jugée faible. */
    @Column(name = "tension_batterie_min_v")
    private Double tensionBatterieMinV;

    protected Station() {
        // requis par JPA
    }

    public Station(String code, String nom, String planEau, Double latitude, Double longitude,
                   Double seuilVigilanceCm, Double seuilAlerteCm, Double tensionBatterieMinV) {
        this.code = code;
        this.nom = nom;
        this.planEau = planEau;
        this.latitude = latitude;
        this.longitude = longitude;
        this.seuilVigilanceCm = seuilVigilanceCm;
        this.seuilAlerteCm = seuilAlerteCm;
        this.tensionBatterieMinV = tensionBatterieMinV;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getNom() { return nom; }
    public String getPlanEau() { return planEau; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public Double getSeuilVigilanceCm() { return seuilVigilanceCm; }
    public Double getSeuilAlerteCm() { return seuilAlerteCm; }
    public Double getTensionBatterieMinV() { return tensionBatterieMinV; }
}
