package com.polopoly.ps.hotdeploy.deployer;

import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.WorkflowAwareContentPolicy;
import com.polopoly.cm.workflow.WorkflowAction;

public class WorkflowAwarePolicyWrapper {
    private WorkflowAwareContentPolicy workflowPolicy;

    public static WorkflowAwarePolicyWrapper wrap(Policy policy) throws CMException {
        return new WorkflowAwarePolicyWrapper(policy);
    }

    private WorkflowAwarePolicyWrapper(Policy policy) throws CMException {
        for (Object fieldName : policy.getChildPolicyNames()) {
            Policy p = policy.getChildPolicy((String) fieldName);
            if (p instanceof WorkflowAwareContentPolicy) {
                workflowPolicy = (WorkflowAwareContentPolicy) p;
                return;
            }
        }
        throw new CMException("Policy " + policy.getContentId().getContentId().getContentIdString()
                              + " does not have workflow assigned to it");
    }

    public void peformWorkflowAction(String actionName) throws CMException {

        for (WorkflowAction action : workflowPolicy.getWorkflowActions()) {
            if (action.getName().equals(actionName)) {
                workflowPolicy.doWorkflowAction(action);
                return;
            }
        }
        throw new CMException("Could not perform workflow action '" + actionName + "' in state '"
                              + workflowPolicy.getWorkflowState().getName() + "' on "
                              + workflowPolicy.getContentId().getContentId().getContentIdString()
                              + ", possible values: " + join(workflowPolicy.getWorkflowActions()));
    }

    private String join(WorkflowAction[] availableActions) {
        StringBuilder sb = new StringBuilder();
        String delim = "";
        for (WorkflowAction action : availableActions) {
            sb.append(delim);
            sb.append(action.getName());
            delim = ", ";
        }
        return sb.toString();
    }

}
