package fi.hsl.transitlog.hfp.domain;

import fi.hsl.common.hfp.proto.Hfp;
import lombok.Data;

import javax.persistence.Entity;

@Data
@Entity
public class VehiclePosition extends Event {
    public VehiclePosition(Hfp.Topic topic, Hfp.Payload payload) {
        super(topic, payload, TableType.VEHICLEPOSITION);
    }

    public VehiclePosition() {
    }
}
