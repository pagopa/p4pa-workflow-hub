package it.gov.pagopa.pu.workflow.wf.debtposition.custom.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.service.debtposition.sync.complete.generic.DebtPositionGenericSyncService;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.config.SynchronizeDebtPositionWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ActivityImpl(taskQueues = SynchronizeDebtPositionWfConfig.TASK_QUEUE_SYNCHRONIZE_DP_LOCAL_ACTIVITY)
public class InvokeSyncDebtPositionActivityImpl implements InvokeSyncDebtPositionActivity {

  private final DebtPositionGenericSyncService debtPositionGenericSyncService;

  public InvokeSyncDebtPositionActivityImpl(DebtPositionGenericSyncService debtPositionGenericSyncService) {
    this.debtPositionGenericSyncService = debtPositionGenericSyncService;
  }

  @Override
  public String synchronizeDPSync(DebtPositionDTO debtPositionDTO, PaymentEventRequestDTO paymentEventRequest, boolean massive, GenericWfExecutionConfig wfExecutionConfig, String accessToken) {
    return debtPositionGenericSyncService.invokeWorkflow(debtPositionDTO, paymentEventRequest, massive, wfExecutionConfig, accessToken);
  }
}
