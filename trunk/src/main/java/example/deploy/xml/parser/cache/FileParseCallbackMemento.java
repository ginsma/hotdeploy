package example.deploy.xml.parser.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import example.deploy.hotdeploy.client.Major;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.xml.parser.ParseCallback;

public class FileParseCallbackMemento extends SingleObjectHolder<List<SingleCallMemento>> implements ParseCallback {
    private static final Logger logger = Logger.getLogger(FileParseCallbackMemento.class.getName());

    private DeploymentFile file;
    private List<SingleCallMemento> mementos;

    public FileParseCallbackMemento(DeploymentFile file) {
        super(new ArrayList<SingleCallMemento>());
        mementos = heldObject;
        this.file = file;
    }

    public void classReferenceFound(DeploymentFile foundInFile, String klass) {
        createMemento(foundInFile, new ClassReferenceMemento(klass));
    }

    public void contentFound(DeploymentFile foundInFile, String externalId, Major major, String inputTemplate) {
        createMemento(foundInFile, new ContentMemento(externalId, major, inputTemplate));
    }

    public void contentReferenceFound(DeploymentFile foundInFile, String externalId) {
        createMemento(foundInFile, new ContentReferenceMemento(externalId));
    }

    public void templateFound(DeploymentFile foundInFile, String inputTemplate) {
        createMemento(foundInFile, new TemplateMemento(inputTemplate));
    }

    public void templateReferenceFound(DeploymentFile foundInFile, String inputTemplate) {
        createMemento(foundInFile, new TemplateReference(inputTemplate));
    }

    private void createMemento(DeploymentFile foundInFile,
            SingleCallMemento memento) {
        if (!foundInFile.equals(file)) {
            logger.log(Level.WARNING, "Attempt to log mementos both from " + foundInFile + " and " + file + " in same memento.");
        }
        else {
            mementos.add(memento);
        }
    }

    public void replay(ParseCallback parseCallback) {
        for (SingleCallMemento memento : mementos) {
            memento.replay(file, memento, parseCallback);
        }
    }

    public List<SingleCallMemento> getMementos() {
        return mementos;
    }

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer(100);

        for (SingleCallMemento memento : mementos) {
            if (result.length() > 0) {
                result.append(", ");
            }

            result.append(memento.toString());
        }

        return result.toString();
    }
}
