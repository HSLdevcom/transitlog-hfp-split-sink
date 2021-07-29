package fi.hsl.transitlog.hfp.persisthfpdata.archivetodw.azure;

import com.azure.storage.blob.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

@Component
public class PrivateAzureBlobClient extends AzureBlobClient {
    PrivateAzureBlobClient(@Value("${blobstorage.connectionString}") String connectionString, @Value("${blobstorage.privateblobcontainer}") String blobContainer, @Value("${csv.compress:false}") boolean compress) {
        super(connectionString, blobContainer, compress);
    }
}
