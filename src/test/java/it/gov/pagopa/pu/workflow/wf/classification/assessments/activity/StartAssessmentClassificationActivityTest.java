package it.gov.pagopa.pu.workflow.wf.classification.assessments.activity;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import it.gov.pagopa.pu.workflow.wf.classification.assessments.ClassifyAssessmentsWFClient;
import it.gov.pagopa.pu.workflow.wf.classification.assessments.dto.ClassifyAssessmentStartSignalDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StartAssessmentClassificationActivityTest {
  @Mock
  private ClassifyAssessmentsWFClient clientMock;

  private StartAssessmentClassificationActivity startAssessmentClassificationActivity;

  @BeforeEach
  void init() {
    startAssessmentClassificationActivity = new StartAssessmentClassificationActivityImpl(clientMock);
  }

  @Test
  void whenSignalAssessmentClassificationWithStartThenOk() {
    // When
    assertDoesNotThrow(() -> startAssessmentClassificationActivity
      .signalAssessmentClassificationWithStart(1L, "IUV", "IUD"));
    // Then
    ClassifyAssessmentStartSignalDTO expectedSignalDTO = ClassifyAssessmentStartSignalDTO.builder()
      .orgId(1L)
      .iuv("IUV")
      .iud("IUD")
      .build();
    Mockito.verify(clientMock).startAssessmentsClassification(expectedSignalDTO);
  }
}
