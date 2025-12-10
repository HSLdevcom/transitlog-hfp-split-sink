package fi.hsl.transitlog.hfp.domain;
import fi.hsl.common.hfp.proto.Hfp;

import jakarta.persistence.Entity;
import lombok.Data;

@Data
@Entity
public class UnsignedEvent extends Event {
    public UnsignedEvent(Hfp.Topic topic, Hfp.Payload payload) {
        super(topic, payload);
    }

    public UnsignedEvent() {
    }
}
