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
    private final AzureUploader faultAwareAzureUploader;

    @Autowired
    LazyBlobstorage(FileStream fileStream, @Qualifier("faultAwareAzureUploader") AzureUploader azureUploader) {
        this.fileStream = fileStream;
        this.faultAwareAzureUploader = azureUploader;
    }

    @Override
    public String uploadBlob(Event event) throws IOException {
        return this.fileStream.writeEvent(event);
    }
}
