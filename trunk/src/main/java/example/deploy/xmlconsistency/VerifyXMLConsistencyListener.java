package example.deploy.xmlconsistency;

import java.io.File;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Servlet listener that verifies that content XML is consistent and warns in
 * non-existing fields are referenced.
 * @author AndreasE
 */
public class VerifyXMLConsistencyListener implements ServletContextListener {
    public void contextDestroyed(ServletContextEvent event) {

    }

    public void contextInitialized(ServletContextEvent event) {
        final String directory = 
            event.getServletContext().getRealPath("/META-INF/content");
        
        Thread thread = new Thread() {
            @Override
            public void run() {
                // let deployment finish first.
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                }
                
                XMLConsistencyVerifier verifier = 
                    new XMLConsistencyVerifier(new File(directory));
                
                verifier.verify();
            }
        };
        
        thread.run();
    }
}
