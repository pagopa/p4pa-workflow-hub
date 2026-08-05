package it.gov.pagopa.pu.workflow.wf.exportfile.export.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import it.gov.pagopa.pu.workflow.dto.ExportDataDTO;
import it.gov.pagopa.pu.workflow.event.dataevents.dto.DataEventRequestDTO;

@ActivityInterface
public interface NotifyExportActivity {

  @ActivityMethod
  void notifyExportEvent(ExportDataDTO exportDataDTO, DataEventRequestDTO dataEventRequest);
}
