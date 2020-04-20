package fi.hsl.transitlog.hfp.persisthfpdata.azure;

import fi.hsl.transitlog.hfp.domain.*;
import fi.hsl.transitlog.hfp.persisthfpdata.*;
import fi.hsl.transitlog.hfp.persisthfpdata.azure.filesystem.*;
import org.junit.jupiter.api.*;

import java.io.*;
import java.util.concurrent.*;

import static org.awaitility.Awaitility.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.*;

class LazyBlobstorageTest extends AbstractPodamTest {
    private LazyBlobstorage lazyBlobStorage;
    private AzureUploader.AzureBlobClient blobClientWrapper;
    private File file;

    @BeforeEach
    void setUp() throws IOException {
        //Create a test file if it doesn't exist already
        file = new File("csv/VehiclePosition/2010-01-02-hfp.csv");
        file.getParentFile().mkdirs();
        file.createNewFile();
        blobClientWrapper = mock(AzureUploader.AzureBlobClient.class);
        ClassNameAwareFilePathStrategy classNameAwareFilePathStrategy = new ClassNameAwareFilePathStrategy();
        CSVMapper format = new CSVMapper();
        FileStream fileStream = new FileStream(format, classNameAwareFilePathStrategy);
        FaultAwareAzureUploader faultAwareAzureUploader = new FaultAwareAzureUploader(blobClientWrapper, classNameAwareFilePathStrategy, 5L);
        this.lazyBlobStorage = new LazyBlobstorage(fileStream, faultAwareAzureUploader);
        when(blobClientWrapper.fileExists(any())).thenReturn(true);
    }

    @Test
    void uploadOtherEvent() throws IOException {
        OtherEvent otherEvent = podamFactory.manufacturePojo(OtherEvent.class);
        String createdFile = this.lazyBlobStorage.uploadBlob(otherEvent);
        verifyFileDeleted(createdFile);
    }

    private void verifyFileDeleted(String createdFile) {
        await()
                .atMost(60, TimeUnit.SECONDS)
                .pollDelay(1, TimeUnit.SECONDS)
                .untilAsserted(() -> assertTrue("File is not deleted", !file.exists()));
        File file = new File(createdFile);
        file.delete();
        file.getParentFile().delete();
    }

    @Test
    void uploadUnsignedEvent() throws IOException {
        UnsignedEvent unsignedEvent = podamFactory.manufacturePojo(UnsignedEvent.class);
        String createdFile = this.lazyBlobStorage.uploadBlob(unsignedEvent);
        verifyFileDeleted(createdFile);
    }

    @Test
    void uploadLightPriorityEvent() throws IOException {
        LightPriorityEvent lightPriorityEvent = podamFactory.manufacturePojo(LightPriorityEvent.class);
        String createdFile = this.lazyBlobStorage.uploadBlob(lightPriorityEvent);
        //Await to see file is actually deleted after a brief period of time
        verifyFileDeleted(createdFile);

    }

    @Test
    void uploadBlob() throws IOException {
        VehiclePosition vehiclePosition = podamFactory.manufacturePojo(VehiclePosition.class);
        String createdFile = this.lazyBlobStorage.uploadBlob(vehiclePosition);
        await()
                .atMost(60, TimeUnit.SECONDS)
                .pollDelay(1, TimeUnit.SECONDS)
                .untilAsserted(() -> assertTrue("File is not deleted", !file.exists()));
        //Verify a record was created for today
        File file = new File(createdFile);
        assertTrue("File doesn't exist", file.exists());
        file.delete();
        file.getParentFile().delete();
    }

}











