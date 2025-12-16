package it.gov.pagopa.pu.workflow.wf.dataevents.wfingestion;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.workflow.dto.IngestionDataDTO;
import it.gov.pagopa.pu.workflow.event.dataevents.dto.DataEventRequestDTO;

/**
 * Workflow interface for publish data events.
 * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1601503397/Gestione+degli+Eventi>Confluence page</a>
 * */
@WorkflowInterface
public interface PublishIngestionFlowFileWF {
  @WorkflowMethod
  void publishIngestionFlowFileEvent(IngestionDataDTO ingestionDataDTO, DataEventRequestDTO dataEventRequest);
}
