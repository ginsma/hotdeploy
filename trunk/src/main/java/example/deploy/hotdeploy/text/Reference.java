package example.deploy.hotdeploy.text;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyCMServer;

public interface Reference {

    void validate(ValidationContext context) throws ValidationException;

    void validateTemplate(ValidationContext context) throws ValidationException;

    ContentId resolve(PolicyCMServer server) throws CMException;
}
