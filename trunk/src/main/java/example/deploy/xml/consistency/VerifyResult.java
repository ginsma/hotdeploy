package example.deploy.xml.consistency;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VerifyResult {
    private static final Logger logger =
        Logger.getLogger(VerifyResult.class.getName());

    public Set<String> nonFoundContent;
    public Set<String> nonFoundTemplates;
    public Set<String> nonFoundClasses;
    public Set<String> unusedTemplates;

    public void reportUsingLogging() {
        if (!nonFoundContent.isEmpty()) {
            logger.log(Level.WARNING, "The following content objects " +
                "were referenced before they were declared: " + nonFoundContent + ".");
        }

        if (!nonFoundTemplates.isEmpty()) {
            logger.log(Level.WARNING, "The following templates " +
                "were referenced before they were declared: " + nonFoundTemplates + ".");
        }

        if (!nonFoundClasses.isEmpty()) {
            logger.log(Level.WARNING, "The following policy classes " +
                "did not exist: " + nonFoundClasses + ".");
        }

        if (!unusedTemplates.isEmpty()) {
            logger.log(Level.FINE, "The following templates are defined but never used: " + unusedTemplates);
        }
    }

    public boolean isEverythingOk() {
        return nonFoundContent.isEmpty() && nonFoundTemplates.isEmpty();
    }
}
