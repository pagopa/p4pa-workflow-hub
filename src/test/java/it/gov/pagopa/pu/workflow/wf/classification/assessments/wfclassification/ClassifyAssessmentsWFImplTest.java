package it.gov.pagopa.pu.workflow.wf.classification.assessments.wfclassification;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.assessments.AssessmentsClassificationActivity;
import it.gov.pagopa.payhub.activities.dto.assessments.AssessmentEventDTO;
import it.gov.pagopa.payhub.activities.dto.assessments.AssessmentsClassificationSemanticKeyDTO;
import it.gov.pagopa.pu.workflow.wf.classification.assessments.activity.NotifyAssessmentClassificationActivity;
import it.gov.pagopa.pu.workflow.wf.classification.assessments.config.ClassifyAssessmentsWfConfig;
import it.gov.pagopa.pu.workflow.wf.classification.assessments.dto.ClassifyAssessmentStartSignalDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClassifyAssessmentsWFImplTest {

  @Mock
  private AssessmentsClassificationActivity assessmentsClassificationActivityMock;
  @Mock
  private NotifyAssessmentClassificationActivity notifyAssessmentClassificationActivityMock;

  private ClassifyAssessmentsWFImpl wf;

  @BeforeEach
  void setUp() {
    ClassifyAssessmentsWfConfig classifyAssessmentsWfConfig = mock(ClassifyAssessmentsWfConfig.class);
    ApplicationContext applicationContextMock = mock(ApplicationContext.class);

    when(applicationContextMock.getBean(ClassifyAssessmentsWfConfig.class))
      .thenReturn(classifyAssessmentsWfConfig);
    when(classifyAssessmentsWfConfig.buildAssessmentsClassificationActivityStub())
      .thenReturn(assessmentsClassificationActivityMock);
    when(classifyAssessmentsWfConfig.buildNotifyAssessmentClassificationActivityStub())
      .thenReturn(notifyAssessmentClassificationActivityMock);

    wf = new ClassifyAssessmentsWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      assessmentsClassificationActivityMock,
      notifyAssessmentClassificationActivityMock
    );
  }

  @Test
  void testSignalWithAssessmentEventDTOAndWfExecution() {
    AssessmentEventDTO assessmentEventDTO = new AssessmentEventDTO();
    assessmentEventDTO.setAssessmentId(1L);
    when(assessmentsClassificationActivityMock.classifyAssessment(Mockito.any()))
      .thenReturn(assessmentEventDTO);

    signalClassifyAssessment(1L, "iuv1", "iud1");
    signalClassifyAssessment(2L, "iuv1", "iud1");
    signalClassifyAssessment(2L, "iuv2", "iud2");

    try(MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(Workflow::isEveryHandlerFinished).thenReturn(true);

      wf.classify();

      workflowMock.verify(() -> Workflow.await(Mockito.argThat(Supplier::get)));

      verify(assessmentsClassificationActivityMock)
        .classifyAssessment(new AssessmentsClassificationSemanticKeyDTO(1L, "iuv1", "iud1"));
      verify(assessmentsClassificationActivityMock)
        .classifyAssessment(new AssessmentsClassificationSemanticKeyDTO(2L, "iuv1", "iud1"));
      verify(assessmentsClassificationActivityMock)
        .classifyAssessment(new AssessmentsClassificationSemanticKeyDTO(2L, "iuv2", "iud2"));
      verify(notifyAssessmentClassificationActivityMock, times(3))
        .notifyAssessmentClassificationEvent(any(), any());
    }
  }

  @Test
  void testSignalWithNullAssessmentEventDTOAndWfExecution() {
    signalClassifyAssessment(1L, "iuv1", "iud1");
    signalClassifyAssessment(2L, "iuv1", "iud1");
    signalClassifyAssessment(2L, "iuv2", "iud2");

    try(MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(Workflow::isEveryHandlerFinished).thenReturn(true);

      wf.classify();

      workflowMock.verify(() -> Workflow.await(Mockito.argThat(Supplier::get)));

      verify(assessmentsClassificationActivityMock)
        .classifyAssessment(new AssessmentsClassificationSemanticKeyDTO(1L, "iuv1", "iud1"));
      verify(assessmentsClassificationActivityMock)
        .classifyAssessment(new AssessmentsClassificationSemanticKeyDTO(2L, "iuv1", "iud1"));
      verify(assessmentsClassificationActivityMock)
        .classifyAssessment(new AssessmentsClassificationSemanticKeyDTO(2L, "iuv2", "iud2"));
    }
  }

  private void signalClassifyAssessment(Long organizationId, String iuv, String iud) {
    wf.startAssessmentClassification(new ClassifyAssessmentStartSignalDTO(organizationId, iuv, iud));
  }
}
