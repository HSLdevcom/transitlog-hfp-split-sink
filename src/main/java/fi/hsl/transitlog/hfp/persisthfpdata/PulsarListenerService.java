package fi.hsl.transitlog.hfp.persisthfpdata;

import com.typesafe.config.Config;
import fi.hsl.common.config.ConfigParser;
import fi.hsl.common.pulsar.PulsarApplication;
import fi.hsl.transitlog.hfp.MessageProcessor;
import fi.hsl.transitlog.hfp.domain.repositories.EventsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@Slf4j
public class PulsarListenerService {
    @Autowired
    private EventsRepository eventsRepository;

    @PostConstruct
    public void init() {
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
        } catch (Exception e) {
            log.error("Exception at main", e);
            if (domainMappingWriter != null) {
                domainMappingWriter.close(false);
            }
        }
    }

}
