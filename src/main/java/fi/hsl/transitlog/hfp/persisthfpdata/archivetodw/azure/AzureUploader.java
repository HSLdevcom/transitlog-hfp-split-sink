package fi.hsl.transitlog.hfp.persisthfpdata.archivetodw.azure;


import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import java.util.concurrent.*;

@Component
public
class AzureUploader {
    final AzureBlobClient azureBlobClient;
    private final ExecutorService executorService;

    @Autowired
    public AzureUploader(AzureBlobClient azureBlobClient) {
        this.azureBlobClient = azureBlobClient;
        executorService = Executors.newCachedThreadPool();
    }

    public AzureUploadTask uploadBlob(String filePath) {
        //Register as task for the asynchronous uploader
        return new AzureUploadTask(azureBlobClient, filePath).run();
    }


}
