package it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.wfingestion;

import it.gov.pagopa.payhub.activities.activity.ingestionflow.UpdateIngestionFlowStatusActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.email.SendEmailIngestionFlowActivity;
import it.gov.pagopa.payhub.activities.activity.paymentsreporting.PaymentsReportingIngestionFlowFileActivity;
import it.gov.pagopa.payhub.activities.dto.classifications.PaymentsReportingTransferDTO;
import it.gov.pagopa.payhub.activities.dto.paymentsreporting.PaymentsReportingIngestionFlowFileActivityResult;
import it.gov.pagopa.payhub.activities.exception.NotRetryableActivityException;
import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFile;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.activity.NotifyPaymentsReportingToIufClassificationActivity;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.config.PaymentsReportingIngestionWfConfig;
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
class PaymentsReportingIngestionWFTest {

  @Mock
  private UpdateIngestionFlowStatusActivity updateIngestionFlowStatusActivityMock;
  @Mock
  private PaymentsReportingIngestionFlowFileActivity paymentsReportingIngestionFlowFileActivityMock;
  @Mock
  private SendEmailIngestionFlowActivity sendEmailIngestionFlowActivityMock;

  @Mock
  private NotifyPaymentsReportingToIufClassificationActivity notifyPaymentsReportingToIufClassificationActivityMock;


  private PaymentsReportingIngestionWFImpl wf;

  @BeforeEach
  void init() {
    PaymentsReportingIngestionWfConfig paymentsReportingIngestionWfConfigMock = Mockito.mock(PaymentsReportingIngestionWfConfig.class);
    ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    Mockito.when(paymentsReportingIngestionWfConfigMock.buildUpdateIngestionFlowStatusActivityStub())
      .thenReturn(updateIngestionFlowStatusActivityMock);
    Mockito.when(paymentsReportingIngestionWfConfigMock.buildPaymentsReportingIngestionFlowFileActivityStub())
      .thenReturn(paymentsReportingIngestionFlowFileActivityMock);
    Mockito.when(paymentsReportingIngestionWfConfigMock.buildSendEmailIngestionFlowActivityStub())
      .thenReturn(sendEmailIngestionFlowActivityMock);
    Mockito.when(paymentsReportingIngestionWfConfigMock.buildNotifyPaymentsReportingToIufClassificationActivityStub())
      .thenReturn(notifyPaymentsReportingToIufClassificationActivityMock);

    Mockito.when(applicationContextMock.getBean(PaymentsReportingIngestionWfConfig.class))
      .thenReturn(paymentsReportingIngestionWfConfigMock);

    wf = new PaymentsReportingIngestionWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      updateIngestionFlowStatusActivityMock,
      paymentsReportingIngestionFlowFileActivityMock,
      sendEmailIngestionFlowActivityMock,
      notifyPaymentsReportingToIufClassificationActivityMock);
  }

  @Test
  void givenSuccessfulProcessingConditionWhenIngestThenOk() {
    // Given
    long ingestionFlowFileId = 1L;
    long organizationId = 2L;

    PaymentsReportingTransferDTO paymentsReportingTransferDTO = PaymentsReportingTransferDTO.builder()
      .iur("iur-1")
      .iuv("iuv-1")
      .transferIndex(1)
      .orgId(organizationId)
      .paymentOutcomeCode("CODICEESITO")
      .build();
    PaymentsReportingIngestionFlowFileActivityResult result =
      new PaymentsReportingIngestionFlowFileActivityResult("iuf-1", organizationId,
        List.of(paymentsReportingTransferDTO));

    when(paymentsReportingIngestionFlowFileActivityMock.processFile(ingestionFlowFileId))
      .thenReturn(result);

    // When
    wf.ingest(ingestionFlowFileId);

    // Then
    Mockito.verify(updateIngestionFlowStatusActivityMock)
      .updateStatus(ingestionFlowFileId, IngestionFlowFile.StatusEnum.PROCESSING, null, null);

    Mockito.verify(sendEmailIngestionFlowActivityMock)
      .sendEmail(ingestionFlowFileId, true);
    Mockito.verify(updateIngestionFlowStatusActivityMock)
      .updateStatus(ingestionFlowFileId, IngestionFlowFile.StatusEnum.COMPLETED, null, null);

    Mockito.verify(notifyPaymentsReportingToIufClassificationActivityMock)
      .signalIufClassificationWithStart(organizationId, "iuf-1", List.of(paymentsReportingTransferDTO));
  }

  @Test
  void givenFailingProcessingConditionWhenIngestThenKo() {
    // Given
    long ingestionFlowFileId = 1L;

    Mockito.when(paymentsReportingIngestionFlowFileActivityMock.processFile(ingestionFlowFileId))
      .thenThrow(new NotRetryableActivityException("DUMMY"));

    // When
    wf.ingest(ingestionFlowFileId);

    // Then
    Mockito.verify(updateIngestionFlowStatusActivityMock)
      .updateStatus(ingestionFlowFileId, IngestionFlowFile.StatusEnum.PROCESSING, null, null);
    Mockito.verify(sendEmailIngestionFlowActivityMock)
      .sendEmail(ingestionFlowFileId, false);

    Mockito.verify(updateIngestionFlowStatusActivityMock)
      .updateStatus(ingestionFlowFileId, IngestionFlowFile.StatusEnum.ERROR, "DUMMY", null);
  }
}
