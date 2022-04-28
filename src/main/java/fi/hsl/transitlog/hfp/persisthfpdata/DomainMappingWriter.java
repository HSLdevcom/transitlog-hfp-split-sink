package fi.hsl.transitlog.hfp.persisthfpdata;

import fi.hsl.common.hfp.proto.*;
import fi.hsl.common.pulsar.*;
import fi.hsl.transitlog.hfp.domain.*;
import fi.hsl.transitlog.hfp.persisthfpdata.archivetodw.*;
import lombok.extern.slf4j.*;
import org.apache.pulsar.client.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.*;

import javax.persistence.*;
import java.io.*;
import java.text.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;

@Slf4j
@Component
public class DomainMappingWriter {
    //TODO: this should be configurable
    private final Duration UNHEALTHY_AFTER_NO_UPLOAD = Duration.ofMinutes(10);

    final Map<MessageId, Event> eventQueue;
    private final DumpService dumpTask;
    private final DWUpload DWUpload;
    private final EventFactory eventFactory;
    private EntityManager entityManager;
    private PulsarApplication pulsarApplication;
    private Consumer<byte[]> consumer;

    private long lastUpload = System.nanoTime();
    //Health check that checks that data was written to the DB in last 10 minutes
    private final BooleanSupplier isHealthy = () -> {
        final Duration lastUploadDelta = Duration.ofNanos(System.nanoTime() - lastUpload);

        final boolean healthy = lastUploadDelta.compareTo(UNHEALTHY_AFTER_NO_UPLOAD) < 0;
        if (!healthy) {
            log.warn("Service unhealthy, data last written to DB {} seconds ago", lastUploadDelta.toSeconds());
        }

        return healthy;
    };

    @Autowired
    DomainMappingWriter(DWUpload dwUpload, PulsarApplication pulsarApplication, EntityManager entityManager, DumpService dumpTask, EventFactory eventFactory) {
        this.DWUpload = dwUpload;
        eventQueue = new ConcurrentHashMap<>();
        this.dumpTask = dumpTask;
        this.entityManager = entityManager;
        this.pulsarApplication = pulsarApplication;
        this.consumer = pulsarApplication.getContext().getConsumer();
        this.eventFactory = eventFactory;

        //Add health check if health checks are enabled (i.e. health server is not null)
        if (pulsarApplication.getContext().getHealthServer() != null) {
            pulsarApplication.getContext().getHealthServer().addCheck(isHealthy);
        }
    }

    void process(MessageId msgId, Hfp.Data data) throws IOException, ParseException {
        Event event = null;
        switch (data.getTopic().getEventType()) {
            case VP:
                switch (data.getTopic().getJourneyType()) {
                    case journey:
                        event = eventFactory.createVehiclePositionEvent(data.getTopic(), data.getPayload());
                        eventQueue.put(msgId, event);
                        break;
                    case deadrun:
                        event = eventFactory.createUnsignedEvent(data.getTopic(), data.getPayload());
                        eventQueue.put(msgId, event);
                        break;
                    default:
                        if (data.getTopic().getJourneyType() != Hfp.Topic.JourneyType.signoff) {
                            log.warn("Received unknown journey type {}", data.getTopic().getJourneyType());
                        }
                }
                break;
            case DUE:
            case ARR:
            case ARS:
            case PDE:
            case DEP:
            case PAS:
            case WAIT:
                event = eventFactory.createStopEvent(data.getTopic(), data.getPayload());
                eventQueue.put(msgId, event);
                break;
            case TLR:
            case TLA:
                event = eventFactory.createLightPriorityEvent(data.getTopic(), data.getPayload());
                eventQueue.put(msgId, event);
                break;
            case DOO:
            case DOC:
            case DA:
            case DOUT:
            case BA:
            case BOUT:
            case VJA:
            case VJOUT:
                event = eventFactory.createOtherEvent(data.getTopic(), data.getPayload());
                eventQueue.put(msgId, event);
                break;
            default:
                log.warn("Received HFP message with unknown event type: {}", data.getTopic().getEventType());
        }
        if (event != null) {
            DWUpload.uploadBlob(event);
        }

        //Acknowledge all messages that were not inserted to the event queue to avoid them filling up Pulsar backlogs
        if (event == null) {
            ack(msgId);
        }
    }

    @Scheduled(fixedRateString = "${application.dumpInterval}")
    @Async
    public void attemptDump() {
        try {
            List<MessageId> dumpedMessagedIds = dumpTask.dump(eventQueue);
            ackMessages(dumpedMessagedIds);

            lastUpload = System.nanoTime();
        } catch (Exception e) {
            log.error("Failed to check results, closing application", e);
            close(true);
        }
    }

    private void ackMessages(List<MessageId> messageIds) {
        for (MessageId msgId : messageIds) {
            ack(msgId);
        }
    }

    void close(boolean closePulsar) {
        log.warn("Closing MessageProcessor resources");
        if (closePulsar && pulsarApplication != null) {
            log.info("Closing Pulsar application");
            pulsarApplication.close();
        }
    }

    public void ack(MessageId received) {
        consumer.acknowledgeAsync(received)
                .exceptionally(throwable -> {
                    log.error("Failed to ack Pulsar message", throwable);
                    return null;
                })
                .thenRun(() -> {
                });
    }
}
