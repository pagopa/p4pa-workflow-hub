package it.gov.pagopa.pu.workflow.wf.ingestionflow.debtposition.activity;

import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.wfmassivegeneration.MassiveNoticesGenerationWF;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StartMassiveNoticesGenerationWFActivityImplTest {
  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private WorkflowClientService workflowClientServiceMock;
  @Mock
  private MassiveNoticesGenerationWF massiveNoticesGenerationWFMock;

  private StartMassiveNoticesGenerationWFActivity startMassiveNoticesGenerationWFActivity;

  @BeforeEach
  void setUp() {
    startMassiveNoticesGenerationWFActivity = new StartMassiveNoticesGenerationWFActivityImpl(
      workflowServiceMock,
      workflowClientServiceMock
    );
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(workflowServiceMock, workflowClientServiceMock);
  }

  @Test
  void whenStartMassiveNoticesGenerationWFThenOk() {
    Long ingestionFlowFileId = 1L;

    Mockito.when(workflowServiceMock.buildWorkflowStubToStartNew(
        Mockito.eq(MassiveNoticesGenerationWF.class),
        Mockito.eq(TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY),
        Mockito.anyString()))
      .thenReturn(massiveNoticesGenerationWFMock);

    startMassiveNoticesGenerationWFActivity.startMassiveNoticesGenerationWF(ingestionFlowFileId);

    Mockito.verify(workflowServiceMock).buildWorkflowStubToStartNew(
      Mockito.eq(MassiveNoticesGenerationWF.class),
      Mockito.eq(TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY),
      Mockito.anyString()
    );

    Mockito.verify(workflowClientServiceMock).start(Mockito.any(), Mockito.eq(ingestionFlowFileId));
  }
}
