package fi.hsl.transitlog.hfp.persisthfpdata.archivetodw;

import fi.hsl.transitlog.hfp.domain.*;
import fi.hsl.transitlog.hfp.persisthfpdata.*;
import org.apache.commons.io.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.test.context.*;
import org.springframework.test.context.junit.jupiter.*;

import java.io.*;
import java.text.*;

@TestPropertySource(value = "classpath:/application.properties")
@SpringJUnitConfig(classes = {DWTest.class})
public class LazyDWServiceIT extends AbstractPodamTest {
    @Autowired
    private LazyDWService lazyDWService;

    @Test
    public void lazyDWService() throws InterruptedException, IOException, ParseException {
        OtherEvent otherEvent = podamFactory.manufacturePojo(OtherEvent.class);
        lazyDWService.uploadBlob(otherEvent);
        lazyDWService.uploadBlob(otherEvent);
        Thread.sleep(10000000L);
    }

    @AfterEach
    public void clean() throws IOException {
        FileUtils.forceDelete(new File("csv"));
    }

}
