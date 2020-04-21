package fi.hsl.transitlog.hfp.persisthfpdata.archivetodw.azure;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

@Component
public class PrivateAzureUploader extends AzureUploader {
    public PrivateAzureUploader(@Qualifier("privateAzureBlobClient") AzureBlobClient azureBlobClient) {
        super(azureBlobClient);
    }
}


