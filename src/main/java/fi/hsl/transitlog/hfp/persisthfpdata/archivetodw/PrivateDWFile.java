package fi.hsl.transitlog.hfp.persisthfpdata.archivetodw;

import fi.hsl.transitlog.hfp.domain.*;

import java.io.*;
import java.text.*;

public class PrivateDWFile extends DWFile {
    public PrivateDWFile(File file) throws ParseException {
        super(file);
    }

    public PrivateDWFile(Event event, String rootFolder) throws IOException, ParseException {
        super(event, rootFolder);
    }
}
