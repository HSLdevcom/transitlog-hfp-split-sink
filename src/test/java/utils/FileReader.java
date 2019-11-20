package utils;

import fi.hsl.common.files.FileUtils;

import java.io.InputStream;

public class FileReader {

    public String readHfpMqttPayload_1() {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("hfp-sample-vp.json");
        String payload = "";
        try {
            payload = FileUtils.readFileFromStreamOrThrow(stream);
        } catch (Exception e) {
            System.out.println("Could not read vp json for test");
        }
        return payload;
    }

}
