package fi.hsl.transitlog.hfp.persisthfpdata.archivetodw.azure;

import com.azure.storage.blob.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

@Component
public class AzureBlobClient {
    private final BlobServiceClient blobServiceClient;
    private final String blobContainer;

    private BlobContainerClient blobContainerClient;

    AzureBlobClient(@Value(value = "${blobstorage.connectionString}") String connectionString, @Value(value = "${blobstorage.blobcontainer}") String blobContainer) {
        this.blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        this.blobContainer = blobContainer;
    }

    public synchronized BlobContainerClient getBlobContainerClient() {
        if (blobContainerClient == null) {
            if (blobServiceClient.getBlobContainerClient(blobContainer).exists()) {
                this.blobContainerClient = blobServiceClient.getBlobContainerClient(blobContainer);
            } else {
                this.blobContainerClient = blobServiceClient.createBlobContainer(blobContainer);
            }
        }

        return blobContainerClient;
    }

    void uploadFromFile(String filePath) {
        BlobClient blobClient = getBlobContainerClient().getBlobClient(filePath);
        blobClient.uploadFromFile(filePath, true);
    }

    public boolean fileExists(String filePath) {
        getBlobContainerClient().getBlobClient(filePath);
        return getBlobContainerClient().exists();
    }
}

