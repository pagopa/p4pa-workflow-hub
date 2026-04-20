package it.gov.pagopa.pu.workflow.wf.pagopa.send.stream;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.wf.SendNotificationStreamConsumeWF;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.wf.SendNotificationStreamConsumeWFImpl;
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
class SendNotificationStreamWFClientTest {

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private WorkflowClientService workflowClientServiceMock;
  @Mock
  private SendNotificationStreamConsumeWF sendNotificationStreamConsumeWFMock;

  private SendNotificationStreamWFClient client;

  @BeforeEach
  void setUp() {
    client = new SendNotificationStreamWFClient(workflowServiceMock, workflowClientServiceMock);
  }

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(workflowServiceMock, workflowClientServiceMock);
  }

  @Test
  void givenSendStreamIdWhenStartSendNotificationStreamConsumeThenOk() {
    // Given
    String sendStreamId = "sendStreamId";
    String taskQueue = TaskQueueConstants.TASK_QUEUE_SEND_RESERVED_STREAM;
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("SendNotificationStreamConsumeWF-"+sendStreamId, "RUNID");

    Mockito.when(workflowServiceMock.buildWorkflowStubToStartNew(SendNotificationStreamConsumeWF.class, taskQueue, expectedResult.getWorkflowId()))
      .thenReturn(sendNotificationStreamConsumeWFMock);

    TemporalTestUtils.configureWorkflowClientServiceMock(workflowClientServiceMock, expectedResult, sendStreamId);

    // When
    WorkflowCreatedDTO result = client.startSendNotificationStreamConsume(sendStreamId);

    // Then
    assertEquals(expectedResult, result);
    verify(sendNotificationStreamConsumeWFMock).readSendStream(sendStreamId);

    TemporalTestUtils.verifyWorkflowTaskQueueConfiguration(taskQueue, SendNotificationStreamConsumeWFImpl.class);
  }

}
