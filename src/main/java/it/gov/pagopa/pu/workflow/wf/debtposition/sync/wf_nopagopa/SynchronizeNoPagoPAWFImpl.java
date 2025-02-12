package it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_nopagopa;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.IupdSyncStatusUpdateDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.BaseDPSynchronizeWf;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@WorkflowImpl(taskQueues = SynchronizeNoPagoPAWFImpl.TASK_QUEUE_SYNCHRONIZE_DP_NO_PAGOPA_WF)
public class SynchronizeNoPagoPAWFImpl extends BaseDPSynchronizeWf implements SynchronizeNoPagoPAWF {

  public static final String TASK_QUEUE_SYNCHRONIZE_DP_NO_PAGOPA_WF = "SynchronizeDP_NoPagoPA_WF";

  @Override
  public void synchronizeDPNoPagoPA(DebtPositionDTO debtPosition, PaymentEventType paymentEventType) {
    synchronizeDebtPosition(debtPosition, paymentEventType);
  }

  @Override
  protected IupdSyncStatusUpdateDTO synchronizeInstallment(DebtPositionDTO debtPosition, InstallmentDTO installment) {
    // SYNC DebtPosition should not invoke any PagoPA API
    return buildIupdSyncStatusUpdateDTO(installment);
  }

  @Override
  protected void callIONotificationActivity(DebtPositionDTO requestedDebtPosition, Map<String, IupdSyncStatusUpdateDTO> iupdSyncStatusUpdateDTOMap) {
    // Do Nothing
  }
}
