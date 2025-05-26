package it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentnotification.wfingestion;

import it.gov.pagopa.payhub.activities.activity.ingestionflow.paymentnotification.PaymentNotificationIngestionActivity;
import it.gov.pagopa.payhub.activities.dto.ingestion.paymentnotification.PaymentNotificationIngestionFlowFileResult;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFImpl;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFTest;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentnotification.activity.NotifyPaymentNotificationToIudClassificationActivity;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentnotification.config.PaymentNotificationIngestionWfConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.function.Function;

@ExtendWith(MockitoExtension.class)
class PaymentNotificationIngestionWFTest extends BaseIngestionFlowFileWFTest<PaymentNotificationIngestionFlowFileResult> {

  @Mock
  private PaymentNotificationIngestionActivity paymentNotificationIngestionActivityMock;

  @Mock
  private NotifyPaymentNotificationToIudClassificationActivity notifyPaymentNotificationToIudClassificationActivityMock;

  @Override
  protected Pair<Object, Function<Long, PaymentNotificationIngestionFlowFileResult>> configureIngestionFlowFileProcessorActivityMock(ApplicationContext applicationContextMock) {
    PaymentNotificationIngestionWfConfig paymentNotificationIngestionWfConfigMock = Mockito.mock(PaymentNotificationIngestionWfConfig.class);

    Mockito.doReturn(paymentNotificationIngestionWfConfigMock)
        .when(applicationContextMock)
          .getBean(PaymentNotificationIngestionWfConfig.class);

    Mockito.when(paymentNotificationIngestionWfConfigMock.buildPaymentNotificationIngestionActivityStub())
      .thenReturn(paymentNotificationIngestionActivityMock);
    Mockito.when(paymentNotificationIngestionWfConfigMock.buildNotifyPaymentNotificationToIudClassificationActivityStub())
      .thenReturn(notifyPaymentNotificationToIudClassificationActivityMock);

    return Pair.of(paymentNotificationIngestionActivityMock, paymentNotificationIngestionActivityMock::processFile);
  }

  @Override
  protected BaseIngestionFlowFileWFImpl<PaymentNotificationIngestionFlowFileResult> buildWf() {
    return new PaymentNotificationIngestionWFImpl();
  }

  @Override
  protected PaymentNotificationIngestionFlowFileResult buildExpectedIngestionFlowFileResult() {
    return PaymentNotificationIngestionFlowFileResult.builder()
      .iudList(List.of("iud1"))
      .organizationId(2L)
      .processedRows(10L)
      .totalRows(100L)
      .build();
  }

  @Override
  protected void verifyExtraMocks(long ingestionFlowFileId, PaymentNotificationIngestionFlowFileResult expectedResult) {
    Mockito.verify(notifyPaymentNotificationToIudClassificationActivityMock)
      .signalIudClassificationWithStart(expectedResult.getOrganizationId(), "iud1");
  }
}
