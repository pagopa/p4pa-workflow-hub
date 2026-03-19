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

import java.time.LocalDate;

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
    LocalDate scheduleDate = LocalDate.now();

    Mockito.when(workflowServiceMock.buildWorkflowStubScheduled(
        Mockito.eq(DeleteMassiveNoticesFileWF.class),
        Mockito.eq(TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY),
        Mockito.anyString(),
        Mockito.eq(scheduleDate)))
      .thenReturn(deleteMassiveNoticesFileWFMock);

    scheduleMassiveNoticesFileDeletionWFActivity.scheduleFileDeletion(ingestionFlowFileId, scheduleDate);

    Mockito.verify(workflowServiceMock).buildWorkflowStubScheduled(
      Mockito.eq(DeleteMassiveNoticesFileWF.class),
      Mockito.eq(TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY),
      Mockito.anyString(),
      Mockito.eq(scheduleDate)
    );

    Mockito.verify(workflowClientServiceMock).start(Mockito.any(), Mockito.eq(ingestionFlowFileId));
  }
}
