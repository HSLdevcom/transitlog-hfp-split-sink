package fi.hsl.transitlog.hfp.persisthfpdata.archivetodw;

import fi.hsl.transitlog.hfp.domain.*;
import fi.hsl.transitlog.hfp.persisthfpdata.archivetodw.azure.*;
import fi.hsl.transitlog.hfp.persisthfpdata.archivetodw.filesystem.*;
import lombok.extern.slf4j.*;
import org.apache.commons.io.*;
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
 * Lazily uploads CSV dumps
 */
@Component
@Profile({"default", "integration-test"})
@Slf4j
public class LazyDWService implements DWUpload {

    private final FileStream fileStream;
    private final AzureUploader azureUploader;
    private final ScheduledExecutorService scheduledExecutorService;
    private final Integer delayBeforeFileDeletion;
    private final ScheduledExecutorService removalExecutorService;
    private Set<DWFile> dwFileSet;
    private long fileLastModifiedInSecondsBuffer;
    private String filePath;


    @Autowired
    LazyDWService(AzureUploader azureUploader, @Value("${fileLastModifiedInSecondsBuffer:300}")
            Integer fileLastModifiedInSecondsBuffer, @Value("${filepath:csv}")
                          String filePath, @Value("${delayBeforeFileDeletionSeconds:1800}") Integer delayBeforeFileDeletionSeconds) throws ParseException {
        this.fileLastModifiedInSecondsBuffer = fileLastModifiedInSecondsBuffer;
        this.delayBeforeFileDeletion = delayBeforeFileDeletionSeconds;
        this.filePath = filePath;
        this.fileStream = new FileStream(new CSVMapper(), filePath);
        this.azureUploader = azureUploader;
        this.dwFileSet = ConcurrentHashMap.newKeySet();
        this.scheduledExecutorService = Executors.newScheduledThreadPool(2);
        this.removalExecutorService = Executors.newScheduledThreadPool(2);
        populateFileSetFromFileSystem();
    }

    private void populateFileSetFromFileSystem() throws ParseException {
        dwFileSet = fileStream.readDWFiles(filePath);
    }

    @Scheduled(fixedDelay = 50000, initialDelay = 3000)
    public void uploadOldEnough() {
        log.info("Attempting to upload old enough blobs");
        Set<DWFile> oldEnoughBlobs = filterSuitableUploads();
        dwFileSet.removeAll(oldEnoughBlobs);
    }

    private Set<DWFile> filterSuitableUploads() {
        return dwFileSet.stream()
                .filter(dwFile -> dwFile.fileSuitableForUpload(fileLastModifiedInSecondsBuffer))
                .map(this::uploadFile)
                .collect(Collectors.toSet());
    }

    private DWFile uploadFile(DWFile dwFile) {
        //Upload file
        if (new File(dwFile.getFilePath()).exists()) {
            azureUploader.uploadBlob(dwFile.getFilePath());
            scheduleForRemoval(dwFile.getFilePath());
            return dwFile;
        }
        return dwFile;
    }

    private void scheduleForRemoval(String filePath) {
        removalExecutorService.schedule(() -> {
            File file = new File(filePath);
            try {
                FileUtils.forceDelete(file);
                log.info("Removed file: {}", file.getPath());
            } catch (IOException e) {
                log.info("Failed to remove file: {}", file.getPath());
                e.printStackTrace();
            }
        }, delayBeforeFileDeletion, TimeUnit.SECONDS);
    }

    @Override
    public String uploadBlob(Event event) throws IOException, ParseException {
        DWFile dwFile = this.fileStream.writeEvent(event);
        dwFileSet.add(dwFile);
        return dwFile.getFilePath();
    }
}
