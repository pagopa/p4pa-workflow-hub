package it.gov.pagopa.pu.workflow.wf.classification.assessments.wfassessments;

import it.gov.pagopa.payhub.activities.activity.classifications.AssessmentsCreationActivity;
import it.gov.pagopa.pu.workflow.wf.classification.assessments.config.CreateAssessmentsWFConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateAssessmentsWFTest {

  @Mock
  private AssessmentsCreationActivity assessmentsCreationActivityMock;

  private CreateAssessmentsWFImpl workflow;

  @BeforeEach
  void setUp() {
    CreateAssessmentsWFConfig configMock = mock(CreateAssessmentsWFConfig.class);
    ApplicationContext applicationContextMock = mock(ApplicationContext.class);
    when(configMock.buildAssessmentsCreationActivityStub()).thenReturn(assessmentsCreationActivityMock);

    when(applicationContextMock.getBean(CreateAssessmentsWFConfig.class)).thenReturn(configMock);

    workflow = new CreateAssessmentsWFImpl();
    workflow.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(assessmentsCreationActivityMock);
  }

  @Test
  void givenValidReceiptIdWhenCreateThenLogAndCreateAssessments() {
    // Given
    Long receiptId = 123L;

    // When
    workflow.create(receiptId);

    // Then
    verify(assessmentsCreationActivityMock).createAssessments(receiptId);
  }

  @Test
  void givenExceptionWhenCreateThenLogError() {
    // Given
    Long receiptId = 123L;
    doThrow(new RuntimeException("Test exception")).when(assessmentsCreationActivityMock).createAssessments(receiptId);

    // When
    assertThrows(RuntimeException.class, () -> workflow.create(receiptId));

    // Then
    verify(assessmentsCreationActivityMock).createAssessments(receiptId);
  }

}
