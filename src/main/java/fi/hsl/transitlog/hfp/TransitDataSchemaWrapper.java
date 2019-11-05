package fi.hsl.transitlog.hfp;

import fi.hsl.common.transitdata.TransitdataProperties;
import fi.hsl.common.transitdata.TransitdataSchema;
import org.apache.pulsar.client.api.Message;
import org.springframework.stereotype.Component;


@Component
public
class TransitDataSchemaWrapper {
    public boolean hasProtobufSchema(Message message) {
        return TransitdataSchema.hasProtobufSchema(message, TransitdataProperties.ProtobufSchema.HfpData);
    }
}
