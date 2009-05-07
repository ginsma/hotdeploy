package example.deploy.xml.parser.cache;

import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.xml.parser.ParseCallback;

public class TemplateReference extends SingleObjectHolder<String> implements SingleCallMemento {
    private String inputTemplate;

    public TemplateReference(String inputTemplate) {
        super(inputTemplate);

        this.inputTemplate = inputTemplate;
    }

    public void replay(DeploymentFile file, SingleCallMemento memento,
            ParseCallback parseCallback) {
        parseCallback.templateReferenceFound(file, inputTemplate);
    }

}
