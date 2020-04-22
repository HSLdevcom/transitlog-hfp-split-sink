package fi.hsl.transitlog.hfp.persisthfpdata.archivetodw;

import fi.hsl.transitlog.hfp.domain.*;
import fi.hsl.transitlog.hfp.persisthfpdata.archivetodw.filesystem.*;
import lombok.*;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.*;

import java.io.*;
import java.text.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;

@Slf4j
public class DWFile {

    @Getter
    final String filePath;
    final long fileCreatedAt;
    final DWFileName dwFilename;
    private final File file;

    public DWFile(Event event, String rootFolder) throws IOException, ParseException {
        this(new DWFileName().createFileName(rootFolder, event));
    }

    public DWFile(String absoluteFilePath) throws IOException, ParseException {
        this.dwFilename = new DWFileName(absoluteFilePath);
        this.filePath = absoluteFilePath;
        file = new File(absoluteFilePath);
        file.getParentFile().mkdirs();
        file.createNewFile();

        fileCreatedAt = dwFilename.fileTimeStampNow();
    }


    public DWFile(File file) throws ParseException {
        this.filePath = file.getPath();
        this.file = file;
        this.dwFilename = new DWFileName(filePath);
        //Linux does not store creation time so lets assume it's the day in the filename and use that
        this.fileCreatedAt = dwFilename.getFileCreatedAt();
    }

    public void writeEvent(Event event, CSVMapper csvMapper) throws IOException {
        FileWriter fileWriter = new FileWriter(file, true);
        fileWriter.write(csvMapper.format(event));
        fileWriter.close();
    }

    boolean fileSuitableForUpload(long fileLastModifiedInSecondsBuffer) {
        long currentTime = dwFilename.fileTimeStampNow();
        final long diff = (currentTime - fileLastModified()) / 1000;
        return diff > fileLastModifiedInSecondsBuffer;
    }

    private long fileLastModified() {
        return file.lastModified();
    }


    @Override
    public int hashCode() {
        return filePath.hashCode() * 31;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DWFile) {
            return ((DWFile) obj).filePath.equals(filePath);
        }
        return false;
    }

    static
    class FileTimeDelays {

        final long fileLastModifiedInSecondsBuffer;
        final Integer delayBeforeFileDeletion;
        final ScheduledExecutorService scheduledExecutorService;

        FileTimeDelays(long fileLastModifiedInSecondsBuffer, Integer delayBeforeFileDeletion, ScheduledExecutorService scheduledExecutorService) {
            this.fileLastModifiedInSecondsBuffer = fileLastModifiedInSecondsBuffer;
            this.delayBeforeFileDeletion = delayBeforeFileDeletion;
            this.scheduledExecutorService = scheduledExecutorService;
        }
    }

    @NoArgsConstructor
    static
    class DWFileName {
        //Hours 0-23
        private static final String DW_FILE_DATEFORMAT = "yyyy-MM-dd-HH";
        private static final String TIMEZONE = "Europe/Helsinki";
        @Delegate
        private Date dwFileNameWithDateComponent;

        DWFileName(String absoluteFilePath) throws ParseException {
            this.dwFileNameWithDateComponent = new Date(absoluteFilePath);
        }

        String createFileName(String rootFolder, Event event) {
            return rootFolder + "/" + event.getClass().getSimpleName() + "/" + eventDateInDWFormat(event) + ".csv";
        }

        private static String eventDateInDWFormat(Event event) {
            LocalDateTime tstLocalizedDateTime = event.getTst().toInstant().atZone(ZoneId.of(DWFileName.TIMEZONE)).toLocalDateTime();
            DateTimeFormatter year_month_day_hour_format = DateTimeFormatter.ofPattern(DWFileName.DW_FILE_DATEFORMAT, Locale.ENGLISH);
            return year_month_day_hour_format.format(tstLocalizedDateTime);
        }

        @Data
        private static class Date {
            private final long fileCreatedAt;

            Date(String absoluteFilePath) throws ParseException {
                fileCreatedAt = parseFileCreatedAt(new File(absoluteFilePath));
            }

            private long parseFileCreatedAt(File file) throws ParseException {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DWFileName.DW_FILE_DATEFORMAT);
                Pattern compile = Pattern.compile("\\d{4}-\\d{2}-\\d{2}-\\d{2}");
                Matcher matcher = compile.matcher(file.getPath());
                if (!matcher.find()) {
                    throw new ParseException("found no dates in filename", 0);
                }
                return simpleDateFormat.parse(file.getPath().substring(matcher.start(), matcher.end())).getTime();
            }

            public long fileTimeStampNow() {
                Calendar today = new GregorianCalendar(TimeZone.getTimeZone(DWFileName.TIMEZONE));
                java.util.Date time = today.getTime();
                return time.getTime();
            }
        }
    }
}
