package it.gov.pagopa.pu.workflow.wf.dataevents.wfexport;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.workflow.dto.ExportDataDTO;
import it.gov.pagopa.pu.workflow.event.dataevents.dto.DataEventRequestDTO;

/**
 * Workflow interface for publish data events.
 * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1601503397/Gestione+degli+Eventi>Confluence page</a>
 * */
@WorkflowInterface
public interface PublishExportFileEventWF {
  @WorkflowMethod
  void publishExportFileEvent(ExportDataDTO exportDataDTO, DataEventRequestDTO dataEventRequestDTO);
}
