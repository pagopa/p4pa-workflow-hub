package it.gov.pagopa.pu.workflow.wf.assessments.wfassessments;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Workflow interface for the Assessment creation
 * */

@WorkflowInterface
public interface CreateAssessmentsWF {

    @WorkflowMethod
    void createAssessment(Long receiptId);
}
