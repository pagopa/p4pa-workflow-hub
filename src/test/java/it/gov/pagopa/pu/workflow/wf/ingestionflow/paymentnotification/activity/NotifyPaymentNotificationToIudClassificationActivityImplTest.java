package it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentnotification.activity;

import it.gov.pagopa.pu.workflow.wf.classification.iud.IudClassificationWFClient;
import it.gov.pagopa.pu.workflow.wf.classification.iud.dto.IudClassificationNotifyPaymentNotificationSignalDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotifyPaymentNotificationToIudClassificationActivityImplTest {

  @Mock
  private IudClassificationWFClient iudClassificationWFClientMock;

  private NotifyPaymentNotificationToIudClassificationActivity activity;

  @BeforeEach
  void setUp() {
    activity = new NotifyPaymentNotificationToIudClassificationActivityImpl(iudClassificationWFClientMock);
  }

  @Test
  void testSignalPaymentNotificationIudClassificationWithStart() {
    // Given
    Long organizationId = 1L;
    String iud = "iud-123";
    IudClassificationNotifyPaymentNotificationSignalDTO expectedSignalDTO = IudClassificationNotifyPaymentNotificationSignalDTO.builder()
      .organizationId(organizationId)
      .iud(iud)
      .build();

    // When
    activity.signalPaymentNotificationIudClassificationWithStart(organizationId, iud);

    // Then
    verify(iudClassificationWFClientMock).notifyPaymentNotification(expectedSignalDTO);
  }
}
