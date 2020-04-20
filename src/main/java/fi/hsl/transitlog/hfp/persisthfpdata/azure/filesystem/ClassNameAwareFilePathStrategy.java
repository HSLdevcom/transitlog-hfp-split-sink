package fi.hsl.transitlog.hfp.persisthfpdata.azure.filesystem;

import fi.hsl.transitlog.hfp.domain.*;
import org.springframework.stereotype.*;

import java.text.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.regex.*;

@Component
public
class ClassNameAwareFilePathStrategy {
    String createFilename(Event event) {
        return "csv/" + event.getClass().getSimpleName() + "/" + DateFormat.todayInDateFormat() + ".csv";
    }

    public Date parseDateFromFilePath(String filePath) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Pattern compile = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
        Matcher matcher = compile.matcher(filePath);
        if (!matcher.find()) {
            throw new ParseException("found no dates in filename", 0);
        }
        return simpleDateFormat.parse(filePath.substring(matcher.start(), matcher.end()));
    }

    private static class DateFormat {
        static String todayInDateFormat() {
            LocalDateTime ldt = LocalDateTime.now();
            DateTimeFormatter year_month_day_format = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
            return year_month_day_format.format(ldt);
        }
    }
}
