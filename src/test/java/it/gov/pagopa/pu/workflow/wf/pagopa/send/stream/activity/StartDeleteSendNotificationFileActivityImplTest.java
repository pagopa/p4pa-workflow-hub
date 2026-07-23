package it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.activity;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowExecutionAlreadyStarted;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.delete.DeleteSendNotificationFileWFClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.activity.StartDeleteSendLegalFactFileActivityImplTest.podamFactory;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StartDeleteSendNotificationFileActivityImplTest {
  @Mock
  private DeleteSendNotificationFileWFClient deleteSendNotificationFileWFClientMock;

  @InjectMocks
  private StartDeleteSendNotificationFileActivityImpl startDeleteSendNotificationFileActivity;

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      deleteSendNotificationFileWFClientMock
    );
  }

  @Test
  void whenStartDeleteSendNotificationExpiredFilesThenOk() {
    String sendNotificationId = "sendNotificationId";
    String workflowId = "workflowId";
    WorkflowCreatedDTO workflowCreatedDTO = new WorkflowCreatedDTO();
    workflowCreatedDTO.setWorkflowId(workflowId);
    Mockito.when(deleteSendNotificationFileWFClientMock.startDeleteSendNotificationExpiredFiles(sendNotificationId))
      .thenReturn(workflowCreatedDTO);

    Assertions.assertDoesNotThrow(() -> startDeleteSendNotificationFileActivity.startDeleteSendNotificationExpiredFiles(sendNotificationId));
  }

  @Test
  void givenWorkflowAlreadyStartedWhenStartDeleteSendNotificationExpiredFilesThenDoNothing() {
    String sendNotificationId = "sendNotificationId";
    when(deleteSendNotificationFileWFClientMock.startDeleteSendNotificationExpiredFiles(sendNotificationId))
      .thenThrow(new WorkflowExecutionAlreadyStarted(podamFactory.manufacturePojo(WorkflowExecution.class),"workflowType", new Exception()));

    Assertions.assertDoesNotThrow(() -> startDeleteSendNotificationFileActivity.startDeleteSendNotificationExpiredFiles(sendNotificationId));
  }
}
