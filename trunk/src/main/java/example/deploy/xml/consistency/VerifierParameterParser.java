package example.deploy.xml.consistency;

import static example.deploy.xml.consistency.ParameterConstants.DIRECTORY_ARGUMENT;
import static example.deploy.xml.consistency.ParameterConstants.WRITE_PRESENT_DIRECTORY_ARGUMENT;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import example.deploy.hotdeploy.DiscovererParameterParser;
import example.deploy.hotdeploy.client.ArgumentParser;

public class VerifierParameterParser extends DiscovererParameterParser {
    private static final Logger logger = Logger
            .getLogger(VerifierParameterParser.class.getName());

    private String[] args;

    private XMLConsistencyVerifier verifier;

    public VerifierParameterParser(XMLConsistencyVerifier verifier,
            String[] args) {
        super(verifier);
        this.verifier = verifier;
        this.args = args;
    }

    public void parse() {
        new ArgumentParser(this, args).parse();
    }

    @Override
    public boolean argumentFound(String parameter, String value) {
        if (super.argumentFound(parameter, value)) {
            return true;
        } else if (parameter.equals("classdir")) {
            if (value == null) {
                valueRequired(parameter);
            }

            parseClassDirectoryNames(value);
        } else if (parameter.equals(WRITE_PRESENT_DIRECTORY_ARGUMENT)) {
            if (value == null) {
                valueRequired(parameter);
            }

            verifier.setWritePresentFilesDirectory(new File(value));
        } else {
            System.err.println("Unknown parameter " + parameter + ".");
            printParameterHelp();
            System.exit(1);
        }

        return true;
    }

    private void parseClassDirectoryNames(String classDirectoryNames) {
        String[] classDirectoryNamesArray = classDirectoryNames
                .split(File.pathSeparator);

        for (int i = 0; i < classDirectoryNamesArray.length; i++) {
            File classDirectory = new File(classDirectoryNamesArray[i]);

            if (!classDirectory.canRead()) {
                logger.log(Level.WARNING, "The class directory "
                        + classDirectory.getAbsolutePath()
                        + " (specified as \"" + classDirectoryNamesArray[i]
                        + "\") could not be read.");

                System.exit(1);
            }

            verifier.addClassDirectory(classDirectory);
        }
    }

    @Override
    public void printParameterHelp() {
        System.err.println();
        System.err.println("Accepted parameters:");
        System.err
                .println("  --"
                        + WRITE_PRESENT_DIRECTORY_ARGUMENT
                        + " The directory to write presentContent and presentTemplates to containing the "
                        + "content found during validation (useful if there are other projects depending on this one).");
        System.err
                .println("  --"
                        + DIRECTORY_ARGUMENT
                        + " The directory where the _import_order_ file or the content to import is located.");
        System.err
                .println("  --classdir The directory where the project classes are built to (optional).");
    }
}
