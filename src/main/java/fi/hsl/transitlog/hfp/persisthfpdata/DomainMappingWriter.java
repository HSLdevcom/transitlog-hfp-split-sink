package fi.hsl.transitlog.hfp.persisthfpdata;

import fi.hsl.common.hfp.proto.Hfp;
import fi.hsl.common.pulsar.PulsarApplication;
import fi.hsl.transitlog.hfp.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.MessageId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
@Component
public class DomainMappingWriter {
    final Map<MessageId, Event> eventQueue;
    private final EntityManager entityManager;
    ScheduledExecutorService scheduler;
    private PulsarApplication pulsarApplication;
    private Consumer<byte[]> consumer;


    @Autowired
    DomainMappingWriter(PulsarApplication pulsarApplication, EntityManager entityManager) {
        this.entityManager = entityManager;
        eventQueue = new HashMap<>();
        this.pulsarApplication = pulsarApplication;
        this.consumer = pulsarApplication.getContext().getConsumer();
    }

    void process(MessageId msgId, Hfp.Data data) {
        switch (data.getTopic().getEventType()) {
            case VP:
                switch (data.getTopic().getJourneyType()) {
                    case journey:
                        eventQueue.put(msgId, new VehiclePosition(data.getTopic(), data.getPayload()));
                        break;
                    case deadrun:
                        eventQueue.put(msgId, new UnsignedEvent(data.getTopic(), data.getPayload()));
                        break;
                    default:
                        log.warn("Received unknown journey type {}", data.getTopic().getJourneyType());
                }
                break;
            case DUE:
            case ARR:
            case ARS:
            case PDE:
            case DEP:
            case PAS:
            case WAIT:
                eventQueue.put(msgId, new StopEvent(data.getTopic(), data.getPayload()));
                break;
            case TLR:
            case TLA:
                eventQueue.put(msgId, new LightPriorityEvent(data.getTopic(), data.getPayload()));
                break;
            case DOO:
            case DOC:
            case DA:
            case DOUT:
            case BA:
            case BOUT:
            case VJA:
            case VJOUT:
                eventQueue.put(msgId, new OtherEvent(data.getTopic(), data.getPayload()));
                break;
            default:
                log.warn("Received HFP message with unknown event type: {}", data.getTopic().getEventType());
        }
    }

    @Scheduled(fixedRateString = "${application.dumpInterval}")
    public void attemptDump() {
        try {
            dump();
        } catch (Exception e) {
            log.error("Failed to check results, closing application", e);
            close(true);
        }
    }

    void close(boolean closePulsar) {
        log.warn("Closing MessageProcessor resources");
        scheduler.shutdown();
        log.info("Scheduler shutdown finished");
        if (closePulsar && pulsarApplication != null) {
            log.info("Closing also Pulsar application");
            pulsarApplication.close();
        }
    }

    private void dump() throws Exception {
        log.debug("Saving results");
        Map<MessageId, Event> eventQueueCopy;
        synchronized (eventQueue) {
            eventQueueCopy = new HashMap<>(eventQueue);
            eventQueue.clear();
        }
        log.error("To write vehiclepositions count: {}", eventQueueCopy.size());
        List<Event> events = new ArrayList<>(eventQueueCopy.values());
        events.parallelStream()
                .forEach(entityManager::persist);
        List<MessageId> messageIds = new ArrayList<>(eventQueueCopy.keySet());
        ackMessages(messageIds);
    }

    private void ackMessages(List<MessageId> messageIds) {
        for (MessageId msgId : messageIds) {
            ack(msgId);
        }
    }

    private void ack(MessageId received) {
        consumer.acknowledgeAsync(received)
                .exceptionally(throwable -> {
                    log.error("Failed to ack Pulsar message", throwable);
                    return null;
                })
                .thenRun(() -> {
                });
    }
}
