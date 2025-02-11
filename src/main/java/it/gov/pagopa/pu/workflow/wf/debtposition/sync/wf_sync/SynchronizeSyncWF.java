package it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_sync;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.event.payments.enums.PaymentEventType;

/**
 * Workflow interface for creating a Sync Debt Position Workflow
 * */
@WorkflowInterface
public interface SynchronizeSyncWF {

  /**
   * Workflow method for the Sync Debt Position Workflow
   * @param debtPosition the debt position to be created
   * */
  @WorkflowMethod
  void synchronizeDpSync(DebtPositionDTO debtPosition, PaymentEventType paymentEventType);
}
