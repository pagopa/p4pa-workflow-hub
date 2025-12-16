package it.gov.pagopa.pu.workflow.wf.dataevents.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import it.gov.pagopa.payhub.activities.dto.assessments.AssessmentEventDTO;
import it.gov.pagopa.pu.workflow.dto.ExportDataDTO;
import it.gov.pagopa.pu.workflow.dto.IngestionDataDTO;
import it.gov.pagopa.pu.workflow.event.dataevents.dto.DataEventRequestDTO;

@ActivityInterface
public interface PublishDataEventsActivity {
  @ActivityMethod
  void publishExportFileEventActivity(ExportDataDTO exportDataDTO, DataEventRequestDTO dataEventRequestDTO);
  @ActivityMethod
  void publishIngestionFlowFileEventActivity(IngestionDataDTO ingestionDataDTO, DataEventRequestDTO dataEventRequest);
  @ActivityMethod
  void publishPaymentAssessmentsEventActivity(AssessmentEventDTO assessmentsEventDTO, DataEventRequestDTO dataEventRequest);
}
