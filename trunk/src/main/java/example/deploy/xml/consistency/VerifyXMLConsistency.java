package example.deploy.xml.consistency;

import example.deploy.hotdeploy.discovery.DefaultDiscoverers;
import example.deploy.xml.present.PresentFileReader;


public class VerifyXMLConsistency {

    public static void main(String[] args) {
        XMLConsistencyVerifier verifier = new XMLConsistencyVerifier(
                DefaultDiscoverers.getDiscoverers());

        new VerifierParameterParser(verifier, args).parse();

        new PresentFileReader(verifier.getRootDirectory(), verifier).read();

        verifier.verify().reportUsingLogging();

        if (verifier.areErrorsFound()) {
            System.exit(1);
        }
    }
}
