package example.deploy.xml.consistency;

import java.io.File;

import example.deploy.xml.present.PresentFileReader;


public class VerifyXMLConsistency {

    public static void main(String[] args) {
        XMLConsistencyVerifier verifier = new XMLConsistencyVerifier();

        VerifierParameterParser parser = new VerifierParameterParser(verifier, args);

        parser.parse();

        for (File xmlDirectory : parser.getXMLDirectories()) {
            new PresentFileReader(xmlDirectory, verifier).read();
        }

        verifier.verify().reportUsingLogging();

        if (verifier.areErrorsFound()) {
            System.exit(1);
        }
    }
}
