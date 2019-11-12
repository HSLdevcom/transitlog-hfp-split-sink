package fi.hsl.transitlog.hfp.persisthfpdata;

import fi.hsl.transitlog.hfp.domain.Event;
import fi.hsl.transitlog.hfp.domain.repositories.EventRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.MessageId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DumpService {

    @Autowired
    private EventRepository eventRepository;

    @Transactional
    public List<MessageId> dump(Map<MessageId, Event> eventQueue) {
        log.debug("Saving results");
        Map<MessageId, Event> eventQueueCopy;
        synchronized (eventQueue) {
            eventQueueCopy = eventQueue.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            eventQueue.clear();
        }
        log.debug("To write vehiclepositions count: {}", eventQueueCopy.size());
        List<Event> events = new ArrayList<>(eventQueueCopy.values());
        eventRepository.saveAll(events);
        return new ArrayList<>(eventQueueCopy.keySet());
    }

}
