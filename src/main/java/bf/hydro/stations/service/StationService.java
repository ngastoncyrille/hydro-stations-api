package bf.hydro.stations.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bf.hydro.stations.model.Mesure;
import bf.hydro.stations.model.Station;
import bf.hydro.stations.repository.MesureRepository;
import bf.hydro.stations.repository.StationRepository;
import bf.hydro.stations.web.Dtos.EtatStation;
import bf.hydro.stations.web.Dtos.MesureEntree;
import bf.hydro.stations.web.Dtos.ResultatImport;
import bf.hydro.stations.web.Dtos.StationEntree;

@Service
public class StationService {

    public static final String HAUTEUR = "hauteur";
    public static final String BATTERIE = "batterie";

    private final StationRepository stations;
    private final MesureRepository mesures;
    private final Clock horloge;
    private final Duration delaiSilence;

    public StationService(StationRepository stations, MesureRepository mesures, Clock horloge,
                          @Value("${hydro.delai-silence:PT6H}") Duration delaiSilence) {
        this.stations = stations;
        this.mesures = mesures;
        this.horloge = horloge;
        this.delaiSilence = delaiSilence;
    }

    @Transactional(readOnly = true)
    public List<Station> lister() {
        return stations.findAll();
    }

    @Transactional(readOnly = true)
    public Station trouver(String code) {
        return stations.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new IntrouvableException("Station inconnue : " + code));
    }

    @Transactional
    public Station creer(StationEntree e) {
        if (stations.existsByCodeIgnoreCase(e.code())) {
            throw new ConflitException("Une station porte déjà le code " + e.code());
        }
        if (e.seuilVigilanceCm() != null && e.seuilAlerteCm() != null && e.seuilAlerteCm() < e.seuilVigilanceCm()) {
            throw new IllegalArgumentException("Le seuil d'alerte doit être supérieur ou égal au seuil de vigilance");
        }
        return stations.save(new Station(e.code().toUpperCase(Locale.ROOT), e.nom(), e.planEau(), e.latitude(),
                e.longitude(), e.seuilVigilanceCm(), e.seuilAlerteCm(), e.tensionBatterieMinV()));
    }

    /**
     * Enregistre un lot de mesures transmis par un enregistreur. Les mesures déjà
     * présentes (même station, même instant, même paramètre) sont ignorées, ce qui
     * permet de renvoyer sans risque un fichier qui recouvre un envoi précédent.
     */
    @Transactional
    public ResultatImport importer(String code, List<MesureEntree> lot) {
        Station station = trouver(code);
        int enregistrees = 0;
        for (MesureEntree m : lot) {
            String parametre = normaliserParametre(m.parametre());
            if (mesures.existsByStationAndHorodatageAndParametre(station, m.horodatage(), parametre)) {
                continue;
            }
            mesures.save(new Mesure(station, m.horodatage(), parametre, m.valeur(), m.unite(), m.qualite()));
            enregistrees++;
        }
        return new ResultatImport(lot.size(), enregistrees, lot.size() - enregistrees);
    }

    @Transactional(readOnly = true)
    public List<Mesure> serie(String code, String parametre, Instant debut, Instant fin) {
        if (debut.isAfter(fin)) {
            throw new IllegalArgumentException("La date de début est postérieure à la date de fin");
        }
        return mesures.findByStationAndParametreAndHorodatageBetweenOrderByHorodatage(
                trouver(code), normaliserParametre(parametre), debut, fin);
    }

    @Transactional(readOnly = true)
    public EtatStation etat(String code) {
        Station s = trouver(code);
        Mesure hauteur = mesures.findFirstByStationAndParametreOrderByHorodatageDesc(s, HAUTEUR).orElse(null);
        Mesure batterie = mesures.findFirstByStationAndParametreOrderByHorodatageDesc(s, BATTERIE).orElse(null);
        Instant derniere = mesures.findFirstByStationOrderByHorodatageDesc(s).map(Mesure::getHorodatage).orElse(null);

        Double h = hauteur == null ? null : hauteur.getValeur();
        Double v = batterie == null ? null : batterie.getValeur();
        return new EtatStation(s.getCode(), s.getNom(), h, hauteur == null ? null : hauteur.getHorodatage(),
                EvaluateurAlerte.niveau(h, s.getSeuilVigilanceCm(), s.getSeuilAlerteCm()),
                v, EvaluateurAlerte.batterieFaible(v, s.getTensionBatterieMinV()),
                derniere, EvaluateurAlerte.silencieuse(derniere, horloge.instant(), delaiSilence));
    }

    /** Harmonise les libellés des enregistreurs (ex. « Batt » devient « batterie »). */
    static String normaliserParametre(String brut) {
        String p = brut.trim().toLowerCase(Locale.ROOT);
        return switch (p) {
            case "batt", "bat", "tension" -> BATTERIE;
            case "h", "niveau", "hauteur_eau" -> HAUTEUR;
            default -> p;
        };
    }

    public static class IntrouvableException extends RuntimeException {
        public IntrouvableException(String message) {
            super(message);
        }
    }

    public static class ConflitException extends RuntimeException {
        public ConflitException(String message) {
            super(message);
        }
    }
}
