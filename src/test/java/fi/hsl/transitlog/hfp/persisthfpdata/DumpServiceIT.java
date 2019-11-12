package fi.hsl.transitlog.hfp.persisthfpdata;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
import config.H2Configuration;
import fi.hsl.Main;
import fi.hsl.common.config.ConfigParser;
import fi.hsl.common.pulsar.PulsarApplication;
import fi.hsl.transitlog.hfp.configuration.PulsarConfiguration;
import fi.hsl.transitlog.hfp.domain.Event;
import fi.hsl.transitlog.hfp.domain.repositories.EventRepository;
import lombok.extern.slf4j.Slf4j;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PulsarContainer;

import java.util.List;

@ActiveProfiles(value = "integration-test")
@TestPropertySource(value = "classpath:/application.properties")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {H2Configuration.class, Main.class, DumpServiceIT.PulsarConfiguration.class})
class DumpServiceIT {

    static final PulsarContainer pulsarContainer;

    static {
        pulsarContainer = new PulsarContainer("2.4.0").withNetwork(Network.SHARED).withExposedPorts(6650, 8080);
        pulsarContainer.start();
    }

    @Autowired
    private EventRepository eventRepository;

    @Test
    void dump() {
        //TODO Keksi tapa lähettää brokerille viestiä

        List<Event> all =
                eventRepository.findAll();

        //TODO Assertoi mitä meni kantaan
    }


    @Configuration
    @Slf4j
    @Retryable
    @Profile(value = {"integration-test"})
    public static class PulsarConfiguration {
        @Bean
        @Retryable(value = {Exception.class}, backoff = @Backoff(delay = 5000))
        public PulsarApplication pulsarApplication() throws Exception {

            log.info("Pulsar info {}", pulsarContainer.getContainerInfo());
            log.info("Launching Transitdata-HFP-Sink.");
            Config config = ConfigParser.createConfig();

            String brokerUrl = pulsarContainer.getPulsarBrokerUrl();
            String brokerPort = brokerUrl.substring(brokerUrl.indexOf("t:") + 2);
            Config testConfig = config.withValue("pulsar.port",
                    ConfigValueFactory.fromAnyRef(brokerPort)).withValue("application.dumpInterval",
                    ConfigValueFactory.fromAnyRef((5000)));

            log.info("Configuration read, launching the main loop");
            PulsarApplication pulsarApplication = PulsarApplication.newInstance(testConfig);
            return pulsarApplication;
        }
    }

}