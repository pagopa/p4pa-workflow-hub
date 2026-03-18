package it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.activity;

import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
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

    Assertions.assertDoesNotThrow(() ->
      scheduleMassiveNoticesFileDeletionWFActivity.scheduleFileDeletion(ingestionFlowFileId, scheduleDate)
    );
  }
}
