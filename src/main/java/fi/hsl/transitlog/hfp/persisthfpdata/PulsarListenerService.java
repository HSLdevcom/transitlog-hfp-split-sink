package fi.hsl.transitlog.hfp.persisthfpdata;

import fi.hsl.common.pulsar.PulsarApplication;
import fi.hsl.transitlog.hfp.MessageProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@Slf4j
public class PulsarListenerService {
    @Autowired
    private DomainMappingWriter domainMappingWriter;
    @Autowired
    private PulsarApplication pulsarApplication;

    @PostConstruct
    public void init() {
        try {
            pulsarApplication.launchWithHandler(new MessageProcessor(domainMappingWriter));
        } catch (Exception e) {
            log.error("Error launching pulsar application", e);
            if (domainMappingWriter != null) {
                domainMappingWriter.close(false);
            }
        }
    }

}
