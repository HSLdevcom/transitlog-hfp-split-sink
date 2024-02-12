package fi.hsl.transitlog.hfp.persisthfpdata;

import com.typesafe.config.Config;
import fi.hsl.common.pulsar.*;
import fi.hsl.common.config.ConfigParser;
import lombok.extern.slf4j.*;


import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.*;

@Service
@Slf4j
@DependsOn(value = {"domainMappingWriter", "pulsarApplication", "messageProcessor"})
public class PulsarListenerService {
    private DomainMappingWriter domainMappingWriter;
    private PulsarApplication pulsarApplication;
    private MessageProcessor messageProcessor;

    @Autowired
    public PulsarListenerService(DomainMappingWriter domainMappingWriter, PulsarApplication pulsarApplication, MessageProcessor messageProcessor) {
        this.domainMappingWriter = domainMappingWriter;
        this.pulsarApplication = pulsarApplication;
        this.messageProcessor = messageProcessor;
    }

    @Scheduled(fixedDelay = 1L)
    public void schedulePulsarApplicationListener() {
        try {
            pulsarApplication.launchWithHandler(messageProcessor);
        } catch (PulsarClientException.AlreadyClosedException ace) {
            log.warn("Pulsar consumer already closed, attempting to recover...", ace);
            recoverFromAlreadyClosedException();
        } catch (Exception e) {
            log.error("Error launching pulsar application", e);
            if (domainMappingWriter != null) {
                domainMappingWriter.close(false);
            }
        }
    }

    private void recoverFromAlreadyClosedException() {
        log.info("Attempting to recover from AlreadyClosedException by reinitializing PulsarApplication...");
        try {
            if (pulsarApplication != null) {
                pulsarApplication.close();
            }
            Config config = ConfigParser.createConfig();
            pulsarApplication = PulsarApplication.newInstance(config);
            log.info("PulsarApplication reinitialized successfully.");
        } catch (Exception e) {
            log.error("Failed to recover from AlreadyClosedException", e);
        }
    }

}
