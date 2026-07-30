package it.gov.pagopa.pu.workflow.wf.pagopa.send.delete;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.delete.wfsendlegalfact.DeleteSendLegalFactFileWF;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.delete.wfsendlegalfact.DeleteSendLegalFactFileWFImpl;
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
class DeleteSendLegalFactFileWFClientTest {

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private WorkflowClientService workflowClientServiceMock;
  @Mock
  private DeleteSendLegalFactFileWF deleteSendLegalFactFileWFMock;

  private DeleteSendLegalFactFileWFClient client;

  @BeforeEach
  void setUp() {
    client = new DeleteSendLegalFactFileWFClient(workflowServiceMock, workflowClientServiceMock);
  }

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(workflowServiceMock, workflowClientServiceMock, deleteSendLegalFactFileWFMock);
  }

  @Test
  void whenStartDeleteSendLegalFactExpiredFilesThenOk() {
    // Given
    String sendNotificationId = "sendNotificationId";
    String taskQueue = TaskQueueConstants.TASK_QUEUE_SEND_MEDIUM_PRIORITY;
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("DeleteSendLegalFactFileWF-"+sendNotificationId, "RUNID");

    Mockito.when(workflowServiceMock.buildWorkflowStubToStartNew(DeleteSendLegalFactFileWF.class, taskQueue, expectedResult.getWorkflowId()))
      .thenReturn(deleteSendLegalFactFileWFMock);
    Mockito.doNothing().when(deleteSendLegalFactFileWFMock).deleteSendLegalFactExpiredFiles(sendNotificationId);

    TemporalTestUtils.configureWorkflowClientServiceMock(workflowClientServiceMock, expectedResult, sendNotificationId);

    // When
    WorkflowCreatedDTO result = client.startDeleteSendLegalFactExpiredFiles(sendNotificationId);

    // Then
    assertEquals(expectedResult, result);

    TemporalTestUtils.verifyWorkflowTaskQueueConfiguration(taskQueue, DeleteSendLegalFactFileWFImpl.class);
  }

}
