package academy.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.StringWriter;

public final class JsonUtil {

    private static final ObjectMapper mapper = new ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .findAndRegisterModules();

    private JsonUtil() {}

    public static String prettyWrite(Object value) {
        try {
            StringWriter writer = new StringWriter();
            mapper.writer(PrettyJson.PRINTER).writeValue(writer, value);
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException("JSON serialization failed", e);
        }
    }
}
