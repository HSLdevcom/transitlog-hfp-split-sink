package fi.hsl.transitlog.hfp.persisthfpdata;

import fi.hsl.transitlog.hfp.domain.*;
import fi.hsl.transitlog.hfp.domain.repositories.*;
import lombok.extern.slf4j.*;
import org.apache.pulsar.client.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

import java.util.*;
import java.util.stream.*;

@Service
@Slf4j
public class DumpService {

    @Autowired
    private EventRepository eventRepository;

    @Transactional
    List<MessageId> dump(Map<MessageId, Event> eventQueue) {
        log.debug("Saving results");
        Map<MessageId, Event> eventQueueCopy;
        synchronized (eventQueue) {
            eventQueueCopy = eventQueue.entrySet().stream()
                    //Filter events that don't have timestamp.
                    //These can break partitioning in the database and events without timestamp cannot be shown in transitlog-ui anyway
                    .filter(entry -> entry.getValue().getTst() != null)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            eventQueue.clear();
        }
        log.info("To write event count: {}", eventQueueCopy.size());
        List<Event> events = new ArrayList<>(eventQueueCopy.values());
        eventRepository.saveAll(events);
        return new ArrayList<>(eventQueueCopy.keySet());
    }

}
