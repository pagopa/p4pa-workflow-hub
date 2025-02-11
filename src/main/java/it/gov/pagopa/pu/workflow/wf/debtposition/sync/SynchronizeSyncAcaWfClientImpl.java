package it.gov.pagopa.pu.workflow.wf.debtposition.sync;

import io.temporal.client.WorkflowClient;
import io.temporal.workflow.Functions;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.event.payments.enums.PaymentEventType;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_sync.SynchronizeSyncWF;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_sync.SynchronizeSyncWFImpl;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_sync_aca.SynchronizeSyncAcaWF;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_sync_aca.SynchronizeSyncAcaWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.function.Function;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
public class SynchronizeSyncAcaWfClientImpl implements SynchronizeDebtPositionWfClient {
  private final WorkflowService workflowService;

  public SynchronizeSyncAcaWfClientImpl(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  @Override
  public String synchronizeNoPagoPADPSync(DebtPositionDTO debtPositionDTO, PaymentEventType paymentEventType) {
    return "";
  }

  @Override
  public String synchronizeDPSync(DebtPositionDTO debtPositionDTO, PaymentEventType paymentEventType) {
    log.info("Starting synchronization SYNC DebtPosition WF: {}", debtPositionDTO.getDebtPositionId());
    return startWF(
      paymentEventType,
      debtPositionDTO,
      SynchronizeSyncWFImpl.TASK_QUEUE_SYNCHRONIZE_DP_SYNC_WF,
      wf -> wf::synchronizeDpSync,
      SynchronizeSyncWF.class);
  }

  @Override
  public String synchronizeDPSyncAca(DebtPositionDTO debtPositionDTO, PaymentEventType paymentEventType) {
    log.info("Starting synchronization SYNC+ACA DebtPosition WF: {}", debtPositionDTO.getDebtPositionId());
    return startWF(
      paymentEventType,
      debtPositionDTO,
      SynchronizeSyncAcaWFImpl.TASK_QUEUE_SYNCHRONIZE_DP_SYNC_ACA_WF,
      wf -> wf::synchronizeDPSyncAca,
      SynchronizeSyncAcaWF.class);
  }

  @Override
  public String synchronizeDPSyncGpdPreload(DebtPositionDTO debtPositionDTO, PaymentEventType paymentEventType) {
    return "";
  }

  @Override
  public String synchronizeDPSyncAcaGpdPreload(DebtPositionDTO debtPositionDTO, PaymentEventType paymentEventType) {
    return "";
  }

  @Override
  public String synchronizeDPAsyncGpd(DebtPositionDTO debtPositionDTO, PaymentEventType paymentEventType) {
    return "";
  }

  private <T> String startWF(
    String paymentEventType,
    DebtPositionDTO debtPositionDTO,
    String taskQueue,
    Function<T, Functions.Proc2<DebtPositionDTO, PaymentEventType>> wfMethodCall,
    Class<T> wfInterfaceClass)
  {
    String workflowId = generateWorkflowId(debtPositionDTO.getDebtPositionId(), taskQueue);
    T workflow = workflowService.buildWorkflowStub(
      wfInterfaceClass,
      taskQueue,
      workflowId);
    WorkflowClient.start(wfMethodCall.apply(workflow), paymentEventType, debtPositionDTO);
    return workflowId;
  }
}
