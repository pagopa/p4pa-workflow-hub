package it.gov.pagopa.pu.workflow.wf.debtposition.sync.activity;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.event.payments.producer.PaymentsProducerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PublishPaymentEventActivityTest {

  @Mock
  private PaymentsProducerService eventProduceServiceMock;

  private PublishPaymentEventActivity activity;

  @BeforeEach
  void init(){
    activity = new PublishPaymentEventActivityImpl(eventProduceServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(eventProduceServiceMock);
  }

  @Test
  void whenPublishDebtPositionEventThenInvokeProducer(){
    // Given
    DebtPositionDTO debtPositionDTO = new DebtPositionDTO();
    PaymentEventRequestDTO paymentEventRequest = new PaymentEventRequestDTO(PaymentEventType.DP_CREATED, "EVENTDESCRIPTION");

    // When
    activity.publishDebtPositionEvent(debtPositionDTO, paymentEventRequest);

    // Then
    Mockito.verify(eventProduceServiceMock)
      .notifyDebtPositionPaymentsEvent(Mockito.same(debtPositionDTO), Mockito.same(paymentEventRequest));
  }

  @Test
  void whenPublishDebtPositionErrorEventThenInvokeProducer(){
    // Given
    DebtPositionDTO debtPositionDTO = new DebtPositionDTO();
    PaymentEventRequestDTO paymentEventRequest = new PaymentEventRequestDTO(PaymentEventType.DP_CREATED, "EVENTDESCRIPTION");

    // When
    activity.publishDebtPositionErrorEvent(debtPositionDTO, paymentEventRequest);

    // Then
    Mockito.verify(eventProduceServiceMock)
      .notifyDebtPositionPaymentsEvent(Mockito.same(debtPositionDTO), Mockito.same(paymentEventRequest));
  }
}
