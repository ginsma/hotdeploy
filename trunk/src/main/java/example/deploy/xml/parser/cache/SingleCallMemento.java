package example.deploy.xml.parser.cache;

import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.xml.parser.ParseCallback;


public interface SingleCallMemento {

    void replay(DeploymentFile file, SingleCallMemento memento,
            ParseCallback parseCallback);

}
