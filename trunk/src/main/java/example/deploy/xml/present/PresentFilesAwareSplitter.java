package example.deploy.xml.present;

public class PresentFilesAwareSplitter implements PresentFilesAware {
    private PresentFilesAware[] delegates;

    public PresentFilesAwareSplitter(PresentFilesAware... delegates) {
        this.delegates = delegates;
    }

    public void presentContent(String externalId) {
        for (PresentFilesAware delegate : delegates) {
            delegate.presentContent(externalId);
        }
    }

    public void presentTemplate(String inputTemplate) {
        for (PresentFilesAware delegate : delegates) {
            delegate.presentTemplate(inputTemplate);
        }
    }

}
