package fi.hsl.transitlog.hfp.persisthfpdata;

import config.*;
import fi.hsl.*;
import fi.hsl.transitlog.hfp.domain.*;
import fi.hsl.transitlog.hfp.domain.repositories.*;
import org.junit.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.test.context.*;
import org.springframework.test.context.junit.jupiter.*;

import java.util.*;

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

        List<Event> all = eventRepository.findAll();

        //TODO Assertoi mitä meni kantaan
    }

}