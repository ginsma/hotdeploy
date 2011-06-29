package com.polopoly.ps.deploy.text;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentReference;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyCMServer;

public interface Reference {

    void validate(ValidationContext context) throws ValidationException;

    void validateTemplate(ValidationContext context) throws ValidationException;

    ContentId resolveId(PolicyCMServer server) throws CMException;

    ContentReference resolveReference(PolicyCMServer server) throws CMException;
}
