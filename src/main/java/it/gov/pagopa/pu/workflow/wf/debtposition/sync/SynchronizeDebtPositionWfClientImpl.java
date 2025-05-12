package it.gov.pagopa.pu.workflow.wf.debtposition.sync;

import io.temporal.workflow.Functions;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
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
  private final WorkflowClientService workflowClientService;

  public SynchronizeDebtPositionWfClientImpl(WorkflowService workflowService, WorkflowClientService workflowClientService) {
    this.workflowService = workflowService;
    this.workflowClientService = workflowClientService;
  }

  @Override
  public WorkflowCreatedDTO synchronizeNoPagoPADP(DebtPositionDTO debtPositionDTO, PaymentEventRequestDTO paymentEventRequest, GenericWfExecutionConfig wfExecutionConfig) {
    log.info("Starting synchronization no PagoPA DebtPosition WF: {}", debtPositionDTO.getDebtPositionId());
    return startWF(
      debtPositionDTO,
      paymentEventRequest,
      wfExecutionConfig,
      SynchronizeNoPagoPAWFImpl.TASK_QUEUE_SYNCHRONIZE_DP_NO_PAGOPA_WF,
      wf -> wf::synchronizeDPNoPagoPA,
      SynchronizeNoPagoPAWF.class);
  }

  @Override
  public WorkflowCreatedDTO synchronizeDPSync(DebtPositionDTO debtPositionDTO, PaymentEventRequestDTO paymentEventRequest, GenericWfExecutionConfig wfExecutionConfig) {
    log.info("Starting synchronization SYNC DebtPosition WF: {}", debtPositionDTO.getDebtPositionId());
    return startWF(
      debtPositionDTO,
      paymentEventRequest,
      wfExecutionConfig,
      SynchronizeSyncWFImpl.TASK_QUEUE_SYNCHRONIZE_DP_SYNC_WF,
      wf -> wf::synchronizeDPSync,
      SynchronizeSyncWF.class);
  }

  @Override
  public WorkflowCreatedDTO synchronizeDPSyncAca(DebtPositionDTO debtPositionDTO, PaymentEventRequestDTO paymentEventRequest, GenericWfExecutionConfig wfExecutionConfig) {
    log.info("Starting synchronization SYNC+ACA DebtPosition WF: {}", debtPositionDTO.getDebtPositionId());
    return startWF(
      debtPositionDTO,
      paymentEventRequest,
      wfExecutionConfig,
      SynchronizeSyncAcaWFImpl.TASK_QUEUE_SYNCHRONIZE_DP_SYNC_ACA_WF,
      wf -> wf::synchronizeDPSyncAca,
      SynchronizeSyncAcaWF.class);
  }

  @Override
  public WorkflowCreatedDTO synchronizeDPSyncGpdPreLoad(DebtPositionDTO debtPositionDTO, PaymentEventRequestDTO paymentEventRequest, GenericWfExecutionConfig wfExecutionConfig) {
    log.info("Starting synchronization SYNC+GPD PreLoad DebtPosition WF: {}", debtPositionDTO.getDebtPositionId());
    return startWF(
      debtPositionDTO,
      paymentEventRequest,
      wfExecutionConfig,
      SynchronizeSyncGpdPreLoadWFImpl.TASK_QUEUE_SYNCHRONIZE_DP_SYNC_GPDPRELOAD_WF,
      wf -> wf::synchronizeDPSyncGpdPreLoad,
      SynchronizeSyncGpdPreLoadWF.class);
  }

  @Override
  public WorkflowCreatedDTO synchronizeDPSyncAcaGpdPreLoad(DebtPositionDTO debtPositionDTO, PaymentEventRequestDTO paymentEventRequest, GenericWfExecutionConfig wfExecutionConfig) {
    log.info("Starting synchronization SYNC+ACA+GPD PreLoad DebtPosition WF: {}", debtPositionDTO.getDebtPositionId());
    return startWF(
      debtPositionDTO,
      paymentEventRequest,
      wfExecutionConfig,
      SynchronizeSyncAcaGpdPreLoadWFImpl.TASK_QUEUE_SYNCHRONIZE_DP_SYNC_ACA_GPDPRELOAD_WF,
      wf -> wf::synchronizeDPSyncAcaGpdPreLoad,
      SynchronizeSyncAcaGpdPreLoadWF.class);
  }

  @Override
  public WorkflowCreatedDTO synchronizeDPAsyncGpd(DebtPositionDTO debtPositionDTO, PaymentEventRequestDTO paymentEventRequest, GenericWfExecutionConfig wfExecutionConfig) {
    log.info("Starting synchronization GPD DebtPosition WF: {}", debtPositionDTO.getDebtPositionId());
    return startWF(
      debtPositionDTO,
      paymentEventRequest,
      wfExecutionConfig,
      SynchronizeAsyncGpdWFImpl.TASK_QUEUE_SYNCHRONIZE_DP_ASYNC_GPD_WF,
      wf -> wf::synchronizeDPAsyncGpd,
      SynchronizeAsyncGpdWF.class);
  }

  private <T> WorkflowCreatedDTO startWF(
    DebtPositionDTO debtPositionDTO,
    PaymentEventRequestDTO paymentEventRequest,
    GenericWfExecutionConfig wfExecutionConfig,
    String taskQueue,
    Function<T, Functions.Proc3<DebtPositionDTO, PaymentEventRequestDTO, GenericWfExecutionConfig>> wfMethodCall,
    Class<T> wfInterfaceClass)
  {
    String workflowId = generateWorkflowId(debtPositionDTO.getDebtPositionId(), wfInterfaceClass);
    T workflow = workflowService.buildWorkflowStub(
      wfInterfaceClass,
      taskQueue,
      workflowId);
    return workflowClientService.start(wfMethodCall.apply(workflow), debtPositionDTO, paymentEventRequest, wfExecutionConfig);
  }
}
