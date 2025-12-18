package it.gov.pagopa.pu.workflow.wf.debtposition.sync;

import io.temporal.workflow.Functions;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
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

import java.util.Map;

import static it.gov.pagopa.pu.workflow.utils.faker.DebtPositionFaker.buildDebtPositionDTO;

@ExtendWith(MockitoExtension.class)
class SynchronizeDebtPositionWfClientTest {

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private WorkflowClientService workflowClientServiceMock;

  private SynchronizeDebtPositionWfClient client;

  private final Map<Class<?>, Class<?>> wfInterface2impl = Map.of(
    SynchronizeNoPagoPAWF.class, SynchronizeNoPagoPAWFImpl.class,
    SynchronizeSyncWF.class, SynchronizeSyncWFImpl.class,
    SynchronizeSyncAcaWF.class, SynchronizeSyncAcaWFImpl.class,
    SynchronizeSyncGpdPreLoadWF.class, SynchronizeSyncGpdPreLoadWFImpl.class,
    SynchronizeSyncAcaGpdPreLoadWF.class, SynchronizeSyncAcaGpdPreLoadWFImpl.class,
    SynchronizeAsyncGpdWF.class, SynchronizeAsyncGpdWFImpl.class
  );

  @BeforeEach
  void init() {
    client = new SynchronizeDebtPositionWfClientImpl(workflowServiceMock, workflowClientServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(workflowServiceMock, workflowClientServiceMock);
  }

  @Test
  void whenSynchronizeDPNoPagoPAThenInvokeWF() {
    testInvokeWF(
      SynchronizeNoPagoPAWF.class,
      client::synchronizeNoPagoPADP,
      SynchronizeNoPagoPAWF::synchronizeDPNoPagoPA);
  }

  @Test
  void whenSynchronizeDPSyncThenInvokeWF() {
    testInvokeWF(
      SynchronizeSyncWF.class,
      client::synchronizeDPSync,
      SynchronizeSyncWF::synchronizeDPSync);
  }

  @Test
  void whenSynchronizeDPSyncAcaThenInvokeWF() {
    testInvokeWF(
      SynchronizeSyncAcaWF.class,
      client::synchronizeDPSyncAca,
      SynchronizeSyncAcaWF::synchronizeDPSyncAca);
  }

  @Test
  void whenSynchronizeDPSyncGpdPreLoadThenInvokeWF() {
    testInvokeWF(
      SynchronizeSyncGpdPreLoadWF.class,
      client::synchronizeDPSyncGpdPreLoad,
      SynchronizeSyncGpdPreLoadWF::synchronizeDPSyncGpdPreLoad);
  }

  @Test
  void whenSynchronizeDPSyncAcaGpdPreLoadThenInvokeWF() {
    testInvokeWF(
      SynchronizeSyncAcaGpdPreLoadWF.class,
      client::synchronizeDPSyncAcaGpdPreLoad,
      SynchronizeSyncAcaGpdPreLoadWF::synchronizeDPSyncAcaGpdPreLoad);
  }

  @Test
  void whenSynchronizeDPAsyncGpdThenInvokeWF() {
    testInvokeWF(
      SynchronizeAsyncGpdWF.class,
      client::synchronizeDPAsyncGpd,
      SynchronizeAsyncGpdWF::synchronizeDPAsyncGpd);
  }

  private <T> void testInvokeWF(
    Class<T> wfInterfaceClass,
    TriFunction<DebtPositionDTO, PaymentEventRequestDTO, GenericWfExecutionConfig, WorkflowCreatedDTO> clientInvoke,
    Functions.Proc4<T, DebtPositionDTO, PaymentEventRequestDTO, GenericWfExecutionConfig> wfInvokeVerifier) {
    // Given
    DebtPositionDTO debtPosition = buildDebtPositionDTO();
    debtPosition.setDebtPositionId(1L);
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO(wfInterfaceClass.getSimpleName()+"-1", "RUNID");
    PaymentEventRequestDTO paymentEventRequest = new PaymentEventRequestDTO(PaymentEventType.DP_CREATED, "EVENTDESCRIPTION");
    GenericWfExecutionConfig genericWfExecutionConfig = new GenericWfExecutionConfig();
    genericWfExecutionConfig.setIoMessages(new GenericWfExecutionConfig.IONotificationBaseOpsMessages());

    T wf = Mockito.mock(wfInterfaceClass);

    String taskQueue = TaskQueueConstants.TASK_QUEUE_DP_RESERVED_SYNC;
    Mockito.when(workflowServiceMock.buildWorkflowStubToStartNew(
        wfInterfaceClass,
        taskQueue,
        expectedResult.getWorkflowId()))
      .thenReturn(wf);

    TemporalTestUtils.configureWorkflowClientServiceMock(workflowClientServiceMock, expectedResult, debtPosition, paymentEventRequest, genericWfExecutionConfig);

    // When
    WorkflowCreatedDTO result = clientInvoke.apply(debtPosition, paymentEventRequest, genericWfExecutionConfig);

    // Then
    Assertions.assertEquals(expectedResult, result);
    wfInvokeVerifier.apply(Mockito.verify(wf), debtPosition, paymentEventRequest, genericWfExecutionConfig);

    TemporalTestUtils.verifyWorkflowTaskQueueConfiguration(taskQueue, wfInterface2impl.get(wfInterfaceClass));
  }
}
