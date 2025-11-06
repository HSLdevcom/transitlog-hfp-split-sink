package fi.hsl.transitlog.hfp;

import com.google.protobuf.InvalidProtocolBufferException;
import fi.hsl.common.hfp.proto.Hfp;
import org.springframework.stereotype.Component;

@Component
public class HfpDataParser {
    public Hfp.Data parseFrom(byte[] data) throws InvalidProtocolBufferException {
        return Hfp.Data.parseFrom(data);
    }
}
