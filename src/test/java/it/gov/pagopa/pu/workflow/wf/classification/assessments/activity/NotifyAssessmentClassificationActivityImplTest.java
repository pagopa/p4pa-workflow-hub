package it.gov.pagopa.pu.workflow.wf.classification.assessments.activity;

import it.gov.pagopa.payhub.activities.dto.assessments.AssessmentEventDTO;
import it.gov.pagopa.pu.workflow.event.dataevents.dto.DataEventRequestDTO;
import it.gov.pagopa.pu.workflow.event.dataevents.producer.DataEventsProducerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
class NotifyAssessmentClassificationActivityImplTest {

  @Mock
  private DataEventsProducerService dataEventsProducerServiceMock;

  private NotifyAssessmentClassificationActivity notifyAssessmentClassificationActivity;

  @BeforeEach
  void setUp() {
    notifyAssessmentClassificationActivity = new NotifyAssessmentClassificationActivityImpl(dataEventsProducerServiceMock);
  }

  @Test
  void whenNotifyAssessmentClassificationEventThenOk() {
    //given
    AssessmentEventDTO assessmentsEventDTO = new AssessmentEventDTO();
    DataEventRequestDTO dataEventRequestDTO = new DataEventRequestDTO();

    doNothing().when(dataEventsProducerServiceMock).notifyPaymentAssessmentsEvent(assessmentsEventDTO, dataEventRequestDTO);
    //then
    assertDoesNotThrow(() -> notifyAssessmentClassificationActivity.notifyAssessmentClassificationEvent(assessmentsEventDTO, dataEventRequestDTO));
  }
}
