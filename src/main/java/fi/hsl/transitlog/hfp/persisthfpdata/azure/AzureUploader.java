package fi.hsl.transitlog.hfp.persisthfpdata.azure;


import com.azure.storage.blob.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import java.io.*;
import java.util.concurrent.*;

class AzureUploader {
    private final ExecutorService executorService;
    private final AzureBlobClient azureBlobClient;

    AzureUploader(AzureBlobClient azureBlobClient) {
        this.azureBlobClient = azureBlobClient;
        executorService = Executors.newCachedThreadPool();
    }

    void uploadBlob(String filePath) {
        //Register as task for the asynchronous uploader
        executorService.submit(new AzureUploadTask(azureBlobClient, filePath));
    }

    @Component
    static
    class AzureBlobClient {
        private BlobContainerClient blobContainerClient;

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

    private static class AzureUploadTask implements Runnable {
        private final AzureBlobClient blobClient;
        private final String filePath;

        AzureUploadTask(AzureBlobClient blobClient, String filePath) {
            this.blobClient = blobClient;
            this.filePath = filePath;
        }

        @Override
        public void run() {
            this.blobClient.uploadFromFile(filePath);
            //Check file exists and then remove it from filesystem
            if (this.blobClient.fileExists(filePath)) {
                File file = new File(filePath);
                file.delete();
            }
        }
    }
}
