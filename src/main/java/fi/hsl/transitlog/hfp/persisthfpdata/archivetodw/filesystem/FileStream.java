package fi.hsl.transitlog.hfp.persisthfpdata.archivetodw.filesystem;

import fi.hsl.transitlog.hfp.domain.*;
import fi.hsl.transitlog.hfp.persisthfpdata.archivetodw.*;
import org.apache.commons.io.*;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;

public
class FileStream {
    private final String csvFolder;
    private CSVMapper csvMapper;
    private Map<String, DWFile> dwFiles;

    public FileStream(CSVMapper format, String filePath) {
        this.csvMapper = format;
        this.dwFiles = new ConcurrentHashMap<>();
        this.csvFolder = filePath;
    }

    public Set<DWFile> readDWFiles(String filePath) throws ParseException {
        File file = new File(filePath);
        if (!file.exists()) {
            return ConcurrentHashMap.newKeySet();
        }
        Set<DWFile> dwFiles = ConcurrentHashMap.newKeySet();
        Iterator<File> fileIterator = FileUtils.iterateFiles(new File(filePath), null, true);
        while (fileIterator.hasNext()) {
            File dwFile = fileIterator.next();
            if (dwFile.getPath().contains("private")) {
                dwFiles.add(new PrivateDWFile(dwFile));
            } else {
                dwFiles.add(new DWFile(dwFile));
            }
        }
        return dwFiles;
    }

    public DWFile writeEvent(Event event) throws IOException, ParseException {
        DWFile dwFile;
        if (event.getJourney_type().equals("journey")) {
            dwFile = new DWFile(event, csvFolder);
        } else {
            dwFile = new PrivateDWFile(event, csvFolder + "/private");
        }

        String filePath = dwFile.getFilePath();
        if (dwFiles.containsKey(filePath)) {
            dwFile = dwFiles.get(filePath);
        } else {
            dwFile = new DWFile(filePath);
        }
        dwFile.writeEvent(event, csvMapper);
        return dwFile;
    }

}
