package fi.hsl.transitlog.hfp.persisthfpdata.archivetodw;

import fi.hsl.transitlog.hfp.persisthfpdata.archivetodw.azure.AzureBlobClient;

public class MockAzureBlockClient extends AzureBlobClient {

    protected MockAzureBlockClient(String connectionString, String blobContainer) {
        super(connectionString, blobContainer);
    }

    @Override
    public boolean fileExists(String filePath) {
        return true;
    }


    @Override
    protected void uploadFromFile(String filePath) {
        // no-op
    }
}
