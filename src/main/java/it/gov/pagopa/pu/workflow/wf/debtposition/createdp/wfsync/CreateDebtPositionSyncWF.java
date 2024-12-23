package it.gov.pagopa.pu.workflow.wf.debtposition.createdp.wfsync;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.payhub.activities.dto.debtposition.DebtPositionDTO;

@WorkflowInterface
public interface CreateDebtPositionSyncWF {

  @WorkflowMethod
  void createDPSync(DebtPositionDTO debtPosition);
}
