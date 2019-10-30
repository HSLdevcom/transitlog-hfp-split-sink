package fi.hsl.transitlog.hfp.domain;
import fi.hsl.common.hfp.proto.Hfp;

import lombok.Data;
import javax.persistence.Entity;

@Data
@Entity
public class StopEvent extends Event {
    public StopEvent(Hfp.Topic topic, Hfp.Payload payload) {
        super(topic, payload, TableType.STOPEVENT);
    }

    public StopEvent() {
    }
}
