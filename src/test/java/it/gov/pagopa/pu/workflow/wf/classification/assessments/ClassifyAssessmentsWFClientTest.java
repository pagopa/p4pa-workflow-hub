package it.gov.pagopa.pu.workflow.wf.classification.assessments;

import io.temporal.client.WorkflowStub;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowInternalErrorException;
import it.gov.pagopa.pu.workflow.service.organization.OrganizationRetrieverService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import it.gov.pagopa.pu.workflow.wf.classification.assessments.dto.ClassifyAssessmentStartSignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.assessments.wfclassification.ClassifyAssessmentsWF;
import it.gov.pagopa.pu.workflow.wf.classification.assessments.wfclassification.ClassifyAssessmentsWFImpl;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class ClassifyAssessmentsWFClientTest {
  private static final Long ORGANIZATION = 123L;
  private static final String IUV = "01011112222333345";
  private static final String IUD = "testIUD";

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private WorkflowClientService workflowClientServiceMock;
  @Mock
  private WorkflowStub workflowStubMock;
  @Mock
  private OrganizationRetrieverService organizationRetrieverServiceMock;

  private ClassifyAssessmentsWFClient client;
  private final Class<ClassifyAssessmentsWF> wfInterface = ClassifyAssessmentsWF.class;

  @BeforeEach
  void setUp() {
    client = new ClassifyAssessmentsWFClient(workflowServiceMock, workflowClientServiceMock, organizationRetrieverServiceMock);
  }

  @AfterEach
  void tearDown() {
    Mockito.verifyNoMoreInteractions(workflowServiceMock, workflowClientServiceMock, workflowStubMock, organizationRetrieverServiceMock);
  }

  @Test
  void testSignalMethodsExist() {
    TemporalTestUtils.assertSignalMethodExists(wfInterface,
      ClassifyAssessmentsWF.SIGNAL_METHOD_NAME_START_ASSESSMENTS_CLASSIFICATION, ClassifyAssessmentStartSignalDTO.class);
  }

  @Test
  void whenClassifyThenOk() {
    // Given
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO(String.format("%s-%d-%s-%s", "ClassifyAssessmentsWF", ORGANIZATION, IUV, IUD), "RUNID");
    ClassifyAssessmentStartSignalDTO signalDTO = new ClassifyAssessmentStartSignalDTO(ORGANIZATION, IUV, IUD);

    String taskQueue = TaskQueueConstants.TASK_QUEUE_ASSESSMENTS_CLASSIFICATION;
    Mockito.when(organizationRetrieverServiceMock.isClassificationEnabled(ORGANIZATION)).thenReturn(true);
    Mockito.when(workflowServiceMock.buildUntypedWorkflowStub(wfInterface, taskQueue, expectedResult.getWorkflowId()))
      .thenReturn(workflowStubMock);
    Mockito.when(workflowClientServiceMock.signalWithStart(
        same(workflowStubMock),
        eq(ClassifyAssessmentsWF.SIGNAL_METHOD_NAME_START_ASSESSMENTS_CLASSIFICATION),
        argThat(o -> o[0] == signalDTO),
        argThat(o -> o.length == 0)))
      .thenReturn(expectedResult);

    // When
    WorkflowCreatedDTO result = client.startAssessmentsClassification(signalDTO);

    // Then
    assertSame(expectedResult, result);

    TemporalTestUtils.verifyWorkflowTaskQueueConfiguration(taskQueue, ClassifyAssessmentsWFImpl.class);
  }

  @Test
  void givenClassificationDisabledWhenClassifyThenError(){
    // Given
    ClassifyAssessmentStartSignalDTO signalDTO = new ClassifyAssessmentStartSignalDTO(ORGANIZATION, IUV, IUD);

    Mockito.when(organizationRetrieverServiceMock.isClassificationEnabled(ORGANIZATION))
      .thenReturn(false);

    // When Then
    assertThrows(ValidationException.class,
      () -> client.startAssessmentsClassification(signalDTO),
      "Classification disabled for organization " + ORGANIZATION);
  }

  @Test
  void givenGenerateWorkflowIdWhenOrgIdNullThenThrowWorkflowInternalErrorException(){
    testGenerateWorkflowIdWhenNullErrors(null, IUV, IUD);
  }

  @Test
  void givenGenerateWorkflowIuvWhenWorkflowNullThenThrowWorkflowInternalErrorException(){
    testGenerateWorkflowIdWhenNullErrors(ORGANIZATION, null, IUD);
  }

  @Test
  void givenGenerateWorkflowIudWhenWorkflowNullThenThrowWorkflowInternalErrorException(){
    testGenerateWorkflowIdWhenNullErrors(ORGANIZATION, IUV, null);
  }

  private void testGenerateWorkflowIdWhenNullErrors(Long orgId, String iuv, String iud) {
    ClassifyAssessmentStartSignalDTO classifyAssessmentStartSignalDTO = new ClassifyAssessmentStartSignalDTO(orgId, iuv, iud);
    Mockito.when(organizationRetrieverServiceMock.isClassificationEnabled(orgId)).thenReturn(true);
    assertThrows(WorkflowInternalErrorException.class,
      () -> client.startAssessmentsClassification(classifyAssessmentStartSignalDTO),
      "The ID or the workflow must not be null");
  }
}
