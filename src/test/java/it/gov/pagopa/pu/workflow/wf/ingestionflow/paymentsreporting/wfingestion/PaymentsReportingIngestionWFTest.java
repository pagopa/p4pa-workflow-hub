package it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.wfingestion;

import it.gov.pagopa.payhub.activities.activity.ingestionflow.paymentsreporting.PaymentsReportingIngestionFlowFileActivity;
import it.gov.pagopa.payhub.activities.dto.classifications.PaymentsReportingTransferDTO;
import it.gov.pagopa.payhub.activities.dto.ingestion.paymentsreporting.PaymentsReportingIngestionFlowFileActivityResult;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFTest;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.activity.NotifyPaymentsReportingToIufClassificationActivity;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.config.PaymentsReportingIngestionWfConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.function.Function;

@ExtendWith(MockitoExtension.class)
class PaymentsReportingIngestionWFTest extends BaseIngestionFlowFileWFTest<PaymentsReportingIngestionFlowFileActivityResult> {

  @Mock
  private PaymentsReportingIngestionFlowFileActivity paymentsReportingIngestionFlowFileActivityMock;
  @Mock
  private NotifyPaymentsReportingToIufClassificationActivity notifyPaymentsReportingToIufClassificationActivityMock;

  @Override
  protected Pair<Object, Function<Long, PaymentsReportingIngestionFlowFileActivityResult>> configureIngestionFlowFileProcessorActivityMock(ApplicationContext applicationContextMock) {
    PaymentsReportingIngestionWfConfig paymentsReportingIngestionWfConfigMock = Mockito.mock(PaymentsReportingIngestionWfConfig.class);

    Mockito.doReturn(paymentsReportingIngestionWfConfigMock)
      .when(applicationContextMock)
      .getBean(PaymentsReportingIngestionWfConfig.class);

    Mockito.when(paymentsReportingIngestionWfConfigMock.buildPaymentsReportingIngestionFlowFileActivityStub())
      .thenReturn(paymentsReportingIngestionFlowFileActivityMock);
    Mockito.when(paymentsReportingIngestionWfConfigMock.buildNotifyPaymentsReportingToIufClassificationActivityStub())
      .thenReturn(notifyPaymentsReportingToIufClassificationActivityMock);

    return Pair.of(paymentsReportingIngestionFlowFileActivityMock, paymentsReportingIngestionFlowFileActivityMock::processFile);
  }

  @Override
  protected PaymentsReportingIngestionWFImpl buildWf() {
    return new PaymentsReportingIngestionWFImpl();
  }

  @Override
  protected PaymentsReportingIngestionFlowFileActivityResult buildExpectedIngestionFlowFileResult() {
    long organizationId = 2L;

    PaymentsReportingTransferDTO paymentsReportingTransferDTO = PaymentsReportingTransferDTO.builder()
      .iur("iur-1")
      .iuv("iuv-1")
      .transferIndex(1)
      .orgId(organizationId)
      .paymentOutcomeCode("CODICEESITO")
      .build();
    return PaymentsReportingIngestionFlowFileActivityResult.builder()
        .iuf("iuf-1")
        .organizationId(organizationId)
        .transfers(List.of(paymentsReportingTransferDTO))
        .build();
  }

  @Override
  protected void verifyExtraMocks(long ingestionFlowFileId, PaymentsReportingIngestionFlowFileActivityResult expectedResult) {
    Mockito.verify(notifyPaymentsReportingToIufClassificationActivityMock)
      .signalIufClassificationWithStart(expectedResult.getOrganizationId(), expectedResult.getIuf(), expectedResult.getTransfers());
  }
}
