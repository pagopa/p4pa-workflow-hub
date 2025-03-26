package it.gov.pagopa.pu.workflow.wf.classification.assessments;

import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import it.gov.pagopa.pu.workflow.wf.classification.assessments.wfassessments.CreateAssessmentsWF;
import it.gov.pagopa.pu.workflow.wf.classification.assessments.wfassessments.CreateAssessmentsWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CreateAssessmentsWFClientTest {

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private CreateAssessmentsWF wfMock;

  private CreateAssessmentsWFClient client;

  @BeforeEach
  void setUp() {
    client = new CreateAssessmentsWFClient(workflowServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(workflowServiceMock);
  }

  @Test
  void testCreate() {
    Long receiptId = 123L;
    String taskQueue = CreateAssessmentsWFImpl.TASK_QUEUE_CREATE_ASSESSMENTS_WF;
    String expectedWorkflowId = "CreateAssessmentsWF-123";

    try (MockedStatic<Utilities> utilitiesMockedStatic = mockStatic(Utilities.class)) {
      utilitiesMockedStatic
        .when(() -> Utilities.generateWorkflowId(receiptId, taskQueue))
        .thenReturn(expectedWorkflowId);

      Mockito.when(workflowServiceMock.buildWorkflowStub(CreateAssessmentsWF.class, taskQueue, expectedWorkflowId))
        .thenReturn(wfMock);

      // When
      String workflowId = client.createAssessments(receiptId);

      // Then
      assertEquals(expectedWorkflowId, workflowId);
      verify(wfMock).create(receiptId);
    }
  }
}
