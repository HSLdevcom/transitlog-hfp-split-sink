package fi.hsl.transitlog.hfp.persisthfpdata.archivetodw.azure;

import com.azure.storage.blob.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

@Component
public class AzureBlobClient {
    BlobContainerClient blobContainerClient;

    AzureBlobClient(@Value(value = "${blobstorage.connectionString}") String connectionString, @Value(value = "${blobstorage.blobcontainer}") String blobContainer) {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
        if (blobServiceClient.getBlobContainerClient(blobContainer).exists())
            this.blobContainerClient = blobServiceClient.getBlobContainerClient(blobContainer);
        else
            this.blobContainerClient = blobServiceClient.createBlobContainer(blobContainer);

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

