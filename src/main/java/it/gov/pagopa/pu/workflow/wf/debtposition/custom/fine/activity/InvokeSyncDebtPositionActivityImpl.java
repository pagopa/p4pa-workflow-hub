package it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.wfreductionexpiration.FineReductionOptionExpirationWFImpl;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.SynchronizeDebtPositionWfClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ActivityImpl(taskQueues = FineReductionOptionExpirationWFImpl.TASK_QUEUE_SYNCHRONIZE_DP_SYNC_LOCAL_ACTIVITY)
public class InvokeSyncDebtPositionActivityImpl implements InvokeSyncDebtPositionActivity {

  private final SynchronizeDebtPositionWfClient synchronizeDebtPositionWfClient;

  public InvokeSyncDebtPositionActivityImpl(SynchronizeDebtPositionWfClient synchronizeDebtPositionWfClient) {
    this.synchronizeDebtPositionWfClient = synchronizeDebtPositionWfClient;
  }

  @Override
  public String synchronizeDPSync(DebtPositionDTO debtPositionDTO, PaymentEventRequestDTO paymentEventRequest, GenericWfExecutionConfig wfExecutionConfig) {
    return synchronizeDebtPositionWfClient.synchronizeDPSync(debtPositionDTO, paymentEventRequest, wfExecutionConfig);
  }
}
