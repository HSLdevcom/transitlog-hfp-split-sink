package fi.hsl.transitlog.hfp.persisthfpdata.azure.filesystem;

import fi.hsl.transitlog.hfp.domain.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import java.io.*;

@Component
public
class FileStream {
    private final ClassNameAwareFilePathStrategy classNameAwareFilePathStrategy;
    private CSVMapper csvMapper;

    @Autowired
    public FileStream(CSVMapper format, ClassNameAwareFilePathStrategy classNameAwareFilePathStrategy) {
        this.csvMapper = format;
        this.classNameAwareFilePathStrategy = classNameAwareFilePathStrategy;
    }

    public String writeEvent(Event event) throws IOException {
        String filePath = classNameAwareFilePathStrategy.createFilename(event);
        File file = new File(filePath);
        file.getParentFile().mkdirs();
        file.createNewFile();
        FileWriter fileWriter = new FileWriter(file, true);
        fileWriter.write(csvMapper.format(event));
        fileWriter.close();
        return filePath;
    }

}
