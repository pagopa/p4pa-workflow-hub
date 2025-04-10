package it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentnotification.wfingestion;

import it.gov.pagopa.payhub.activities.activity.ingestionflow.UpdateIngestionFlowStatusActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.email.SendEmailIngestionFlowActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.paymentnotification.PaymentNotificationIngestionActivity;
import it.gov.pagopa.payhub.activities.dto.paymentnotification.PaymentNotificationIngestionFlowFileActivityResult;
import it.gov.pagopa.payhub.activities.exception.NotRetryableActivityException;
import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFileStatus;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentnotification.activity.NotifyPaymentNotificationToIudClassificationActivity;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentnotification.config.PaymentNotificationIngestionWfConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentNotificationIngestionWFTest {

  @Mock
  private UpdateIngestionFlowStatusActivity updateIngestionFlowStatusActivityMock;
  @Mock
  private PaymentNotificationIngestionActivity paymentNotificationIngestionActivityMock;
  @Mock
  private SendEmailIngestionFlowActivity sendEmailIngestionFlowActivityMock;

  @Mock
  private NotifyPaymentNotificationToIudClassificationActivity notifyPaymentNotificationToIudClassificationActivityMock;


  private PaymentNotificationIngestionWFImpl wf;

  @BeforeEach
  void init() {
    PaymentNotificationIngestionWfConfig paymentNotificationIngestionWfConfigMock = Mockito.mock(PaymentNotificationIngestionWfConfig.class);
    ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    Mockito.when(paymentNotificationIngestionWfConfigMock.buildUpdateIngestionFlowStatusActivityStub())
      .thenReturn(updateIngestionFlowStatusActivityMock);
    Mockito.when(paymentNotificationIngestionWfConfigMock.buildPaymentNotificationIngestionActivityStub())
      .thenReturn(paymentNotificationIngestionActivityMock);
    Mockito.when(paymentNotificationIngestionWfConfigMock.buildSendEmailIngestionFlowActivityStub())
      .thenReturn(sendEmailIngestionFlowActivityMock);
    Mockito.when(paymentNotificationIngestionWfConfigMock.buildNotifyPaymentNotificationToIudClassificationActivityStub())
      .thenReturn(notifyPaymentNotificationToIudClassificationActivityMock);

    Mockito.when(applicationContextMock.getBean(PaymentNotificationIngestionWfConfig.class))
      .thenReturn(paymentNotificationIngestionWfConfigMock);

    wf = new PaymentNotificationIngestionWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      updateIngestionFlowStatusActivityMock,
      paymentNotificationIngestionActivityMock,
      sendEmailIngestionFlowActivityMock,
      notifyPaymentNotificationToIudClassificationActivityMock);
  }

  @Test
  void givenSuccessfulProcessingConditionWhenIngestThenOk() {
    // Given
    long ingestionFlowFileId = 1L;
    long organizationId = 2L;

    PaymentNotificationIngestionFlowFileActivityResult result =
      new PaymentNotificationIngestionFlowFileActivityResult(List.of("iud1"),organizationId);

    when(paymentNotificationIngestionActivityMock.processFile(ingestionFlowFileId))
      .thenReturn(result);

    // When
    wf.ingest(ingestionFlowFileId);

    // Then
    Mockito.verify(updateIngestionFlowStatusActivityMock)
      .updateStatus(ingestionFlowFileId, IngestionFlowFileStatus.UPLOADED, IngestionFlowFileStatus.PROCESSING, null, null);

    Mockito.verify(sendEmailIngestionFlowActivityMock)
      .sendEmail(ingestionFlowFileId, true);
    Mockito.verify(updateIngestionFlowStatusActivityMock)
      .updateStatus(ingestionFlowFileId, IngestionFlowFileStatus.PROCESSING, IngestionFlowFileStatus.COMPLETED, null, null);

    Mockito.verify(notifyPaymentNotificationToIudClassificationActivityMock)
      .signalIudClassificationWithStart(organizationId, "iud1");
  }

  @Test
  void givenFailingProcessingConditionWhenIngestThenKo() {
    // Given
    long ingestionFlowFileId = 1L;

    Mockito.when(paymentNotificationIngestionActivityMock.processFile(ingestionFlowFileId))
      .thenThrow(new NotRetryableActivityException("DUMMY"));

    // When
    wf.ingest(ingestionFlowFileId);

    // Then
    Mockito.verify(updateIngestionFlowStatusActivityMock)
      .updateStatus(ingestionFlowFileId, IngestionFlowFileStatus.UPLOADED, IngestionFlowFileStatus.PROCESSING, null, null);
    Mockito.verify(sendEmailIngestionFlowActivityMock)
      .sendEmail(ingestionFlowFileId, false);

    Mockito.verify(updateIngestionFlowStatusActivityMock)
      .updateStatus(ingestionFlowFileId, IngestionFlowFileStatus.PROCESSING, IngestionFlowFileStatus.ERROR, "DUMMY", null);
  }
}
