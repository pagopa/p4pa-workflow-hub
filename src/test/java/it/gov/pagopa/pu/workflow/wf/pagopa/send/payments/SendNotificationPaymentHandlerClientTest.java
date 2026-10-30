package it.gov.pagopa.pu.workflow.wf.pagopa.send.payments;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.payments.wf.SendNotificationPaymentHandlerWF;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.payments.wf.SendNotificationPaymentHandlerWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendNotificationPaymentHandlerClientTest {

  @Mock
  private WorkflowService workflowServiceMock;

  @Mock
  private WorkflowClientService workflowClientServiceMock;

  @Mock
  private SendNotificationPaymentHandlerWF sendNotificationPaymentHandlerWFMock;

  private SendNotificationPaymentHandlerClient client;

  @BeforeEach
  void setUp() {
    client = new SendNotificationPaymentHandlerClient(
      workflowServiceMock,
      workflowClientServiceMock);
  }

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(
      workflowServiceMock,
      workflowClientServiceMock);
  }

  @Test
  void whenNotifyPaymentThenOk() {
    // Given
    DebtPositionDTO debtPosition = new DebtPositionDTO();
    Long receiptId = 1L;
    String taskQueue = TaskQueueConstants.TASK_QUEUE_SEND_RESERVED_PAYMENT_HANDLER;
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("SendNotificationPaymentHandlerWF-1", "RUNID");

    when(workflowServiceMock.buildWorkflowStubToStartNew(eq(SendNotificationPaymentHandlerWF.class), eq(taskQueue), startsWith("SendNotificationPaymentHandlerWF-")))
      .thenReturn(sendNotificationPaymentHandlerWFMock);

    TemporalTestUtils.configureWorkflowClientServiceMock(workflowClientServiceMock, expectedResult, debtPosition, receiptId);

    // When
    WorkflowCreatedDTO result = client.notifyPayment(debtPosition, receiptId);

    // Then
    assertEquals(expectedResult, result);
    TemporalTestUtils.verifyWorkflowTaskQueueConfiguration(taskQueue, SendNotificationPaymentHandlerWFImpl.class);
  }
}
