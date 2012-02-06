package com.polopoly.ps.hotdeploy.xml.parser.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.ps.hotdeploy.client.Major;
import com.polopoly.ps.hotdeploy.file.DeploymentFile;
import com.polopoly.ps.hotdeploy.text.TextContentSet;
import com.polopoly.ps.hotdeploy.util.SingleObjectHolder;
import com.polopoly.ps.hotdeploy.xml.parser.ParseCallback;
import com.polopoly.ps.hotdeploy.xml.parser.ParseContext;


public class FileParseCallbackMemento extends SingleObjectHolder<List<SingleCallMemento>> implements ParseCallback {
    private static final Logger logger = Logger.getLogger(FileParseCallbackMemento.class.getName());

    private DeploymentFile file;
    private List<SingleCallMemento> mementos;
    private TextContentSet contentSet;

    public FileParseCallbackMemento(DeploymentFile file) {
        super(new ArrayList<SingleCallMemento>());
        mementos = heldObject;
        this.file = file;
    }

    public void classReferenceFound(DeploymentFile foundInFile, String klass) {
        createMemento(foundInFile, new ClassReferenceMemento(klass));
    }

    public void contentFound(ParseContext context, String externalId, Major major, String inputTemplate) {
        createMemento(context.getFile(), new ContentMemento(externalId, major, inputTemplate));
    }

    public void contentReferenceFound(ParseContext context, Major major, String externalId) {
        createMemento(context.getFile(), new ContentReferenceMemento(major, externalId));
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

    public TextContentSet replay(ParseCallback parseCallback) {
        ParseContext context = new ParseContext(file);

        for (SingleCallMemento memento : mementos) {
            memento.replay(context, memento, parseCallback);
        }
        
        return contentSet;
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

	public void setResult(TextContentSet contentSet) {
		this.contentSet = contentSet;
	}
}
