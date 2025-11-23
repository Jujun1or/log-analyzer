package academy.report;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

public final class PrettyJson {

    public static final DefaultPrettyPrinter PRINTER;

    static {
        DefaultPrettyPrinter.Indenter indenter = new DefaultIndenter("    ", DefaultIndenter.SYS_LF);

        DefaultPrettyPrinter printer = new DefaultPrettyPrinter();

        printer.indentObjectsWith(indenter);

        printer.indentArraysWith(indenter);

        PRINTER = printer;
    }

    private PrettyJson() {}
}
