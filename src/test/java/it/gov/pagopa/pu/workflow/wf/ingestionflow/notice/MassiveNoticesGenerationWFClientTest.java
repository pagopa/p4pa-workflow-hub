package it.gov.pagopa.pu.workflow.wf.ingestionflow.notice;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.wfmassivegeneration.MassiveNoticesGenerationWF;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.wfmassivegeneration.MassiveNoticesGenerationWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MassiveNoticesGenerationWFClientTest {
  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private WorkflowClientService workflowClientServiceMock;
  @Mock
  private MassiveNoticesGenerationWF wfMock;

  private MassiveNoticesGenerationWFClient client;

  @BeforeEach
  void init() {
    client = new MassiveNoticesGenerationWFClient(workflowServiceMock, workflowClientServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(workflowServiceMock, workflowClientServiceMock);
  }

  @Test
  void whenGenerateThenOk() {
    long ingestionFlowFileId = 1L;
    String taskQueue = TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY;
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("MassiveNoticesGenerationWF-1", "RUNID");

    Mockito.when(workflowServiceMock.buildWorkflowStubToStartNew(MassiveNoticesGenerationWF.class, taskQueue, expectedResult.getWorkflowId()))
      .thenReturn(wfMock);

    TemporalTestUtils.configureWorkflowClientServiceMock(workflowClientServiceMock, expectedResult, ingestionFlowFileId);

    WorkflowCreatedDTO result = client.generate(ingestionFlowFileId);

    Assertions.assertEquals(expectedResult, result);
    Mockito.verify(wfMock).generate(ingestionFlowFileId);

    TemporalTestUtils.verifyWorkflowTaskQueueConfiguration(taskQueue, MassiveNoticesGenerationWFImpl.class);
  }
}
