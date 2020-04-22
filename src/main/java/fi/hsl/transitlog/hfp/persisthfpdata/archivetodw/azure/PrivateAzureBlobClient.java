package fi.hsl.transitlog.hfp.persisthfpdata.archivetodw.azure;

import com.azure.storage.blob.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

@Component
class PrivateAzureBlobClient extends AzureBlobClient {
    PrivateAzureBlobClient(@Value("${blobstorage.connectionString}") String connectionString, @Value("${blobstorage.privateblobcontainer}") String blobContainer) {
        super(connectionString, blobContainer);
    }

    void uploadFromFile(String filePath) {
        BlobClient blobClient = blobContainerClient.getBlobClient(filePath);
        blobClient.uploadFromFile(filePath, true);
    }

    public boolean fileExists(String filePath) {
        blobContainerClient.getBlobClient(filePath);
        return blobContainerClient.exists();
    }
}