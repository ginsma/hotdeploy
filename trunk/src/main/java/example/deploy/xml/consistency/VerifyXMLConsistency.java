package example.deploy.xml.consistency;

import example.deploy.hotdeploy.discovery.DefaultDiscoverers;

public class VerifyXMLConsistency {

    public static void main(String[] args) {
        XMLConsistencyVerifier verifier = new XMLConsistencyVerifier(DefaultDiscoverers.getDiscoverers());
        new VerifierParameterParser(verifier, args).parse();

        verifier.verify();

        if (verifier.areErrorsFound()) {
            System.exit(1);
        }
    }
}
