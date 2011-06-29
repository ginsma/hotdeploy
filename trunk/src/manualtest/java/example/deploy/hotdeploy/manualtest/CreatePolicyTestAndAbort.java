
package com.polopoly.ps.deploy.hotdeploy.manualtest;

import com.polopoly.cm.client.CMException;
import com.polopoly.util.client.PolopolyContext;
import com.polopoly.util.exception.PolicyCreateException;
import com.polopoly.util.policy.PolicyModification;

public abstract class CreatePolicyTestAndAbort<PolicyClass> {
    public CreatePolicyTestAndAbort(PolopolyContext context, String inputTemplate, Class<PolicyClass> policyClass) throws Throwable {
        try {
            PolicyModification<PolicyClass> modification =
                new PolicyModification<PolicyClass>() {
                public void modify(PolicyClass checksums) throws CMException {
                    try {
                        test(checksums);
                    } catch (Exception e) {
                        throw new TestFailureException(e);
                    }

                    throw new AbortSignalException();
                }
            };

            context.createPolicy(1,
                    inputTemplate, null, policyClass,
                    modification);
        } catch (PolicyCreateException e) {
            if (e.getCause() instanceof AbortSignalException) {
                // everything is fine
            }
            else {
                throw e;
            }
        }

    }

    protected abstract void test(PolicyClass policy) throws Exception;
}
