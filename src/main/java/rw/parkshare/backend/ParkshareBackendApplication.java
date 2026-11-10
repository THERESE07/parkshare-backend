package rw.parkshare.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class ParkshareBackendApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ParkshareBackendApplication.class);
        // Email is not essential for the app to run, so a slow or unavailable mail
        // server must not make /actuator/health report DOWN (hosts would restart us).
        app.setDefaultProperties(java.util.Map.of("management.health.mail.enabled", "false"));
        app.run(args);
    }

}
