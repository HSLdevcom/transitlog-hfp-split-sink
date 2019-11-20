package config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
import fi.hsl.common.config.ConfigParser;
import fi.hsl.common.pulsar.PulsarApplication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

@Configuration
@Slf4j
@Retryable
@Profile(value = {"integration-test"})
public class PulsarConfiguration {

    @Bean
    @Retryable(value = {Exception.class}, backoff = @Backoff(delay = 5000))
    public PulsarApplication pulsarApplication() throws Exception {
        log.info("Launching Transitdata-HFP-Sink.");
        Config config = ConfigParser.createConfig();
        Config configForTest = config
                .withValue("pulsar.consumer.topic", ConfigValueFactory.fromAnyRef("persistent://public/default/test-hfp-parsed"))
                .withValue("pulsar.consumer.subscription", ConfigValueFactory.fromAnyRef("test-hfp-split-sink"))
                .withValue("application.dumpInterval", ConfigValueFactory.fromAnyRef("1 seconds"));

        log.info("Configuration read, launching the main loop");
        PulsarApplication pulsarApplication = PulsarApplication.newInstance(configForTest);
        return pulsarApplication;
    }
}