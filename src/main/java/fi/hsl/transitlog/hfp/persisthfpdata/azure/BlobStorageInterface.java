package fi.hsl.transitlog.hfp.persisthfpdata.azure;

import fi.hsl.transitlog.hfp.domain.*;

import java.io.*;

/**
 * Stores filesystem collected csv dumps from yesterday to azure blobstorage.
 */
public interface BlobStorageInterface {
    String uploadBlob(Event event) throws IOException;
}
