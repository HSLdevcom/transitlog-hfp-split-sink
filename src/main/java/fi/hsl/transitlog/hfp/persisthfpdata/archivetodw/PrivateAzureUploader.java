package fi.hsl.transitlog.hfp.persisthfpdata.archivetodw;

import com.azure.storage.blob.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

@Component
public class PrivateAzureUploader extends AzureUploader {
    PrivateAzureUploader(@Qualifier("privateAzureBlobClient") AzureBlobClient azureBlobClient) {
        super(azureBlobClient);
    }
}

@Component
class PrivateAzureBlobClient extends AzureUploader.AzureBlobClient {
    PrivateAzureBlobClient(@Value("${blobstorage.connectionString}") String connectionString, @Value("${blobstorage.privateblobcontainer}") String blobContainer) {
        super(connectionString, blobContainer);
    }

    void uploadFromFile(String filePath) {
        BlobClient blobClient = blobContainerClient.getBlobClient(filePath);
        blobClient.uploadFromFile(filePath, true);
    }

    boolean fileExists(String filePath) {
        blobContainerClient.getBlobClient(filePath);
        return blobContainerClient.exists();
    }
}


