package it.gov.pagopa.pu.workflow.wf.classification.assessments.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import it.gov.pagopa.payhub.activities.dto.assessments.AssessmentEventDTO;
import it.gov.pagopa.pu.workflow.event.dataevents.dto.DataEventRequestDTO;

@ActivityInterface
public interface NotifyPaymentAssessmentsActivity {

  @ActivityMethod
  void notifyPaymentAssessmentsEvent(AssessmentEventDTO assessmentsEventDTO, DataEventRequestDTO dataEventRequest);
}
