package fi.hsl.transitlog.hfp.persisthfpdata;

import config.H2Configuration;
import fi.hsl.Main;
import fi.hsl.transitlog.hfp.configuration.PulsarConfiguration;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PulsarContainer;

@ActiveProfiles(value = "integration-test")
@TestPropertySource(value = "classpath:/application.properties")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {H2Configuration.class, Main.class, PulsarConfiguration.class})
class DumpServiceIT {

    @ClassRule
    public static PulsarContainer pulsarContainer = new PulsarContainer().withExposedPorts(6650);

    @BeforeEach
    void setUp() {
        System.out.println("hello");
    }

    @Test
    @Ignore
    void dump() {
    }
}