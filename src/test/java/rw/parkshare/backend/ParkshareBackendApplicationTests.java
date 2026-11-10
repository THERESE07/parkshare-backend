package rw.parkshare.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(properties = {
        "PARKSHARE_JWT_SECRET=test-secret-that-is-long-enough-for-hs256-signing",
        "parkshare.scheduler.initial-delay-ms=3600000"
})
@Import(TestcontainersConfiguration.class)
class ParkshareBackendApplicationTests {

    @Test
    void contextLoads() {
    }

}
