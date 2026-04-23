package it.gov.pagopa.pu.workflow.wf.ingestionflow.notice;

import io.temporal.workflow.Functions;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.deletemassivenoticesfile.DeleteMassiveNoticesFileWF;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.deletemassivenoticesfile.DeleteMassiveNoticesFileWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

@ExtendWith(MockitoExtension.class)
class DeleteMassiveNoticesFileWFClientTest {

  @Mock
  private WorkflowService workflowServiceMock;

  @Mock
  private WorkflowClientService workflowClientServiceMock;

  @Mock
  private DeleteMassiveNoticesFileWF wfMock;

  private DeleteMassiveNoticesFileWFClient client;

  @BeforeEach
  void init() {
    client = new DeleteMassiveNoticesFileWFClient(workflowServiceMock, workflowClientServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(workflowServiceMock, workflowClientServiceMock);
  }

  @Test
  void whenDeleteThenOk() {
    long ingestionFlowFileId = 1L;
    String taskQueue = TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY;

    String expectedWorkflowId = "DeleteMassiveNoticesFileWF-1";

    Mockito.when(workflowServiceMock.buildWorkflowStubToStartNew(
      DeleteMassiveNoticesFileWF.class,
      taskQueue,
      expectedWorkflowId
    )).thenReturn(wfMock);

    Mockito.doAnswer(invocation -> {
        Functions.Proc1<Long> proc = invocation.getArgument(0);
        proc.apply(ingestionFlowFileId);
        return null;
      }).when(workflowClientServiceMock)
      .start(Mockito.any(), Mockito.eq(ingestionFlowFileId));

    client.delete(ingestionFlowFileId);

    Mockito.verify(wfMock).deleteMassiveNoticesFile(ingestionFlowFileId);

    TemporalTestUtils.verifyWorkflowTaskQueueConfiguration(
      taskQueue,
      DeleteMassiveNoticesFileWFImpl.class
    );
  }

  @Test
  void whenScheduleMassiveNoticesFileDeletionThenOk() {
    Long ingestionFlowFileId = 1L;
    Duration retentionDuration = Duration.ofDays(100);

    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("DeleteMassiveNoticesFileWF-1", "runId");

    Mockito.when(workflowServiceMock.buildWorkflowStubDelayed(
        Mockito.eq(DeleteMassiveNoticesFileWF.class),
        Mockito.eq(TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY),
        Mockito.anyString(),
        Mockito.eq(retentionDuration)))
      .thenReturn(wfMock);

    TemporalTestUtils.configureWorkflowClientServiceMock(workflowClientServiceMock, expectedResult, ingestionFlowFileId);

    WorkflowCreatedDTO result = client.scheduleMassiveNoticesFileDeletion(ingestionFlowFileId, retentionDuration);

    Assertions.assertEquals(expectedResult, result);

    Mockito.verify(workflowClientServiceMock).start(Mockito.any(), Mockito.eq(ingestionFlowFileId));
  }
}
