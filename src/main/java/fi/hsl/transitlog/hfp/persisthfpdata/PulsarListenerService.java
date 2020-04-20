package fi.hsl.transitlog.hfp.persisthfpdata;

import fi.hsl.common.pulsar.*;
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
        } catch (Exception e) {
            log.error("Error launching pulsar application", e);
            if (domainMappingWriter != null) {
                domainMappingWriter.close(false);
            }
        }
    }

}
