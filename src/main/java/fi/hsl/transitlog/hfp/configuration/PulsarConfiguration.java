package fi.hsl.transitlog.hfp.configuration;

import com.typesafe.config.Config;
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
@Profile(value = {"dev", "default"})
public class PulsarConfiguration {
    @Bean
    @Retryable(value = {Exception.class}, backoff = @Backoff(delay = 5000))
    public PulsarApplication pulsarApplication() throws Exception {
        log.info("Launching Transitdata-HFP-Sink.");
        Config config = ConfigParser.createConfig();
        log.info("Configuration read, launching the main loop");
        PulsarApplication pulsarApplication = PulsarApplication.newInstance(config);
        return pulsarApplication;
    }
}
