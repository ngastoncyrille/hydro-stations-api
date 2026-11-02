package bf.hydro.stations.web;

import java.time.Instant;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import bf.hydro.stations.service.StationService;
import bf.hydro.stations.web.Dtos.EtatStation;
import bf.hydro.stations.web.Dtos.MesureEntree;
import bf.hydro.stations.web.Dtos.MesureSortie;
import bf.hydro.stations.web.Dtos.ResultatImport;
import bf.hydro.stations.web.Dtos.StationEntree;
import bf.hydro.stations.web.Dtos.StationSortie;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

@RestController
@RequestMapping("/api/stations")
@Validated
public class StationController {

    private final StationService service;

    public StationController(StationService service) {
        this.service = service;
    }

    @GetMapping
    public List<StationSortie> lister() {
        return service.lister().stream().map(StationSortie::de).toList();
    }

    @GetMapping("/{code}")
    public StationSortie detail(@PathVariable String code) {
        return StationSortie.de(service.trouver(code));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StationSortie creer(@Valid @RequestBody StationEntree entree) {
        return StationSortie.de(service.creer(entree));
    }

    /** Réception d'un lot de mesures (téléchargement d'un enregistreur ou flux de télétransmission). */
    @PostMapping("/{code}/mesures")
    public ResultatImport recevoir(@PathVariable String code,
                                   @RequestBody @NotEmpty List<@Valid MesureEntree> lot) {
        return service.importer(code, lot);
    }

    @GetMapping("/{code}/mesures")
    public List<MesureSortie> serie(@PathVariable String code,
                                    @RequestParam(defaultValue = StationService.HAUTEUR) String parametre,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant debut,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fin) {
        return service.serie(code, parametre, debut, fin).stream().map(MesureSortie::de).toList();
    }

    /** Dernière hauteur, niveau d'alerte, état de la batterie et de la transmission. */
    @GetMapping("/{code}/etat")
    public EtatStation etat(@PathVariable String code) {
        return service.etat(code);
    }
}
