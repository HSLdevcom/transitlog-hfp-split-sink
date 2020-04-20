package fi.hsl.transitlog.hfp.persisthfpdata.archivetodw;

import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.*;

@Configuration
@ComponentScan(basePackageClasses = LazyDWService.class)
@EnableScheduling
public class DWTest {
}
