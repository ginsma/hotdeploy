package example.deploy.xml.consistency;

import static example.deploy.xml.consistency.ParameterConstants.DIRECTORY_ARGUMENT;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import example.deploy.hotdeploy.client.ArgumentConsumer;
import example.deploy.hotdeploy.client.ArgumentParser;

public class VerifierParameterParser implements ArgumentConsumer {
    private static final Logger logger =
        Logger.getLogger(VerifierParameterParser.class.getName());

    private XMLConsistencyVerifier verifier;
    private String[] args;

    public VerifierParameterParser(XMLConsistencyVerifier verifier,
            String[] args) {
        this.verifier = verifier;
        this.args = args;
    }

    public void parse() {
        new ArgumentParser(this, args).parse();

        if (verifier.getRootDirectory() == null) {
            System.err.println("The parameter --"+ DIRECTORY_ARGUMENT + " is required.");
            System.exit(1);
        }
    }

    public void argumentFound(String parameter, String value) {
        if (value == null) {
            System.err.println("Parameter " + parameter + " required a value. Provide it using --" + parameter + "=<value>.");
            printParameterHelp();
            System.exit(1);
        }

        if (parameter.equals(DIRECTORY_ARGUMENT)) {
            parseRootDirectory(value);
        }
        else if (parameter.equals("classdir")) {
            parseClassDirectoryNames(value);
        }
        else {
            System.err.println("Unknown parameter " + parameter + ".");
            printParameterHelp();
            System.exit(1);
        }
    }

    private void parseRootDirectory(String directoryName) {
        File xmlDirectory = new File(directoryName);

        if (!xmlDirectory.canRead()) {
            logger.log(Level.WARNING,
                "The directory " + xmlDirectory.getAbsolutePath() + " does not exist.");

            System.exit(1);
        }

        verifier.setRootDirectory(xmlDirectory);
    }

    private void parseClassDirectoryNames(String classDirectoryNames) {
        String[] classDirectoryNamesArray = classDirectoryNames.split(File.pathSeparator);

        for (int i = 0; i < classDirectoryNamesArray.length; i++) {
            File classDirectory = new File(classDirectoryNamesArray[i]);

            if (!classDirectory.canRead()) {
                logger.log(Level.WARNING,
                        "The class directory " + classDirectory.getAbsolutePath() +
                        " (specified as \"" + classDirectoryNamesArray[i] + "\") could not be read.");

                System.exit(1);
            }

            verifier.addClassDirectory(classDirectory);
        }
    }

    public void printParameterHelp() {
        System.err.println();
        System.err.println("Accepted parameters:");
        System.err.println("  --" + DIRECTORY_ARGUMENT + " The directory where the _import_order_ file or the content to import is located.");
        System.err.println("  --classdir The directory where the project classes are built to (optional).");
    }
}
