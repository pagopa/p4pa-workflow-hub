package it.gov.pagopa.pu.workflow.wf.debtposition.sync;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.event.payments.enums.PaymentEventType;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_sync.SynchronizeSyncWF;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_sync.SynchronizeSyncWFImpl;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_sync_aca.SynchronizeSyncAcaWF;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_sync_aca.SynchronizeSyncAcaWFImpl;
import org.apache.logging.log4j.util.TriConsumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.BiFunction;

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

  private <T> void testInvokeWF(
    String taskQueue,
    Class<T> wfInterfaceClass,
    BiFunction<DebtPositionDTO, PaymentEventType, String> clientInvoke,
    TriConsumer<T, DebtPositionDTO, PaymentEventType> wfInvokeVerifier) {
    // Given
    DebtPositionDTO debtPosition = buildDebtPositionDTO();
    debtPosition.setDebtPositionId(1L);
    String expectedWorkflowId = taskQueue+"-1";
    PaymentEventType paymentEventType = PaymentEventType.DP_CREATED;

    T wf = Mockito.mock(wfInterfaceClass);

    Mockito.when(workflowServiceMock.buildWorkflowStub(
        wfInterfaceClass,
        taskQueue,
        expectedWorkflowId))
      .thenReturn(wf);

    // When
    String workflowId = clientInvoke.apply(debtPosition, paymentEventType);

    // Then
    Assertions.assertEquals(expectedWorkflowId, workflowId);
    wfInvokeVerifier.accept(Mockito.verify(wf), debtPosition, paymentEventType);
  }
}
