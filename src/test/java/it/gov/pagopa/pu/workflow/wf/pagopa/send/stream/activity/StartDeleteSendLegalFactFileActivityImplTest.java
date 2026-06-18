package it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.activity;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.delete.DeleteSendLegalFactFileWFClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StartDeleteSendLegalFactFileActivityImplTest {
  @Mock
  private DeleteSendLegalFactFileWFClient deleteSendLegalFactFileWFClientMock;

  @InjectMocks
  private StartDeleteSendLegalFactFileActivityImpl startDeleteSendLegalFactFileActivity;

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      deleteSendLegalFactFileWFClientMock
    );
  }

  @Test
  void whenStartDeleteSendLegalFactExpiredFilesThenOk() {
    String sendNotificationId = "sendNotificationId";
    String workflowId = "workflowId";
    WorkflowCreatedDTO workflowCreatedDTO = new WorkflowCreatedDTO();
    workflowCreatedDTO.setWorkflowId(workflowId);
    Mockito.when(deleteSendLegalFactFileWFClientMock.startDeleteSendLegalFactExpiredFiles(sendNotificationId))
      .thenReturn(workflowCreatedDTO);

    Assertions.assertDoesNotThrow(() -> startDeleteSendLegalFactFileActivity.startDeleteSendLegalFactExpiredFiles(sendNotificationId));
  }
}
