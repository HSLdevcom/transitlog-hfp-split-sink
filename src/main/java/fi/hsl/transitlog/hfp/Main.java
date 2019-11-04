package fi.hsl.transitlog.hfp;

import com.typesafe.config.Config;
import fi.hsl.common.config.ConfigParser;
import fi.hsl.common.config.ConfigUtils;
import fi.hsl.common.pulsar.PulsarApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Optional;
@SpringBootApplication
public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
        log.info("Launching Transitdata-HFP-Sink.");

        Config config = ConfigParser.createConfig();

        log.info("Configuration read, launching the main loop");
        MessageProcessor processor = null;
        DomainMappingWriter domainMappingWriter = null;
        try (PulsarApplication app = PulsarApplication.newInstance(config)) {
            domainMappingWriter = DomainMappingWriter.newInstance(app);
            processor = new MessageProcessor(domainMappingWriter);
            log.info("Starting to process messages");
            app.launchWithHandler(processor);
        }
        catch (Exception e) {
            log.error("Exception at main", e);
            if (domainMappingWriter != null) {
                domainMappingWriter.close(false);
            }
        }
    }
}
