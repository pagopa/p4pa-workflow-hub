package it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.activity;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowExecutionAlreadyStarted;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.utils.TestUtils;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.delete.DeleteSendLegalFactFileWFClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StartDeleteSendLegalFactFileActivityImplTest {
  public static final PodamFactory podamFactory = TestUtils.getPodamFactory();
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
    when(deleteSendLegalFactFileWFClientMock.startDeleteSendLegalFactExpiredFiles(sendNotificationId))
      .thenReturn(workflowCreatedDTO);

    Assertions.assertDoesNotThrow(() -> startDeleteSendLegalFactFileActivity.startDeleteSendLegalFactExpiredFiles(sendNotificationId));
  }

  @Test
  void givenWorkflowAlreadyStartedWhenStartDeleteSendLegalFactExpiredFilesThenDoNothing() {
    String sendNotificationId = "sendNotificationId";
    when(deleteSendLegalFactFileWFClientMock.startDeleteSendLegalFactExpiredFiles(sendNotificationId))
      .thenThrow(new WorkflowExecutionAlreadyStarted(podamFactory.manufacturePojo(WorkflowExecution.class),"workflowType", new Exception()));

    Assertions.assertDoesNotThrow(() -> startDeleteSendLegalFactFileActivity.startDeleteSendLegalFactExpiredFiles(sendNotificationId));
  }
}
