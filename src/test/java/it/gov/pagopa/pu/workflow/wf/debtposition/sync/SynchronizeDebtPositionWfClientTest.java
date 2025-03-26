package it.gov.pagopa.pu.workflow.wf.debtposition.sync;

import io.temporal.workflow.Functions;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
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
import org.apache.commons.lang3.function.TriFunction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static it.gov.pagopa.pu.workflow.utils.faker.DebtPositionFaker.buildDebtPositionDTO;

@ExtendWith(MockitoExtension.class)
class SynchronizeDebtPositionWfClientTest {

  @Mock
  private WorkflowService workflowServiceMock;

  private SynchronizeDebtPositionWfClient client;

  @BeforeEach
  void init() {
    client = new SynchronizeDebtPositionWfClientImpl(workflowServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(workflowServiceMock);
  }

  @Test
  void whenSynchronizeDPNoPagoPAThenInvokeWF() {
    testInvokeWF(
      SynchronizeNoPagoPAWFImpl.TASK_QUEUE_SYNCHRONIZE_DP_NO_PAGOPA_WF,
      SynchronizeNoPagoPAWF.class,
      client::synchronizeNoPagoPADP,
      SynchronizeNoPagoPAWF::synchronizeDPNoPagoPA);
  }

  @Test
  void whenSynchronizeDPSyncThenInvokeWF() {
    testInvokeWF(
      SynchronizeSyncWFImpl.TASK_QUEUE_SYNCHRONIZE_DP_SYNC_WF,
      SynchronizeSyncWF.class,
      client::synchronizeDPSync,
      SynchronizeSyncWF::synchronizeDPSync);
  }

  @Test
  void whenSynchronizeDPSyncAcaThenInvokeWF() {
    testInvokeWF(
      SynchronizeSyncAcaWFImpl.TASK_QUEUE_SYNCHRONIZE_DP_SYNC_ACA_WF,
      SynchronizeSyncAcaWF.class,
      client::synchronizeDPSyncAca,
      SynchronizeSyncAcaWF::synchronizeDPSyncAca);
  }

  @Test
  void whenSynchronizeDPSyncGpdPreLoadThenInvokeWF() {
    testInvokeWF(
      SynchronizeSyncGpdPreLoadWFImpl.TASK_QUEUE_SYNCHRONIZE_DP_SYNC_GPDPRELOAD_WF,
      SynchronizeSyncGpdPreLoadWF.class,
      client::synchronizeDPSyncGpdPreLoad,
      SynchronizeSyncGpdPreLoadWF::synchronizeDPSyncGpdPreLoad);
  }

  @Test
  void whenSynchronizeDPSyncAcaGpdPreLoadThenInvokeWF() {
    testInvokeWF(
      SynchronizeSyncAcaGpdPreLoadWFImpl.TASK_QUEUE_SYNCHRONIZE_DP_SYNC_ACA_GPDPRELOAD_WF,
      SynchronizeSyncAcaGpdPreLoadWF.class,
      client::synchronizeDPSyncAcaGpdPreLoad,
      SynchronizeSyncAcaGpdPreLoadWF::synchronizeDPSyncAcaGpdPreLoad);
  }

  @Test
  void whenSynchronizeDPAsyncGpdThenInvokeWF() {
    testInvokeWF(
      SynchronizeAsyncGpdWFImpl.TASK_QUEUE_SYNCHRONIZE_DP_ASYNC_GPD_WF,
      SynchronizeAsyncGpdWF.class,
      client::synchronizeDPAsyncGpd,
      SynchronizeAsyncGpdWF::synchronizeDPAsyncGpd);
  }

  private <T> void testInvokeWF(
    String taskQueue,
    Class<T> wfInterfaceClass,
    TriFunction<DebtPositionDTO, PaymentEventRequestDTO, GenericWfExecutionConfig, String> clientInvoke,
    Functions.Proc4<T, DebtPositionDTO, PaymentEventRequestDTO, GenericWfExecutionConfig> wfInvokeVerifier) {
    // Given
    DebtPositionDTO debtPosition = buildDebtPositionDTO();
    debtPosition.setDebtPositionId(1L);
    String expectedWorkflowId = taskQueue+"-1";
    PaymentEventRequestDTO paymentEventRequest = new PaymentEventRequestDTO(PaymentEventType.DP_CREATED, "EVENTDESCRIPTION");
    GenericWfExecutionConfig genericWfExecutionConfig = new GenericWfExecutionConfig();
    genericWfExecutionConfig.setIoMessages(new GenericWfExecutionConfig.IONotificationBaseOpsMessages());

    T wf = Mockito.mock(wfInterfaceClass);

    Mockito.when(workflowServiceMock.buildWorkflowStub(
        wfInterfaceClass,
        taskQueue,
        expectedWorkflowId))
      .thenReturn(wf);

    // When
    String workflowId = clientInvoke.apply(debtPosition, paymentEventRequest, genericWfExecutionConfig);

    // Then
    Assertions.assertEquals(expectedWorkflowId, workflowId);
    wfInvokeVerifier.apply(Mockito.verify(wf), debtPosition, paymentEventRequest, genericWfExecutionConfig);
  }
}
