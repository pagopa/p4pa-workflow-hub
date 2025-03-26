package it.gov.pagopa.pu.workflow.wf.classification.assessments.wfassessments;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Workflow interface for the Assessment creation
 * */

@WorkflowInterface
public interface CreateAssessmentsWF {
    String SIGNAL_METHOD_NAME_START_TRANSFER_CLASSIFICATION ="startCreateAssessments";

    @WorkflowMethod
    void create(Long receiptId);
}
