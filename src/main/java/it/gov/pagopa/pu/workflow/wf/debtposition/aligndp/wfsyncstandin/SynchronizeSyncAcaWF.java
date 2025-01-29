package it.gov.pagopa.pu.workflow.wf.debtposition.aligndp.wfsyncstandin;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;

/**
 * Workflow interface for creating an Aca Sync Debt Position Workflow
 * */
@WorkflowInterface
public interface SynchronizeSyncAcaWF {

  /**
   * Workflow method for the Aca Sync Debt Position Workflow
   * @param debtPosition the debt position to be synchronized in Aca
   * */
  @WorkflowMethod
  void synchronizeDPSyncAca(DebtPositionDTO debtPosition);
}
