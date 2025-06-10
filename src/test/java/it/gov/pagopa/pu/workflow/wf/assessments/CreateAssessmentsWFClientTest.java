package it.gov.pagopa.pu.workflow.wf.assessments;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import it.gov.pagopa.pu.workflow.wf.assessments.wfassessments.CreateAssessmentsWF;
import it.gov.pagopa.pu.workflow.wf.assessments.wfassessments.CreateAssessmentsWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CreateAssessmentsWFClientTest {

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private WorkflowClientService workflowClientServiceMock;
  @Mock
  private CreateAssessmentsWF wfMock;

  private CreateAssessmentsWFClient client;

  @BeforeEach
  void setUp() {
    client = new CreateAssessmentsWFClient(workflowServiceMock, workflowClientServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(workflowServiceMock, workflowClientServiceMock);
  }

  @Test
  void testCreateAssessment() {
    Long receiptId = 123L;
    String taskQueue = TaskQueueConstants.TASK_QUEUE_ASSESSMENTS_RESERVED_CREATION;
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("CreateAssessmentsWF-123", "RUNID");

    Mockito.when(workflowServiceMock.buildWorkflowStub(CreateAssessmentsWF.class, taskQueue, expectedResult.getWorkflowId()))
      .thenReturn(wfMock);

    TemporalTestUtils.configureWorkflowClientServiceMock(workflowClientServiceMock, expectedResult, receiptId);

    // When
    WorkflowCreatedDTO result = client.createAssessments(receiptId);

    // Then
    assertSame(expectedResult, result);
    verify(wfMock).createAssessment(receiptId);

    TemporalTestUtils.verifyWorkflowTaskQueueConfiguration(taskQueue, CreateAssessmentsWFImpl.class);
  }
}
