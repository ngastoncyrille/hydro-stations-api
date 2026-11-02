package bf.hydro.stations;

import java.time.Clock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class HydroStationsApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(HydroStationsApiApplication.class, args);
    }

    /** Horloge injectée, remplaçable dans les tests pour figer « maintenant ». */
    @Bean
    Clock horloge() {
        return Clock.systemUTC();
    }
}
