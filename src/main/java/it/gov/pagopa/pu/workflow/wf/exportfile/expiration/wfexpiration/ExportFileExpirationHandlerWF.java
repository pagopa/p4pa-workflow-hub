package it.gov.pagopa.pu.workflow.wf.exportfile.expiration.wfexpiration;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Workflow interface for the Export File Expiration Handler
 * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1472069667/Scadenza+di+un+export+di+dati>Confluence page</a>
 * */
@WorkflowInterface
public interface ExportFileExpirationHandlerWF {

  @WorkflowMethod
  void exportFileExpirationHandler(Long exportFileId);
}
