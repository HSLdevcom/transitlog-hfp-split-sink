package fi.hsl.transitlog.hfp.persisthfpdata.azure;

import fi.hsl.transitlog.hfp.domain.*;
import fi.hsl.transitlog.hfp.persisthfpdata.azure.filesystem.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;

import java.io.*;

/**
 * Lazily uploads CSV dumps daily into Azure Blobstorage
 */
@Component
@Profile("default")
public class LazyBlobstorage implements BlobStorageInterface {

    private final FileStream fileStream;
    private final FaultAwareAzureUploader faultAwareAzureUploader;

    @Autowired
    LazyBlobstorage(FileStream fileStream, FaultAwareAzureUploader faultAwareAzureUploader) {
        this.fileStream = fileStream;
        this.faultAwareAzureUploader = faultAwareAzureUploader;
    }

    @Override
    public String uploadBlob(Event event) throws IOException {
        return this.fileStream.writeEvent(event);
    }
}
