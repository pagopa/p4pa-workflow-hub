package it.gov.pagopa.pu.workflow.wf.classification.assessments.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import it.gov.pagopa.payhub.activities.dto.assessments.AssessmentEventDTO;
import it.gov.pagopa.pu.workflow.event.dataevents.dto.DataEventRequestDTO;

@ActivityInterface
public interface NotifyAssessmentClassificationActivity {

  @ActivityMethod
  void notifyAssessmentClassificationEvent(AssessmentEventDTO assessmentsEventDTO, DataEventRequestDTO dataEventRequest);
}
