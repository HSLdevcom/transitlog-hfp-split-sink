package fi.hsl.transitlog.hfp.persisthfpdata.archivetodw;


import com.azure.storage.blob.*;
import lombok.extern.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import java.util.concurrent.*;

@Component
class AzureUploader {
    private final ExecutorService executorService;
    final AzureBlobClient azureBlobClient;

    AzureUploader(AzureBlobClient azureBlobClient) {
        this.azureBlobClient = azureBlobClient;
        executorService = Executors.newCachedThreadPool();
    }

    AzureUploadTask uploadBlob(String filePath) {
        //Register as task for the asynchronous uploader
        return new AzureUploadTask(azureBlobClient, filePath).run();
    }

    @Component
    static
    class AzureBlobClient {
        BlobContainerClient blobContainerClient;

        AzureBlobClient(@Value(value = "${blobstorage.connectionString}") String connectionString, @Value(value = "${blobstorage.blobcontainer}") String blobContainer) {
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();
            if (blobServiceClient.getBlobContainerClient(blobContainer).exists()) {
                this.blobContainerClient = blobServiceClient.getBlobContainerClient(blobContainer);
            } else {
                this.blobContainerClient = blobServiceClient.createBlobContainer(blobContainer);
            }

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

    @Slf4j
    static class AzureUploadTask {
        private final AzureBlobClient blobClient;
        private final String filePath;

        AzureUploadTask(AzureBlobClient blobClient, String filePath) {
            this.blobClient = blobClient;
            this.filePath = filePath;
        }

        public AzureUploadTask run() {
            log.info("Uploading dump from filepath: {}", filePath);
            this.blobClient.uploadFromFile(filePath);
            return this;
        }

        boolean isUploaded() {
            return this.blobClient.fileExists(filePath);
        }
    }
}
