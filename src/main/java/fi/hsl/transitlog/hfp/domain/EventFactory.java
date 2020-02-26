package fi.hsl.transitlog.hfp.domain;

import fi.hsl.common.hfp.proto.*;

public class EventFactory {
    public Event createVehiclePositionEvent(Hfp.Topic topic, Hfp.Payload payload) {
        return new VehiclePosition(topic, payload);
    }

    public Event createUnsignedEvent(Hfp.Topic topic, Hfp.Payload payload) {
        return new UnsignedEvent(topic, payload);
    }

    public Event createStopEvent(Hfp.Topic topic, Hfp.Payload payload) {
        return new StopEvent(topic, payload);
    }

    public Event createLightPriorityEvent(Hfp.Topic topic, Hfp.Payload payload) {
        return new LightPriorityEvent(topic, payload);
    }

    public Event createOtherEvent(Hfp.Topic topic, Hfp.Payload payload) {
        return new OtherEvent(topic, payload);
    }
}
