package fi.hsl.transitlog.hfp.persisthfpdata.archivetodw;

import fi.hsl.transitlog.hfp.domain.*;

import java.io.*;
import java.text.*;

/**
 * Stores filesystem collected csv dumps from previous date to azure blobstorage.
 */
public interface DWUpload {
    String uploadBlob(Event event) throws IOException, ParseException;
}
