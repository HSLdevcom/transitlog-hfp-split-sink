package fi.hsl.transitlog.hfp.persisthfpdata;

import fi.hsl.common.hfp.proto.*;
import fi.hsl.common.pulsar.*;
import fi.hsl.transitlog.hfp.*;
import org.apache.pulsar.client.api.*;
import org.slf4j.*;
import org.springframework.stereotype.*;

@Component
public class MessageProcessor implements IMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(MessageProcessor.class);
    private final TransitDataSchemaWrapper transitdataSchemaWrapper;
    private final HfpDataParser hfpDataParser;
    private DomainMappingWriter domainMappingWriter;

    public MessageProcessor(DomainMappingWriter writer, TransitDataSchemaWrapper transitdataSchemaWrapper, HfpDataParser hfpDataParser) {
        this.domainMappingWriter = writer;
        this.transitdataSchemaWrapper = transitdataSchemaWrapper;
        this.hfpDataParser = hfpDataParser;
    }

    @Override
    public void handleMessage(Message message) throws Exception {
        if (transitdataSchemaWrapper.hasProtobufSchema(message)) {
            Hfp.Data data = hfpDataParser.parseFrom(message.getData());
            domainMappingWriter.process(message.getMessageId(), data);
        } else {
            log.warn("Invalid protobuf schema, expecting HfpData");
        }
        // Messages should not be acked already here but only after successful write
    }
}
