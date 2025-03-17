package it.gov.pagopa.pu.workflow.wf.pagopa.send;

import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.wfsendnotification.SendNotificationProcessWF;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.wfsendnotification.SendNotificationProcessWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendNotificationWFClientTest {

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private SendNotificationProcessWF wfMock;

  private SendNotificationWFClient client;

  @BeforeEach
  void setUp() {
    client = new SendNotificationWFClient(workflowServiceMock);
  }

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(workflowServiceMock);
  }

  @Test
  void whenSendNotificationProcessThenOk() {
    // Given
    String sendNotificationId = "sendNotificationId";
    String taskQueue = SendNotificationProcessWFImpl.TASK_QUEUE_SEND_NOTIFICATION_PROCESS;
    String expectedWorkflowId = "SendNotificationProcessWF-1";

    try (MockedStatic<Utilities> utilitiesMockedStatic = mockStatic(Utilities.class)) {
      utilitiesMockedStatic
        .when(() -> Utilities.generateWorkflowId(sendNotificationId, taskQueue))
        .thenReturn(expectedWorkflowId);

      Mockito.when(workflowServiceMock.buildWorkflowStub(SendNotificationProcessWF.class, taskQueue, expectedWorkflowId))
        .thenReturn(wfMock);

      // When
      String workflowId = client.sendNotificationProcess(sendNotificationId);

      // Then
      assertEquals(expectedWorkflowId, workflowId);
      verify(wfMock).sendNotificationProcess(sendNotificationId);
    }
  }
}
