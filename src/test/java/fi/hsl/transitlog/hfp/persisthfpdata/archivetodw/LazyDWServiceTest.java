package fi.hsl.transitlog.hfp.persisthfpdata.archivetodw;

import fi.hsl.transitlog.hfp.domain.*;
import fi.hsl.transitlog.hfp.persisthfpdata.*;
import fi.hsl.transitlog.hfp.persisthfpdata.archivetodw.azure.*;
import org.apache.commons.io.*;
import org.junit.jupiter.api.*;

import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;

import static org.awaitility.Awaitility.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.*;

class LazyDWServiceTest extends AbstractPodamTest {
    private LazyDWService lazyBlobStorage;
    private AzureBlobClient blobClientWrapper;
    private File file;
    private PrivateAzureBlobClient privateBlobClient;

    @BeforeEach
    void setUp() throws IOException, ParseException {
        UUID uuid = UUID.randomUUID();
        //Create a test file if it doesn't exist already
        String fileFolder = "csv" + uuid.toString();
        file = new File(fileFolder + "/VehiclePosition/2010-01-02-02-hfp.csv");
        file.getParentFile().mkdirs();
        file.createNewFile();
        blobClientWrapper = mock(AzureBlobClient.class);
        privateBlobClient = mock(PrivateAzureBlobClient.class);
        this.lazyBlobStorage = new LazyDWService(new AzureUploader(blobClientWrapper, privateBlobClient), 1, fileFolder,
                3, true);

        when(privateBlobClient.fileExists(any())).thenReturn(true);
        when(blobClientWrapper.fileExists(any())).thenReturn(true);
    }

    @Test
    void uploadOtherEvent() throws IOException, InterruptedException, ParseException {
        OtherEvent otherEvent = podamFactory.manufacturePojo(OtherEvent.class);
        createEventInPast(otherEvent);
        String createdFile = this.lazyBlobStorage.uploadBlob(otherEvent);
        Thread.sleep(2000);
        this.lazyBlobStorage.uploadOldEnough();
        //Verify vehicleposition is deleted
        verifyFileDeleted(file);
        verifyFileDeleted(createdFile);
    }

    private void verifyFileDeleted(File file) {
        await().atMost(60, TimeUnit.SECONDS).pollDelay(5, TimeUnit.SECONDS)
                .untilAsserted(() -> assertTrue("File is not deleted", !file.exists()));
    }

    private void verifyFileDeleted(String createdFile) {
        verifyFileDeleted(new File(createdFile));
    }

    public void createEventInPast(Event event) {
        GregorianCalendar gregorianCalendar = new GregorianCalendar(1980, 1, 20);
        event.setTst(new Timestamp(gregorianCalendar.getTimeInMillis()));
    }

    @Test
    void uploadUnsignedEvent() throws IOException, InterruptedException, ParseException {
        UnsignedEvent unsignedEvent = podamFactory.manufacturePojo(UnsignedEvent.class);
        createEventInPast(unsignedEvent);
        String createdFile = this.lazyBlobStorage.uploadBlob(unsignedEvent);
        Thread.sleep(2000);
        this.lazyBlobStorage.uploadOldEnough();
        verifyFileDeleted(file);
        verifyFileDeleted(createdFile);
    }

    @Test
    void uploadLightPriorityEvent() throws IOException, InterruptedException, ParseException {
        LightPriorityEvent lightPriorityEvent = podamFactory.manufacturePojo(LightPriorityEvent.class);
        createEventInPast(lightPriorityEvent);
        String createdFile = this.lazyBlobStorage.uploadBlob(lightPriorityEvent);
        Thread.sleep(2000);
        this.lazyBlobStorage.uploadOldEnough();
        verifyFileDeleted(file);
        verifyFileDeleted(createdFile);

    }

    @Test
    void uploadBlob() throws IOException, InterruptedException, ParseException {
        VehiclePosition vehiclePosition = podamFactory.manufacturePojo(VehiclePosition.class);
        createEventInPast(vehiclePosition);
        String createdFile = this.lazyBlobStorage.uploadBlob(vehiclePosition);
        Thread.sleep(2000);
        this.lazyBlobStorage.uploadOldEnough();
        verifyFileDeleted(file);
        verifyFileDeleted(createdFile);

    }

    @AfterEach
    void cleanUp() throws IOException {
        FileUtils.forceDelete(file.getParentFile().getParentFile());
    }
}
