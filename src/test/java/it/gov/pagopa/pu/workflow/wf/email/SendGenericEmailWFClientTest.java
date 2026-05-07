package it.gov.pagopa.pu.workflow.wf.email;

import it.gov.pagopa.payhub.activities.dto.email.EmailDTO;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import it.gov.pagopa.pu.workflow.wf.email.wf.SendGenericEmailWF;
import it.gov.pagopa.pu.workflow.wf.email.wf.SendGenericEmailWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SendGenericEmailWFClientTest {

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private WorkflowClientService workflowClientServiceMock;
  @Mock
  private SendGenericEmailWF wfMock;

  private SendGenericEmailWFClient client;

  @BeforeEach
  void init() {
    client = new SendGenericEmailWFClient(workflowServiceMock, workflowClientServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(workflowServiceMock, workflowClientServiceMock);
  }

  @Test
  void whenSendEmailThenOk() {
    // Given
    Long brokerId = 1L;
    EmailDTO emailDTO = new EmailDTO();
    String taskQueue = TaskQueueConstants.TASK_QUEUE_LOW_PRIORITY;
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("SendGenericEmailWF-" + emailDTO.hashCode(), "RUNID");

    Mockito.when(workflowServiceMock.buildWorkflowStubToStartNew(SendGenericEmailWF.class, taskQueue, expectedResult.getWorkflowId()))
      .thenReturn(wfMock);

    TemporalTestUtils.configureWorkflowClientServiceMock(workflowClientServiceMock, expectedResult, emailDTO, brokerId);

    // When
    WorkflowCreatedDTO result = client.sendEmail(emailDTO, brokerId);

    // Then
    Assertions.assertEquals(expectedResult, result);
    Mockito.verify(wfMock).sendGenericEmail(Mockito.same(emailDTO), Mockito.eq(brokerId));

    TemporalTestUtils.verifyWorkflowTaskQueueConfiguration(taskQueue, SendGenericEmailWFImpl.class);
  }
}
