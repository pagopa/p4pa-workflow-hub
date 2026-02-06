package it.gov.pagopa.pu.workflow.wf.pagopa.send;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.wfretrievedt.SendNotificationDateRetrieveWF;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.wfretrievedt.SendNotificationDateRetrieveWFImpl;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.wfsendnotification.SendNotificationProcessWF;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.wfsendnotification.SendNotificationProcessWFImpl;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.wfsendnotification.SendNotificationStreamConsumeWF;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.wfsendnotification.SendNotificationStreamConsumeWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class SendNotificationWFClientTest {

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private WorkflowClientService workflowClientServiceMock;
  @Mock
  private SendNotificationProcessWF sendNotificationProcessWFMock;
  @Mock
  private SendNotificationDateRetrieveWF sendNotificationDateRetrieveWFMock;
  @Mock
  private SendNotificationStreamConsumeWF sendNotificationStreamConsumeWFMock;

  private SendNotificationWFClient client;

  @BeforeEach
  void setUp() {
    client = new SendNotificationWFClient(workflowServiceMock, workflowClientServiceMock);
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

  @Test
  void givenSendNotificationIdWhenStartSendNotificationRetrieveNotificationDateThenOk() {
    // Given
    String sendNotificationId = "sendNotificationId";
    String taskQueue = TaskQueueConstants.TASK_QUEUE_SEND_LOW_PRIORITY;
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("SendNotificationDateRetrieveWF-"+sendNotificationId, "RUNID");

    Mockito.when(workflowServiceMock.buildWorkflowStubToStartNew(SendNotificationDateRetrieveWF.class, taskQueue, expectedResult.getWorkflowId()))
      .thenReturn(sendNotificationDateRetrieveWFMock);

    TemporalTestUtils.configureWorkflowClientServiceMock(workflowClientServiceMock, expectedResult, sendNotificationId);

    // When
    WorkflowCreatedDTO result = client.startSendNotificationDateRetrieve(sendNotificationId);

    // Then
    assertEquals(expectedResult, result);
    verify(sendNotificationDateRetrieveWFMock).sendNotificationDateRetrieve(sendNotificationId);

    TemporalTestUtils.verifyWorkflowTaskQueueConfiguration(taskQueue, SendNotificationDateRetrieveWFImpl.class);
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

  @Test
  void givenSendNotificationIdWhenScheduleSendNotificationDateRetrieveThenOk() {
    // Given
    String sendNotificationId = "sendNotificationId";
    String taskQueue = TaskQueueConstants.TASK_QUEUE_SEND_LOW_PRIORITY;
    String expectedWorkflowId = "SendNotificationDateRetrieveWF-"+sendNotificationId;
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO(expectedWorkflowId, "runId");

    Mockito.when(workflowServiceMock.buildWorkflowStubDelayed(SendNotificationDateRetrieveWF.class, taskQueue, expectedWorkflowId, Duration.ofHours(1)))
      .thenReturn(sendNotificationDateRetrieveWFMock);

    TemporalTestUtils.configureWorkflowClientServiceMock(workflowClientServiceMock, expectedResult, sendNotificationId);

    // When
    client.scheduleSendNotificationDateRetrieve(sendNotificationId, Duration.ofHours(1));

    // Then
    verify(sendNotificationDateRetrieveWFMock).sendNotificationDateRetrieve(sendNotificationId);

    TemporalTestUtils.verifyWorkflowTaskQueueConfiguration(taskQueue, SendNotificationDateRetrieveWFImpl.class);
  }
}
