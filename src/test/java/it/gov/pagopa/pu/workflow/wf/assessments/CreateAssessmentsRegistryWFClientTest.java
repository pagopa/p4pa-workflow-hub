package it.gov.pagopa.pu.workflow.wf.assessments;

import static org.mockito.Mockito.verify;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import it.gov.pagopa.pu.workflow.wf.assessments.wfassessments.CreateAssessmentsRegistryWF;
import it.gov.pagopa.pu.workflow.wf.assessments.wfassessments.CreateAssessmentsRegistryWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateAssessmentsRegistryWFClientTest {

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private WorkflowClientService workflowClientServiceMock;
  @Mock
  private CreateAssessmentsRegistryWF wfMock;

  private CreateAssessmentsRegistryWFClient client;


  @BeforeEach
  void setUp() {
    client = new CreateAssessmentsRegistryWFClient(workflowServiceMock, workflowClientServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(workflowServiceMock, workflowClientServiceMock);
  }

  @Test
  void whenCreateAssessmentsRegistryThenVerify() {
    String eventId = "123";
    String taskQueue = TaskQueueConstants.TASK_QUEUE_ASSESSMENTS;
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("CreateAssessmentsRegistryWF-123", "RUNID");
    DebtPositionDTO debtPositionDTO = new DebtPositionDTO();
    debtPositionDTO.setDebtPositionId(1L);

    Mockito.when(workflowServiceMock.buildWorkflowStubToStartNew(CreateAssessmentsRegistryWF.class, taskQueue, expectedResult.getWorkflowId()))
      .thenReturn(wfMock);

    TemporalTestUtils.configureWorkflowClientServiceMock(workflowClientServiceMock, expectedResult, debtPositionDTO, null);

    // When
    client.createAssessmentsRegistry(eventId, debtPositionDTO, null);

    // Then
    verify(wfMock).createAssessmentsRegistry(debtPositionDTO, null);

    TemporalTestUtils.verifyWorkflowTaskQueueConfiguration(taskQueue, CreateAssessmentsRegistryWFImpl.class);
  }
}
