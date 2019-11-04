package fi.hsl.transitlog.hfp;

import fi.hsl.common.hfp.proto.Hfp;
import fi.hsl.common.pulsar.IMessageHandler;

import fi.hsl.common.transitdata.TransitdataProperties;
import fi.hsl.common.transitdata.TransitdataSchema;
import fi.hsl.transitlog.hfp.persisthfpdata.DomainMappingWriter;
import org.apache.pulsar.client.api.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageProcessor implements IMessageHandler {

    private DomainMappingWriter domainMappingWriter;
    private static final Logger log = LoggerFactory.getLogger(MessageProcessor.class);

    public MessageProcessor(DomainMappingWriter writer) {
        domainMappingWriter = writer;
    }

    @Override
    public void handleMessage(Message message) throws Exception {
        if (TransitdataSchema.hasProtobufSchema(message, TransitdataProperties.ProtobufSchema.HfpData)) {
            Hfp.Data data = Hfp.Data.parseFrom(message.getData());
            domainMappingWriter.process(message.getMessageId(), data);
        } else {
            log.warn("Invalid protobuf schema, expecting HfpData");
        }
        // Messages should not be acked already here but only after successful write
    }
}
