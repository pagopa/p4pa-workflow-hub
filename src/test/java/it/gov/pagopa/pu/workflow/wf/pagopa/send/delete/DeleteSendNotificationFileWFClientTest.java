package it.gov.pagopa.pu.workflow.wf.pagopa.send.delete;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.delete.wf.DeleteSendNotificationFileWF;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.delete.wf.DeleteSendNotificationFileWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class DeleteSendNotificationFileWFClientTest {

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private WorkflowClientService workflowClientServiceMock;
  @Mock
  private DeleteSendNotificationFileWF deleteSendNotificationFileWFMock;

  private DeleteSendNotificationFileWFClient client;

  @BeforeEach
  void setUp() {
    client = new DeleteSendNotificationFileWFClient(workflowServiceMock, workflowClientServiceMock);
  }

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(workflowServiceMock, workflowClientServiceMock, deleteSendNotificationFileWFMock);
  }

  @Test
  void whenStartDeleteSendNotificationExpiredFilesThenOk() {
    // Given
    String sendNotificationId = "sendNotificationId";
    String taskQueue = TaskQueueConstants.TASK_QUEUE_LOW_PRIORITY;
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("DeleteSendNotificationFileWF-"+sendNotificationId, "RUNID");

    Mockito.when(workflowServiceMock.buildWorkflowStubToStartNew(DeleteSendNotificationFileWF.class, taskQueue, expectedResult.getWorkflowId()))
      .thenReturn(deleteSendNotificationFileWFMock);
    Mockito.doNothing().when(deleteSendNotificationFileWFMock).deleteSendNotificationExpiredFiles(sendNotificationId);

    TemporalTestUtils.configureWorkflowClientServiceMock(workflowClientServiceMock, expectedResult, sendNotificationId);

    // When
    WorkflowCreatedDTO result = client.startDeleteSendNotificationExpiredFiles(sendNotificationId);

    // Then
    assertEquals(expectedResult, result);

    TemporalTestUtils.verifyWorkflowTaskQueueConfiguration(taskQueue, DeleteSendNotificationFileWFImpl.class);
  }

}
