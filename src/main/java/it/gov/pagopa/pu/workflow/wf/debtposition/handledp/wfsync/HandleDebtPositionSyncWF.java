package it.gov.pagopa.pu.workflow.wf.debtposition.handledp.wfsync;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;

/**
 * Workflow interface for creating a Sync Debt Position Workflow
 * */
@WorkflowInterface
public interface HandleDebtPositionSyncWF {

  /**
   * Workflow method for the Sync Debt Position Workflow
   * @param debtPosition the debt position to be created
   * */
  @WorkflowMethod
  void handleDPSync(DebtPositionDTO debtPosition);
}
