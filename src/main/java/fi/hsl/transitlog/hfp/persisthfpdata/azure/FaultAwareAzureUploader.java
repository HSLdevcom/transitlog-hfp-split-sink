package fi.hsl.transitlog.hfp.persisthfpdata.azure;

import fi.hsl.transitlog.hfp.persisthfpdata.azure.filesystem.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import java.io.*;
import java.nio.file.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

/**
 * Scans the filepath for dumps from previous dates
 * and uploads them to azure if necessary
 */
@Component
class FaultAwareAzureUploader extends AzureUploader implements Runnable {
    private final ClassNameAwareFilePathStrategy classNameAwareFileNameStrategy;

    @Autowired
    FaultAwareAzureUploader(AzureBlobClient azureBlobClient, ClassNameAwareFilePathStrategy classNameAwareFilePathStrategy, long initialDelay) {
        super(azureBlobClient);
        this.classNameAwareFileNameStrategy = classNameAwareFilePathStrategy;
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(this, initialDelay, 3600, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        try {
            Stream<Path> walk = Files.walk(Paths.get("/csv"));
            walk.map(Path::toString)
                    .filter(this::isPreviousDate)
                    .forEach(this::uploadBlob);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isPreviousDate(String filePath) {
        Date previousDate = null;
        try {
            previousDate = classNameAwareFileNameStrategy.parseDateFromFilePath(filePath);
        } catch (ParseException e) {
            return false;
        }
        if (previousDate == null) {
            return false;
        }
        long prev = previousDate.getTime();


        // today
        Calendar today = new GregorianCalendar();
        // reset hour, minutes, seconds and millis
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        Date time = today.getTime();
        long now = time.getTime();

        long diff = now - prev;
        long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        return days > 0;
    }

    @Override
    void uploadBlob(String filePath) {
        super.uploadBlob(filePath);
    }
}
