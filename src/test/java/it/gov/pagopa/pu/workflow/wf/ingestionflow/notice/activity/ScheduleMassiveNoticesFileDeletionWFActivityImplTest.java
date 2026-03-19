package it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.activity;

import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.deletemassivenoticesfile.DeleteMassiveNoticesFileWF;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

@ExtendWith(MockitoExtension.class)
class ScheduleMassiveNoticesFileDeletionWFActivityImplTest {
  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private WorkflowClientService workflowClientServiceMock;
  @Mock
  private DeleteMassiveNoticesFileWF deleteMassiveNoticesFileWFMock;

  private ScheduleMassiveNoticesFileDeletionWFActivity scheduleMassiveNoticesFileDeletionWFActivity;

  @BeforeEach
  void setUp() {
    scheduleMassiveNoticesFileDeletionWFActivity = new ScheduleMassiveNoticesFileDeletionWFActivityImpl(workflowServiceMock, workflowClientServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(workflowServiceMock, workflowClientServiceMock);
  }

  @Test
  void whenScheduleFileDeletionThenOk() {
    Long ingestionFlowFileId = 1L;
    Duration retentionDuration = Duration.ofDays(100);

    Mockito.when(workflowServiceMock.buildWorkflowStubDelayed(
        Mockito.eq(DeleteMassiveNoticesFileWF.class),
        Mockito.eq(TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY),
        Mockito.anyString(),
        Mockito.eq(retentionDuration)))
      .thenReturn(deleteMassiveNoticesFileWFMock);

    scheduleMassiveNoticesFileDeletionWFActivity.scheduleMassiveNoticesFileDeletionWF(ingestionFlowFileId, retentionDuration);

    Mockito.verify(workflowServiceMock).buildWorkflowStubDelayed(
      Mockito.eq(DeleteMassiveNoticesFileWF.class),
      Mockito.eq(TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY),
      Mockito.anyString(),
      Mockito.eq(retentionDuration)
    );

    Mockito.verify(workflowClientServiceMock).start(Mockito.any(), Mockito.eq(ingestionFlowFileId));
  }
}
