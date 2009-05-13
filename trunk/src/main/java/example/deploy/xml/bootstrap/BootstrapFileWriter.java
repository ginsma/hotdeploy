package example.deploy.xml.bootstrap;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import example.deploy.hotdeploy.client.Major;

public class BootstrapFileWriter {
    private Bootstrap bootstrap;

    private static final Logger logger =
        Logger.getLogger(BootstrapFileWriter.class.getName());

    private static final String XML_HEADER =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<batch xmlns=\"http://www.polopoly.com/polopoly/cm/xmlio\">\n";

    private static final String XML_FOOTER =
        "</batch>\n";

    private static final String SINGLE_CONTENT_XML =
        "  <content updateIfExists=\"false\">\n" +
        "    <metadata>\n" +
        "      <contentid>\n" +
        "        <major>%d</major>\n" +
        "        <externalid>%s</externalid>\n" +
        "      </contentid>\n" +
        "    </metadata>\n" +
        "  </content>\n";

    public BootstrapFileWriter(Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    public void write(Writer out) {
        PrintWriter writer = new PrintWriter(out);

        Formatter formatter = new Formatter(writer);

        writer.write(XML_HEADER);

        for (BootstrapContent content : bootstrap) {
            write(formatter, content);
        }

        writer.write(XML_FOOTER);
    }

    private void write(Formatter formatter, BootstrapContent content) {
        if (content.getMajor() == Major.UNKNOWN) {
            logger.log(Level.WARNING, "Could not write bootstrap XML for content with \"" + content.getExternalId() + "\" since the major was uknown.");
            return;
        }

        formatter.format(SINGLE_CONTENT_XML,
                content.getMajor().getIntegerMajor(),
                content.getExternalId());
    }
}


