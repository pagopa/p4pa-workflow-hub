package it.gov.pagopa.pu.workflow.wf.dataevents.wfassessments;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.payhub.activities.dto.assessments.AssessmentEventDTO;
import it.gov.pagopa.pu.workflow.event.dataevents.dto.DataEventRequestDTO;

/**
 * Workflow interface for publish data events.
 * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1601503397/Gestione+degli+Eventi>Confluence page</a>
 * */
@WorkflowInterface
public interface PublishPaymentAssessmentsEventWF {
  @WorkflowMethod
  void publishPaymentAssessmentsEvent(AssessmentEventDTO assessmentsEventDTO, DataEventRequestDTO dataEventRequest);
}
