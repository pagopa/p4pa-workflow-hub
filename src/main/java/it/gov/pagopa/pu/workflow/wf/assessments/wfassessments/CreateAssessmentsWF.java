package it.gov.pagopa.pu.workflow.wf.assessments.wfassessments;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Workflow interface for the Assessment creation
 * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1621491884/Creazione+Accertamenti>Confluence page</a>
 * */

@WorkflowInterface
public interface CreateAssessmentsWF {

    @WorkflowMethod
    void createAssessment(Long receiptId);
}
