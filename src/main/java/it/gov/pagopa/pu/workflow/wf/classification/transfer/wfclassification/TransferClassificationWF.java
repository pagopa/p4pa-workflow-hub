package it.gov.pagopa.pu.workflow.wf.classification.transfer.wfclassification;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Workflow interface for the Transfer classification
 * */

@WorkflowInterface
public interface TransferClassificationWF {
    /**
     * Workflow method for the Transfer classification
     *
     * @param orgId the unique identifier of the organization
     * @param iuv   the unique identifier of the payment (IUV)
     * @param iur   the identifier of the receipt associated with the payment
     * @param transferIndex  the index of the transfer to be classified
     */
    @WorkflowMethod
    void classify(Long orgId, String iuv, String iur, int transferIndex);
}
