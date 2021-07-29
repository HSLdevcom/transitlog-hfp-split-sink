package fi.hsl.transitlog.hfp.persisthfpdata.archivetodw.azure;

import com.azure.storage.blob.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.zip.GZIPOutputStream;

@Component
@Slf4j
public class AzureBlobClient {
    BlobContainerClient blobContainerClient;

    private final boolean compress;

    AzureBlobClient(@Value(value = "${blobstorage.connectionString}") String connectionString,
                    @Value(value = "${blobstorage.blobcontainer}") String blobContainer,
                    @Value("${csv.compress:false}") boolean compress) {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
        if (blobServiceClient.getBlobContainerClient(blobContainer).exists()) {
            this.blobContainerClient = blobServiceClient.getBlobContainerClient(blobContainer);
        } else {
            this.blobContainerClient = blobServiceClient.createBlobContainer(blobContainer);
        }

        this.compress = compress;
    }

    private BlobClient getBlobClient(String filePath) {
        //Add .gz to filename if using compression
        return blobContainerClient.getBlobClient(compress ? filePath + ".gz" : filePath);
    }

    void uploadFromFile(String filePath) {
        BlobClient blobClient = getBlobClient(filePath);
        if (compress) {
            log.info("Uploading {} with compression to blob {}", filePath, blobClient.getBlobName());
            try (OutputStream os = new GZIPOutputStream(blobClient.getBlockBlobClient().getBlobOutputStream(true), 32768)) {
                FileUtils.copyFile(new File(filePath), os);
            } catch (IOException e) {
                //Wrap to UncheckedIOException because blobClient.uploadFromFile does the same
                throw new UncheckedIOException(e);
            }
        } else {
            blobClient.uploadFromFile(filePath, true);
        }
    }

    public boolean fileExists(String filePath) {
        return getBlobClient(filePath).exists();
    }
}

