package fi.hsl.transitlog.hfp.persisthfpdata;

import fi.hsl.common.hfp.proto.Hfp;
import fi.hsl.common.pulsar.PulsarApplication;
import fi.hsl.transitlog.hfp.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.MessageId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public
class DomainMappingWriter {

    final HashMap<MessageId, VehiclePosition> vehiclePositionQueue;
    final HashMap<MessageId, StopEvent> stopEventQueue;
    final HashMap<MessageId, LightPriorityEvent> lightPriorityEventQueue;
    final HashMap<MessageId, OtherEvent> otherEventQueue;
    final HashMap<MessageId, UnsignedEvent> unsignedEventQueue;
    private final Consumer<byte[]> consumer;
    private final PulsarApplication application;
    ScheduledExecutorService scheduler;

    private DomainMappingWriter(PulsarApplication app) {
        consumer = app.getContext().getConsumer();
        application = app;
        vehiclePositionQueue = new HashMap<>();
        stopEventQueue = new HashMap<>();
        lightPriorityEventQueue = new HashMap<>();
        otherEventQueue = new HashMap<>();
        unsignedEventQueue = new HashMap<>();
    }

    public static DomainMappingWriter newInstance(PulsarApplication app) {
        DomainMappingWriter domainMappingWriter = new DomainMappingWriter(app);
        final long intervalInMs = app.getContext().getConfig().getDuration("application.dumpInterval", TimeUnit.MILLISECONDS);
        domainMappingWriter.startDumpExecutor(intervalInMs);
        return domainMappingWriter;
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
        Map<MessageId, VehiclePosition> vehiclePositionQueueCopy;
        synchronized (vehiclePositionQueue) {
            vehiclePositionQueueCopy = new HashMap<>(vehiclePositionQueue);
            vehiclePositionQueue.clear();
        }
        log.error("To write vehiclepositions count: {}", vehiclePositionQueueCopy.size());
        // TODO: write messages here
        // TODO: ack written messages after successful write
    }

    public void close(boolean closePulsar) {
        log.warn("Closing MessageProcessor resources");
        scheduler.shutdown();
        log.info("Scheduler shutdown finished");
        if (closePulsar && application != null) {
            log.info("Closing also Pulsar application");
            application.close();
        }
    }

    public void process(MessageId msgId, Hfp.Data data) {
        switch (data.getTopic().getEventType()) {
            case VP:
                switch (data.getTopic().getJourneyType()) {
                    case journey:
                        vehiclePositionQueue.put(msgId, new VehiclePosition(data.getTopic(), data.getPayload()));
                        break;
                    case deadrun:
                        unsignedEventQueue.put(msgId, new UnsignedEvent(data.getTopic(), data.getPayload()));
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
                stopEventQueue.put(msgId, new StopEvent(data.getTopic(), data.getPayload()));
                break;
            case TLR:
            case TLA:
                lightPriorityEventQueue.put(msgId, new LightPriorityEvent(data.getTopic(), data.getPayload()));
                break;
            case DOO:
            case DOC:
            case DA:
            case DOUT:
            case BA:
            case BOUT:
            case VJA:
            case VJOUT:
                otherEventQueue.put(msgId, new OtherEvent(data.getTopic(), data.getPayload()));
                break;
            default:
                log.warn("Received HFP message with unknown event type: {}", data.getTopic().getEventType());
        }
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
