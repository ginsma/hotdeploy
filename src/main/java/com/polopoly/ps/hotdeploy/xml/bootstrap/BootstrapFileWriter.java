package com.polopoly.ps.hotdeploy.xml.bootstrap;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.ps.hotdeploy.client.Major;


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
        "      </contentid>\n%s" +
        "    </metadata>\n" +
        "  </content>\n";

    private static final String INPUT_TEMPLATE_XML =
        "      <input-template>\n" +
        "        <externalid>%s</externalid>\n" +
        "      </input-template>\n";


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

        StringBuffer inputTemplateXml = new StringBuffer(200);

        // we should include the inpute template wherever possible. if the object has the wrong
        // template the bootstrapped object can't be included in content lists with content list
        // wrappers limiting the types of objects that can be added.
        if (content.getInputTemplate() != null) {
            new Formatter(inputTemplateXml).format(INPUT_TEMPLATE_XML, content.getInputTemplate());
        }

        formatter.format(SINGLE_CONTENT_XML,
                content.getMajor().getIntegerMajor(),
                content.getExternalId(), inputTemplateXml);
    }
}


