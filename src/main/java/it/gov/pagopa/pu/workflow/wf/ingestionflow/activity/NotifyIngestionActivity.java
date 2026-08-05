package it.gov.pagopa.pu.workflow.wf.ingestionflow.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import it.gov.pagopa.pu.workflow.dto.IngestionDataDTO;
import it.gov.pagopa.pu.workflow.event.dataevents.dto.DataEventRequestDTO;

@ActivityInterface
public interface NotifyIngestionActivity {

  @ActivityMethod
  void notifyIngestionEvent(IngestionDataDTO ingestionDataDTO, DataEventRequestDTO dataEventRequest);
}
