package it.gov.pagopa.pu.workflow.wf.classification.assessments.wfclassification;

import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.workflow.wf.classification.assessments.dto.ClassifyAssessmentStartSignalDTO;

/**
 * Workflow interface for the Assessments classification
 * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/2106916894/Classificazione+Accertamenti>Confluence page</a>
 * */

@WorkflowInterface
public interface ClassifyAssessmentsWF {
    String SIGNAL_METHOD_NAME_START_ASSESSMENTS_CLASSIFICATION ="startAssessmentClassification";

    @WorkflowMethod
    void classify();

    @SignalMethod
    void startAssessmentClassification(ClassifyAssessmentStartSignalDTO signalDTO);
}
