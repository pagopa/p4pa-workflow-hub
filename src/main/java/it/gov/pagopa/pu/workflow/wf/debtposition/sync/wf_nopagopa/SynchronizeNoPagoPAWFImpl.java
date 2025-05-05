package it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_nopagopa;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.SyncCompleteDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.BaseDPSynchronizeWf;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@WorkflowImpl(taskQueues = SynchronizeNoPagoPAWFImpl.TASK_QUEUE_SYNCHRONIZE_DP_NO_PAGOPA_WF)
public class SynchronizeNoPagoPAWFImpl extends BaseDPSynchronizeWf implements SynchronizeNoPagoPAWF {

  public static final String TASK_QUEUE_SYNCHRONIZE_DP_NO_PAGOPA_WF = "DebtPositionSynchronize_NoPagoPA_WF";

  @Override
  public void synchronizeDPNoPagoPA(DebtPositionDTO debtPosition, PaymentEventRequestDTO paymentEventRequest, GenericWfExecutionConfig wfExecutionConfig) {
    synchronizeDebtPosition(debtPosition, paymentEventRequest, wfExecutionConfig);
  }

  @Override
  protected SyncCompleteDTO synchronizeInstallment(DebtPositionDTO debtPosition, InstallmentDTO installment) {
    // SYNC DebtPosition should not invoke any PagoPA API
    return buildIupdSyncStatusUpdateDTO(installment);
  }

  @Override
  protected void callIONotificationActivity(DebtPositionDTO requestedDebtPosition, Map<String, SyncCompleteDTO> iupdSyncStatusUpdateDTOMap, GenericWfExecutionConfig.IONotificationBaseOpsMessages ioMessages) {
    // Do Nothing
  }
}
