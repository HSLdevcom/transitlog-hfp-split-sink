package fi.hsl.transitlog.hfp.persisthfpdata.archivetodw.azure;

import lombok.extern.slf4j.*;

@Slf4j
public class AzureUploadTask {
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

    public boolean isUploaded() {
        return this.blobClient.fileExists(filePath);
    }
}
