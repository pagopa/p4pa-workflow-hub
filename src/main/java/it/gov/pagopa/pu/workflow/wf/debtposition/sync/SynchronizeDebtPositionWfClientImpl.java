package it.gov.pagopa.pu.workflow.wf.debtposition.sync;

import io.temporal.client.WorkflowClient;
import io.temporal.workflow.Functions;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_async_gpd.SynchronizeAsyncGpdWF;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_async_gpd.SynchronizeAsyncGpdWFImpl;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_nopagopa.SynchronizeNoPagoPAWF;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_nopagopa.SynchronizeNoPagoPAWFImpl;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_sync.SynchronizeSyncWF;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_sync.SynchronizeSyncWFImpl;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_sync_aca.SynchronizeSyncAcaWF;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_sync_aca.SynchronizeSyncAcaWFImpl;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_sync_aca_gpdpreload.SynchronizeSyncAcaGpdPreLoadWF;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_sync_aca_gpdpreload.SynchronizeSyncAcaGpdPreLoadWFImpl;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_sync_gpdpreload.SynchronizeSyncGpdPreLoadWF;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_sync_gpdpreload.SynchronizeSyncGpdPreLoadWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.function.Function;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
public class SynchronizeDebtPositionWfClientImpl implements SynchronizeDebtPositionWfClient {
  private final WorkflowService workflowService;

  public SynchronizeDebtPositionWfClientImpl(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  @Override
  public String synchronizeNoPagoPADP(DebtPositionDTO debtPositionDTO, PaymentEventType paymentEventType, GenericWfExecutionConfig wfExecutionConfig) {
    log.info("Starting synchronization no PagoPA DebtPosition WF: {}", debtPositionDTO.getDebtPositionId());
    return startWF(
      debtPositionDTO,
      paymentEventType,
      wfExecutionConfig,
      SynchronizeNoPagoPAWFImpl.TASK_QUEUE_SYNCHRONIZE_DP_NO_PAGOPA_WF,
      wf -> wf::synchronizeDPNoPagoPA,
      SynchronizeNoPagoPAWF.class);
  }

  @Override
  public String synchronizeDPSync(DebtPositionDTO debtPositionDTO, PaymentEventType paymentEventType, GenericWfExecutionConfig wfExecutionConfig) {
    log.info("Starting synchronization SYNC DebtPosition WF: {}", debtPositionDTO.getDebtPositionId());
    return startWF(
      debtPositionDTO,
      paymentEventType,
      wfExecutionConfig,
      SynchronizeSyncWFImpl.TASK_QUEUE_SYNCHRONIZE_DP_SYNC_WF,
      wf -> wf::synchronizeDPSync,
      SynchronizeSyncWF.class);
  }

  @Override
  public String synchronizeDPSyncAca(DebtPositionDTO debtPositionDTO, PaymentEventType paymentEventType, GenericWfExecutionConfig wfExecutionConfig) {
    log.info("Starting synchronization SYNC+ACA DebtPosition WF: {}", debtPositionDTO.getDebtPositionId());
    return startWF(
      debtPositionDTO,
      paymentEventType,
      wfExecutionConfig,
      SynchronizeSyncAcaWFImpl.TASK_QUEUE_SYNCHRONIZE_DP_SYNC_ACA_WF,
      wf -> wf::synchronizeDPSyncAca,
      SynchronizeSyncAcaWF.class);
  }

  @Override
  public String synchronizeDPSyncGpdPreLoad(DebtPositionDTO debtPositionDTO, PaymentEventType paymentEventType, GenericWfExecutionConfig wfExecutionConfig) {
    log.info("Starting synchronization SYNC+GPD PreLoad DebtPosition WF: {}", debtPositionDTO.getDebtPositionId());
    return startWF(
      debtPositionDTO,
      paymentEventType,
      wfExecutionConfig,
      SynchronizeSyncGpdPreLoadWFImpl.TASK_QUEUE_SYNCHRONIZE_DP_SYNC_GPDPRELOAD_WF,
      wf -> wf::synchronizeDPSyncGpdPreLoad,
      SynchronizeSyncGpdPreLoadWF.class);
  }

  @Override
  public String synchronizeDPSyncAcaGpdPreLoad(DebtPositionDTO debtPositionDTO, PaymentEventType paymentEventType, GenericWfExecutionConfig wfExecutionConfig) {
    log.info("Starting synchronization SYNC+ACA+GPD PreLoad DebtPosition WF: {}", debtPositionDTO.getDebtPositionId());
    return startWF(
      debtPositionDTO,
      paymentEventType,
      wfExecutionConfig,
      SynchronizeSyncAcaGpdPreLoadWFImpl.TASK_QUEUE_SYNCHRONIZE_DP_SYNC_ACA_GPDPRELOAD_WF,
      wf -> wf::synchronizeDPSyncAcaGpdPreLoad,
      SynchronizeSyncAcaGpdPreLoadWF.class);
  }

  @Override
  public String synchronizeDPAsyncGpd(DebtPositionDTO debtPositionDTO, PaymentEventType paymentEventType, GenericWfExecutionConfig wfExecutionConfig) {
    log.info("Starting synchronization GPD DebtPosition WF: {}", debtPositionDTO.getDebtPositionId());
    return startWF(
      debtPositionDTO,
      paymentEventType,
      wfExecutionConfig,
      SynchronizeAsyncGpdWFImpl.TASK_QUEUE_SYNCHRONIZE_DP_ASYNC_GPD_WF,
      wf -> wf::synchronizeDPAsyncGpd,
      SynchronizeAsyncGpdWF.class);
  }

  private <T> String startWF(
    DebtPositionDTO debtPositionDTO,
    PaymentEventType paymentEventType,
    GenericWfExecutionConfig wfExecutionConfig,
    String taskQueue,
    Function<T, Functions.Proc3<DebtPositionDTO, PaymentEventType, GenericWfExecutionConfig>> wfMethodCall,
    Class<T> wfInterfaceClass)
  {
    String workflowId = generateWorkflowId(debtPositionDTO.getDebtPositionId(), taskQueue);
    T workflow = workflowService.buildWorkflowStub(
      wfInterfaceClass,
      taskQueue,
      workflowId);
    WorkflowClient.start(wfMethodCall.apply(workflow), debtPositionDTO, paymentEventType, wfExecutionConfig);
    return workflowId;
  }
}
