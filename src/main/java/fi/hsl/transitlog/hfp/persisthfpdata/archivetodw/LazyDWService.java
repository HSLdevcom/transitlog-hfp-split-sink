package fi.hsl.transitlog.hfp.persisthfpdata.archivetodw;

import fi.hsl.transitlog.hfp.domain.*;
import fi.hsl.transitlog.hfp.persisthfpdata.archivetodw.filesystem.*;
import lombok.extern.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.*;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

/**
 * Lazily uploads CSV dumps per hour into Azure Blobstorage
 */
@Component
@Profile({"default", "integration-test"})
@Slf4j
public class LazyDWService implements DWUpload {

    private final FileStream fileStream;
    private final AzureUploader azureUploader;
    private final ScheduledExecutorService scheduledExecutorService;
    private final Integer delayBeforeFileDeletion;
    private Set<DWFile> dwFileSet;
    private long fileLastModifiedInSecondsBuffer;
    private String filePath;


    @Autowired
    LazyDWService(AzureUploader azureUploader, @Value("${fileLastModifiedInSecondsBuffer:5}")
            Integer fileLastModifiedInSecondsBuffer, @Value("${filepath:csv}")
                          String filePath, @Value("${delayBeforeFileDeletion:1800}") Integer delayBeforeFileDeletion) throws ParseException {
        this.fileLastModifiedInSecondsBuffer = fileLastModifiedInSecondsBuffer;
        this.delayBeforeFileDeletion = delayBeforeFileDeletion;
        this.filePath = filePath;
        this.fileStream = new FileStream(new CSVMapper(), filePath);
        this.azureUploader = azureUploader;
        this.dwFileSet = ConcurrentHashMap.newKeySet();
        this.scheduledExecutorService = Executors.newScheduledThreadPool(2);
        populateFileSetFromFileSystem();
    }

    private void populateFileSetFromFileSystem() throws ParseException {
        dwFileSet = fileStream.readDWFiles(filePath);
    }

    @Scheduled(fixedDelay = 50000, initialDelay = 3000)
    public void uploadOldEnough() {
        log.info("Attempting to upload old enough blobs");
        List<DWFile> oldEnoughBlobs = dwFileSet.stream()
                .filter(dwFile -> !dwFile.isUploading())
                .filter(dwFile -> {
                    try {
                        DWFile.FileTimeDelays fileTimeDelays = new DWFile.FileTimeDelays(fileLastModifiedInSecondsBuffer, delayBeforeFileDeletion, scheduledExecutorService);
                        return dwFile.archiveOldEnough(azureUploader, fileTimeDelays);
                    } catch (IOException e) {
                        log.error("Error thrown", e);
                    }
                    return false;
                })
                .collect(Collectors.toList());
        dwFileSet.removeAll(oldEnoughBlobs);
    }

    @Override
    public String uploadBlob(Event event) throws IOException, ParseException {
        DWFile dwFile = this.fileStream.writeEvent(event);
        dwFileSet.add(dwFile);
        return dwFile.getFilePath();
    }
}
