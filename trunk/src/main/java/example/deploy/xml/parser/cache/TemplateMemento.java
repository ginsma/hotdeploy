package example.deploy.xml.parser.cache;

import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.xml.parser.ParseCallback;

public class TemplateMemento extends SingleObjectHolder<String> implements SingleCallMemento {
    private String inputTemplate;

    public TemplateMemento(String inputTemplate) {
        super(inputTemplate);

        this.inputTemplate = inputTemplate;
    }

    public void replay(DeploymentFile file, SingleCallMemento memento,
            ParseCallback parseCallback) {
        parseCallback.templateFound(file, inputTemplate);
    }

}
