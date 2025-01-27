package it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.wfingestion;

import it.gov.pagopa.payhub.activities.activity.ingestionflow.UpdateIngestionFlowStatusActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.email.SendEmailIngestionFlowActivity;
import it.gov.pagopa.payhub.activities.activity.paymentsreporting.PaymentsReportingIngestionFlowFileActivity;
import it.gov.pagopa.payhub.activities.dto.classifications.Transfer2ClassifyDTO;
import it.gov.pagopa.payhub.activities.dto.classifications.TransferSemanticKeyDTO;
import it.gov.pagopa.payhub.activities.dto.paymentsreporting.PaymentsReportingIngestionFlowFileActivityResult;
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

import java.util.Collections;
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
    boolean success = true;


    PaymentsReportingIngestionFlowFileActivityResult result =
      new PaymentsReportingIngestionFlowFileActivityResult(
        List.of(new TransferSemanticKeyDTO(1L, "iuv-1", "iur-1", 1))
        , success, null);

    // TODO P4ADEV-1936 replace fake values with real ones
    // result.getOrganizationId()
    // result.getOutcomeCode()
    // result.getTransfers2classify()

    Long organizationId = 1L;
    String outcomeCode = "CODICEESITO";
    List<Transfer2ClassifyDTO> transfers2classify = null;
      //Collections.singletonList(new Transfer2ClassifyDTO());

    when(paymentsReportingIngestionFlowFileActivityMock.processFile(ingestionFlowFileId))
      .thenReturn(result);

    // When
    wf.ingest(ingestionFlowFileId);

    // Then
    Mockito.verify(updateIngestionFlowStatusActivityMock).updateStatus(ingestionFlowFileId, IngestionFlowFile.StatusEnum.PROCESSING, null, null);
    Mockito.verify(sendEmailIngestionFlowActivityMock).sendEmail(ingestionFlowFileId, success);
    Mockito.verify(updateIngestionFlowStatusActivityMock).updateStatus(ingestionFlowFileId, IngestionFlowFile.StatusEnum.COMPLETED, null, null);
    Mockito.verify(notifyPaymentsReportingToIufClassificationActivityMock).signalIufClassificationWithStart(organizationId, "iuf-1", outcomeCode, transfers2classify);
  }

  @Test
  void givenFailingProcessingConditionWhenIngestThenKo() {
    // Given
    long ingestionFlowFileId = 1L;
    boolean success = false;

    Mockito.when(paymentsReportingIngestionFlowFileActivityMock.processFile(ingestionFlowFileId))
      .thenReturn(new PaymentsReportingIngestionFlowFileActivityResult(Collections.emptyList(), success, null));

    // When
    wf.ingest(ingestionFlowFileId);

    // Then
    Mockito.verify(updateIngestionFlowStatusActivityMock).updateStatus(ingestionFlowFileId, IngestionFlowFile.StatusEnum.PROCESSING, null, null);
    Mockito.verify(sendEmailIngestionFlowActivityMock).sendEmail(ingestionFlowFileId, success);
    Mockito.verify(updateIngestionFlowStatusActivityMock).updateStatus(ingestionFlowFileId, IngestionFlowFile.StatusEnum.ERROR, null, null);
  }
}
