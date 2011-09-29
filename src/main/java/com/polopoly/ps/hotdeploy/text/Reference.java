package com.polopoly.ps.hotdeploy.text;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentReference;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.ps.hotdeploy.validation.ValidationContext;
import com.polopoly.ps.hotdeploy.validation.ValidationException;

public interface Reference {

    void validate(ValidationContext context) throws ValidationException;

    void validateTemplate(ValidationContext context) throws ValidationException;

    ContentId resolveId(PolicyCMServer server) throws CMException;

    ContentReference resolveReference(PolicyCMServer server) throws CMException;
}
