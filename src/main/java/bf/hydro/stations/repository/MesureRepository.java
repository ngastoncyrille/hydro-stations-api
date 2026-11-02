package bf.hydro.stations.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import bf.hydro.stations.model.Mesure;
import bf.hydro.stations.model.Station;

public interface MesureRepository extends JpaRepository<Mesure, Long> {

    boolean existsByStationAndHorodatageAndParametre(Station station, Instant horodatage, String parametre);

    List<Mesure> findByStationAndParametreAndHorodatageBetweenOrderByHorodatage(
            Station station, String parametre, Instant debut, Instant fin);

    Optional<Mesure> findFirstByStationAndParametreOrderByHorodatageDesc(Station station, String parametre);

    Optional<Mesure> findFirstByStationOrderByHorodatageDesc(Station station);
}
