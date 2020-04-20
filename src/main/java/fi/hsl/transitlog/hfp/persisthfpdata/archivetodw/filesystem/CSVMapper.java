package fi.hsl.transitlog.hfp.persisthfpdata.archivetodw.filesystem;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.csv.*;
import fi.hsl.transitlog.hfp.domain.*;

public class CSVMapper {
    public String format(Event event) throws JsonProcessingException {
        CsvMapper schemaMapper = new CsvMapper();
        CsvSchema schema = schemaMapper.typedSchemaFor(event.getClass());
        ObjectWriter writer = schemaMapper.writer(schema);
        return
                writer.writeValueAsString(event);

    }
}