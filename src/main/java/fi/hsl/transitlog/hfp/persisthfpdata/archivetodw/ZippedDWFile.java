package fi.hsl.transitlog.hfp.persisthfpdata.archivetodw;

import fi.hsl.transitlog.hfp.persisthfpdata.archivetodw.azure.*;

import java.io.*;
import java.nio.file.*;
import java.text.*;
import java.util.concurrent.*;
import java.util.zip.*;

class ZippedDWFile extends DWFile {

    private String zipFileName;

    ZippedDWFile(DWFile dwFile) throws ParseException {
        super(new File(dwFile.getFilePath()));
    }

    @Override
    boolean archiveOldEnough(AzureUploader azureUploader, FileTimeDelays fileTimeDelays) throws IOException {
        return archiveOldEnough(fileTimeDelays.fileLastModifiedInSecondsBuffer, azureUploader, fileTimeDelays.scheduledExecutorService, fileTimeDelays.delayBeforeFileDeletion);
    }

    private boolean archiveOldEnough(long lastModifiedBufferInSeconds, AzureUploader azureUploader, ScheduledExecutorService executorService, long delayBeforeRemovalSeconds) throws IOException {
        zipFile();
        long currentTime = dwFilename.fileTimeStampNow();
        long diff = currentTime - fileCreatedAt;
        if (diff / 1000 > lastModifiedBufferInSeconds) {
            //Upload file
            AzureUploadTask azureUploadTask = azureUploader.uploadBlob(zipFileName);
            if (azureUploadTask.isUploaded()) {
                scheduleForRemoval(zipFileName, executorService, delayBeforeRemovalSeconds);
                scheduleForRemoval(filePath, executorService, delayBeforeRemovalSeconds);
                return true;
            }
        }
        return false;
    }

    private void zipFile() throws IOException {
        File file = new File(filePath);
        zipFileName = file.getPath().concat(".zip");

        FileOutputStream fos = new FileOutputStream(zipFileName);
        ZipOutputStream zos = new ZipOutputStream(fos);

        zos.putNextEntry(new ZipEntry(file.getName()));

        byte[] bytes = Files.readAllBytes(Paths.get(filePath));
        zos.write(bytes, 0, bytes.length);
        zos.closeEntry();
        zos.close();

    }
}

