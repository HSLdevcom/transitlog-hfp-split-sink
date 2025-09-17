package fi.hsl.transitlog.hfp.persisthfpdata.archivetodw.azure;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

@Component
public class AzureUploader {
    final AzureBlobClient azureBlobClient;
    private final PrivateAzureBlobClient privateAzureBlobClient;

    @Autowired
    public AzureUploader(AzureBlobClient azureBlobClient, PrivateAzureBlobClient privateAzureBlobClient) {
        this.azureBlobClient = azureBlobClient;
        this.privateAzureBlobClient = privateAzureBlobClient;
    }

    public AzureUploadTask uploadBlob(String filePath) {
        //Register as task for the asynchronous uploader
        if (filePath.contains("private")) {
            return new AzureUploadTask(privateAzureBlobClient, filePath).run();
        }
        return new AzureUploadTask(azureBlobClient, filePath).run();
    }

}
