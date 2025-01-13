package it.gov.pagopa.pu.workflow.wf.debtposition.createdp.wfsync;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;

/**
 * Workflow interface for creating a Sync Debt Position Workflow
 * */
@WorkflowInterface
public interface CreateDebtPositionSyncWF {

  /**
   * Workflow method for the Sync Debt Position Workflow
   * @param debtPosition the debt position to be created
   * */
  @WorkflowMethod
  void createDPSync(DebtPositionDTO debtPosition);
}
