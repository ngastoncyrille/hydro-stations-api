package bf.hydro.stations.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import bf.hydro.stations.model.Station;

public interface StationRepository extends JpaRepository<Station, Long> {

    Optional<Station> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);
}
