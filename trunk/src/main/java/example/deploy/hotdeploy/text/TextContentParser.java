
package example.deploy.hotdeploy.text;

import static com.polopoly.cm.server.ServerNames.CONTENT_ATTRG_SYSTEM;
import static com.polopoly.cm.server.ServerNames.CONTENT_ATTR_NAME;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.polopoly.cm.server.ServerNames;

public class TextContentParser {
    private static final char SEPARATOR_CHAR = ':';

    private static final String ID_PREFIX = "id";
    private static final String INPUT_TEMPLATE_PREFIX = "inputtemplate";
    private static final String NAME_PREFIX = "name";
    private static final String SECURITY_PARENT_PREFIX = "securityparent";
    private static final String COMPONENT_PREFIX = "component";
    private static final String REFERENCE_PREFIX = "ref";
    private static final String LIST_PREFIX = "list";
    private static final String TEMPLATE_PREFIX = "template";

    private BufferedReader reader;

    private TextContentSet parsed = new TextContentSet();
    private TextContent currentContent;

    private String line;

    private int atLine;

    public TextContentParser(InputStream inputStream) throws IOException {
        reader = new BufferedReader(new InputStreamReader(inputStream));
    }

    public TextContentSet parse() throws IOException, ParseException {
        while ((line = reader.readLine()) != null) {
            atLine++;
            parseLine(line);
        }

        return parsed;
    }

    private void parseLine(String line) throws ParseException {
        String[] fields = line.split(""+SEPARATOR_CHAR);

        if (fields.length < 2) {
            if (!line.trim().equals("")) {
                fail("Unrecognized line.");
            }

            return;
        }

        String prefix = fields[0];

        if (prefix.equals(ID_PREFIX)) {
            assertFields(2, fields);
            currentContent = new TextContent();
            currentContent.setId(fields[1]);
            parsed.add(currentContent);

            return;
        }

        if (currentContent == null) {
            fail("Add an \"" + ID_PREFIX + ":\" line first.");
            return;
        }

        if (prefix.equals(INPUT_TEMPLATE_PREFIX)) {
            assertFields(2, fields);

            currentContent.setInputTemplate(new ExternalIdReference(fields[1]));
        }
        else if (prefix.equals(NAME_PREFIX)) {
            assertFields(2, fields);
            // TODO: replace with constants.
            currentContent.setComponent(CONTENT_ATTRG_SYSTEM, CONTENT_ATTR_NAME, fields[1]);
        }
        else if (prefix.equals(SECURITY_PARENT_PREFIX)) {
            assertFields(2, fields);
            currentContent.setSecurityParent(new ExternalIdReference(fields[1]));
        }
        else if (prefix.equals(COMPONENT_PREFIX)) {
            assertFields(4, fields);

            currentContent.setComponent(fields[1], fields[2], fields[3]);
        }
        else if (prefix.equals(REFERENCE_PREFIX)) {
            assertFields(4, fields);

            currentContent.setReference(fields[1], fields[2], new ExternalIdReference(fields[3]));
        }
        else if (prefix.equals(LIST_PREFIX)) {
            String group = null;

            if (fields.length == 2) {
                group = ServerNames.DEPARTMENT_ATTRG_SYSTEM;
            }
            else if (fields.length == 3) {
                group = fields[1];
            }
            else {
                fail("Expected one or two parameters for operation " + fields[0] +
                    " (rather than the provided " + (fields.length-1) + ").");
            }

            currentContent.getList(group).add(new ExternalIdReference(fields[2]));
        }
        else if (prefix.equals(TEMPLATE_PREFIX)) {
            assertFields(2, fields);

            currentContent.setTemplateId(fields[1]);
        }
        else {
            fail("Line should start with " + ID_PREFIX + ", " + INPUT_TEMPLATE_PREFIX + ", " +
                    NAME_PREFIX + ", " + SECURITY_PARENT_PREFIX + ", "+ COMPONENT_PREFIX + ", " +
                    REFERENCE_PREFIX + " or " + LIST_PREFIX + ".");
        }
    }

    private void fail(String message) throws ParseException {
        throw new ParseException(message, line, atLine);
    }

    private void assertFields(int expectedFields, String[] fields) throws ParseException {
        if (fields.length != expectedFields) {
            fail("Expected " + (expectedFields-1) + " parameters for operation " + fields[0] +
                    " (rather than the provided " + (fields.length-1) + ").");
        }

    }

}
