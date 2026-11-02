package bf.hydro.stations.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Valeur mesurée par une station à un instant donné, pour un paramètre donné
 * (hauteur d'eau, tension de batterie...). Une même mesure ne peut être
 * enregistrée deux fois : les téléchargements successifs d'un enregistreur
 * se recouvrent souvent.
 */
@Entity
@Table(name = "mesure",
        uniqueConstraints = @UniqueConstraint(columnNames = {"station_id", "horodatage", "parametre"}),
        indexes = @Index(columnList = "station_id, parametre, horodatage"))
public class Mesure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "station_id")
    private Station station;

    @Column(nullable = false)
    private Instant horodatage;

    /** Paramètre mesuré, en minuscules (ex. hauteur, batterie). */
    @Column(nullable = false, length = 30)
    private String parametre;

    @Column(nullable = false)
    private Double valeur;

    @Column(length = 10)
    private String unite;

    /** Code qualité transmis par l'enregistreur (ex. G pour une valeur jugée bonne). */
    @Column(length = 5)
    private String qualite;

    protected Mesure() {
        // requis par JPA
    }

    public Mesure(Station station, Instant horodatage, String parametre, Double valeur, String unite, String qualite) {
        this.station = station;
        this.horodatage = horodatage;
        this.parametre = parametre;
        this.valeur = valeur;
        this.unite = unite;
        this.qualite = qualite;
    }

    public Long getId() { return id; }
    public Station getStation() { return station; }
    public Instant getHorodatage() { return horodatage; }
    public String getParametre() { return parametre; }
    public Double getValeur() { return valeur; }
    public String getUnite() { return unite; }
    public String getQualite() { return qualite; }
}
