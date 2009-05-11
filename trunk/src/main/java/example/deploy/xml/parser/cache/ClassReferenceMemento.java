package example.deploy.xml.parser.cache;

import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.util.SingleObjectHolder;
import example.deploy.xml.parser.ParseCallback;


public class ClassReferenceMemento extends SingleObjectHolder<String> implements SingleCallMemento {

    private String klass;

    public ClassReferenceMemento(String klass) {
        super(klass);
        this.klass = klass;
    }

    public void replay(DeploymentFile file, SingleCallMemento memento,
            ParseCallback parseCallback) {
        parseCallback.classReferenceFound(file, klass);
    }
}
