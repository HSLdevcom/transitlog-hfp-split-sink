package fi.hsl.transitlog.hfp.persisthfpdata.azure;

import fi.hsl.transitlog.hfp.domain.*;
import lombok.extern.slf4j.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;

import java.io.*;

@Slf4j
@Profile("prod")
@Component
public class BlobStorageNoOpInterface implements BlobStorageInterface {
    @Override
    public String uploadBlob(Event event) throws IOException {
        log.info("Azure uploading not yet implemented");
        return "Azure uploading not yet implemented";
    }
}
