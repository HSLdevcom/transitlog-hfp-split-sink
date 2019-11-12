package fi.hsl.transitlog.hfp.persisthfpdata;

import config.H2Configuration;
import config.PulsarConfiguration;
import fi.hsl.Main;
import fi.hsl.transitlog.hfp.domain.Event;
import fi.hsl.transitlog.hfp.domain.repositories.EventRepository;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

@ActiveProfiles(value = "integration-test")
@TestPropertySource(value = "classpath:/application.properties")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {H2Configuration.class, Main.class, PulsarConfiguration.class})
class DumpServiceIT {

    @Autowired
    private EventRepository eventRepository;

    @Test
    @Ignore
    void dump() {
        //TODO Keksi tapa lähettää brokerille viestiä

        List<Event> all =
                eventRepository.findAll();

        //TODO Assertoi mitä meni kantaan
    }


}