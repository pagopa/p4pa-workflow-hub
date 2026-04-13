package it.gov.pagopa.pu.workflow.wf.pagopa.send.create;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.create.wf.SendNotificationProcessWF;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.create.wf.SendNotificationProcessWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class SendNotificationProcessWFClientTest {

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private WorkflowClientService workflowClientServiceMock;
  @Mock
  private SendNotificationProcessWF sendNotificationProcessWFMock;

  private SendNotificationProcessWFClient client;

  @BeforeEach
  void setUp() {
    client = new SendNotificationProcessWFClient(workflowServiceMock, workflowClientServiceMock);
  }

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(workflowServiceMock, workflowClientServiceMock);
  }

  @Test
  void givenSendNotificationIdWhenStartSendNotificationProcessThenOk() {
    // Given
    String sendNotificationId = "sendNotificationId";
    String taskQueue = TaskQueueConstants.TASK_QUEUE_SEND_RESERVED_NOTIFICATION;
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("SendNotificationProcessWF-"+sendNotificationId, "RUNID");

    Mockito.when(workflowServiceMock.buildWorkflowStubToStartNew(SendNotificationProcessWF.class, taskQueue, expectedResult.getWorkflowId()))
      .thenReturn(sendNotificationProcessWFMock);

    TemporalTestUtils.configureWorkflowClientServiceMock(workflowClientServiceMock, expectedResult, sendNotificationId);

    // When
    WorkflowCreatedDTO result = client.startSendNotificationProcess(sendNotificationId);

    // Then
    assertEquals(expectedResult, result);
    verify(sendNotificationProcessWFMock).sendNotificationProcess(sendNotificationId);

    TemporalTestUtils.verifyWorkflowTaskQueueConfiguration(taskQueue, SendNotificationProcessWFImpl.class);
  }

}
