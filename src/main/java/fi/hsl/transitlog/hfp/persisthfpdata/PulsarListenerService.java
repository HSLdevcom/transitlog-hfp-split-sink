package fi.hsl.transitlog.hfp.persisthfpdata;

import fi.hsl.common.pulsar.PulsarApplication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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

    @Scheduled(fixedDelay = 5000)
    public void schedulePulsarApplicationListener() {
        try {
            pulsarApplication.launchWithHandler(messageProcessor);
        } catch (Exception e) {
            log.error("Error launching pulsar application", e);
            if (domainMappingWriter != null) {
                domainMappingWriter.close(false);
            }
        }
    }

}
