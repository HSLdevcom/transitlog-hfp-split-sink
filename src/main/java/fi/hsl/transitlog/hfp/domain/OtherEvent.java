package fi.hsl.transitlog.hfp.domain;
import fi.hsl.common.hfp.proto.Hfp;

import lombok.Data;
import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
public class OtherEvent extends Event {
    public OtherEvent(Hfp.Topic topic, Hfp.Payload payload) {
        super(topic, payload, TableType.OTHEREVENT);
    }

    public OtherEvent() {
    }
}
