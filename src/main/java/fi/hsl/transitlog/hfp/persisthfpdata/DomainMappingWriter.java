package fi.hsl.transitlog.hfp.persisthfpdata;

import fi.hsl.common.hfp.proto.Hfp;
import fi.hsl.common.pulsar.PulsarApplication;
import fi.hsl.transitlog.hfp.domain.*;
import fi.hsl.transitlog.hfp.domain.repositories.EventsRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.MessageId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@DependsOn(value = {"pulsarApplication"})
public class DomainMappingWriter {
    final Map<MessageId, Event> eventQueue;
    ScheduledExecutorService scheduler;
    @Autowired
    private EventsRepository eventsRepository;
    @Autowired
    private PulsarApplication pulsarApplication;
    private Consumer<byte[]> consumer;


    DomainMappingWriter() {
        eventQueue = new HashMap<>();
    }

    @PostConstruct
    private void init() {
        startMappingWriter();
    }

    private void startMappingWriter() {
        final long intervalInMs = pulsarApplication.getContext().getConfig().getDuration("application.dumpInterval", TimeUnit.MILLISECONDS);
        startDumpExecutor(intervalInMs);
    }

    void startDumpExecutor(long intervalInMs) {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        log.info("Starting result-scheduler with dump interval of {} seconds", intervalInMs / 1000);

        scheduler.scheduleAtFixedRate(() -> {
            try {
                dump();
            } catch (Exception e) {
                log.error("Failed to check results, closing application", e);
                close(true);
            }
        }, intervalInMs, intervalInMs, TimeUnit.MILLISECONDS);
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
        // TODO: write messages (events) here
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

    void close(boolean closePulsar) {
        log.warn("Closing MessageProcessor resources");
        scheduler.shutdown();
        log.info("Scheduler shutdown finished");
        if (closePulsar && pulsarApplication != null) {
            log.info("Closing also Pulsar application");
            pulsarApplication.close();
        }
    }

    public void process(MessageId msgId, Hfp.Data data) {
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
}
