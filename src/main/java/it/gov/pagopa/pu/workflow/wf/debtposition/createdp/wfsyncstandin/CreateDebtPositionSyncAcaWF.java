package it.gov.pagopa.pu.workflow.wf.debtposition.createdp.wfsyncstandin;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;

/**
 * Workflow interface for creating an Aca Sync Debt Position Workflow
 * */
@WorkflowInterface
public interface CreateDebtPositionSyncAcaWF {

  /**
   * Workflow method for the Aca Sync Debt Position Workflow
   * @param debtPosition the debt position to be created in Aca
   * */
  @WorkflowMethod
  void createDPSyncAca(DebtPositionDTO debtPosition);
}
