package it.gov.pagopa.pu.workflow.wf.ingestionflow.receipt.pagopa.wfingestion;

import it.gov.pagopa.payhub.activities.activity.ingestionflow.UpdateIngestionFlowStatusActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.receipt.ReceiptPagopaIngestionActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.receipt.ReceiptPagopaNotifySilActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.receipt.ReceiptPagopaSendEmailActivity;
import it.gov.pagopa.payhub.activities.dto.ingestion.receipt.ReceiptPagopaIngestionFlowFileResult;
import it.gov.pagopa.payhub.activities.exception.NotRetryableActivityException;
import it.gov.pagopa.pu.debtposition.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFileStatus;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.receipt.pagopa.config.ReceiptPagopaIngestionWfConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ReceiptPagopaIngestionWFImplTest {
  @Mock
  private UpdateIngestionFlowStatusActivity updateIngestionFlowStatusActivityMock;
  @Mock
  private ReceiptPagopaSendEmailActivity receiptPagopaSendEmailActivityMock;
  @Mock
  private ReceiptPagopaIngestionActivity receiptPagopaIngestionActivityMock;
  @Mock
  private ReceiptPagopaNotifySilActivity receiptPagopaNotifySilActivityMock;

  private ReceiptPagopaIngestionWFImpl wf;

  @BeforeEach
  void setUp() {
    ReceiptPagopaIngestionWfConfig receiptPagopaIngestionWfConfigMock = mock(ReceiptPagopaIngestionWfConfig.class);
    ApplicationContext applicationContextMock = mock(ApplicationContext.class);

    Mockito.when(receiptPagopaIngestionWfConfigMock.buildUpdateIngestionFlowStatusActivityStub())
      .thenReturn(updateIngestionFlowStatusActivityMock);
    Mockito.when(receiptPagopaIngestionWfConfigMock.buildReceiptPagopaIngestionActivityStub())
      .thenReturn(receiptPagopaIngestionActivityMock);
    Mockito.when(receiptPagopaIngestionWfConfigMock.buildReceiptPagopaNotifySilActivityStub())
      .thenReturn(receiptPagopaNotifySilActivityMock);
    Mockito.when(receiptPagopaIngestionWfConfigMock.buildReceiptPagopaSendEmailActivityStub())
      .thenReturn(receiptPagopaSendEmailActivityMock);

    Mockito.when(applicationContextMock.getBean(ReceiptPagopaIngestionWfConfig.class))
      .thenReturn(receiptPagopaIngestionWfConfigMock);

    wf = new ReceiptPagopaIngestionWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @Test
  void givenSuccessfulProcessingConditionWhenIngestThenOk(){
    // Given
    long ingestionFlowFileId = 1L;
    ReceiptWithAdditionalNodeDataDTO receiptDTO = new ReceiptWithAdditionalNodeDataDTO();
    InstallmentDTO installmentDTO = new InstallmentDTO();

    ReceiptPagopaIngestionFlowFileResult result = new ReceiptPagopaIngestionFlowFileResult(receiptDTO, installmentDTO);

    Mockito.when(receiptPagopaIngestionActivityMock.processFile(ingestionFlowFileId)).thenReturn(result);

    // When
    Assertions.assertDoesNotThrow(() -> wf.ingest(ingestionFlowFileId));

    // Then
    Mockito.verify(updateIngestionFlowStatusActivityMock, Mockito.times(1))
      .updateStatus(ingestionFlowFileId, IngestionFlowFileStatus.UPLOADED, IngestionFlowFileStatus.PROCESSING, null);
    Mockito.verify(updateIngestionFlowStatusActivityMock, Mockito.times(1))
      .updateStatus(ingestionFlowFileId, IngestionFlowFileStatus.PROCESSING, IngestionFlowFileStatus.COMPLETED, result);
    Mockito.verify(receiptPagopaSendEmailActivityMock, Mockito.times(1))
      .sendEmail(receiptDTO, installmentDTO);
    Mockito.verify(receiptPagopaNotifySilActivityMock, Mockito.times(1))
      .handleNotifySil(receiptDTO, installmentDTO);
  }

  @Test
  void givenUnexpectedExceptionWhenIngestThenKo(){
    // Given
    long ingestionFlowFileId = 1L;

    Mockito.when(receiptPagopaIngestionActivityMock.processFile(ingestionFlowFileId))
      .thenThrow(new NotRetryableActivityException("DUMMY"));

    ReceiptPagopaIngestionFlowFileResult ingestionFlowFileResult = ReceiptPagopaIngestionFlowFileResult.builder()
      .errorDescription("error processing receipt id[1]: DUMMY")
      .build();

    // When
    wf.ingest(ingestionFlowFileId);

    // Then
    Mockito.verify(updateIngestionFlowStatusActivityMock, Mockito.times(1))
      .updateStatus(ingestionFlowFileId, IngestionFlowFileStatus.UPLOADED, IngestionFlowFileStatus.PROCESSING, null);
    Mockito.verify(updateIngestionFlowStatusActivityMock, Mockito.times(1))
      .updateStatus(ingestionFlowFileId, IngestionFlowFileStatus.PROCESSING, IngestionFlowFileStatus.ERROR, ingestionFlowFileResult);
    Mockito.verifyNoInteractions(receiptPagopaNotifySilActivityMock, receiptPagopaSendEmailActivityMock);
  }

  @Test
  void givenUnexpectedExceptionInHandleNotifySilWhenIngestThenOk(){
    // Given
    long ingestionFlowFileId = 1L;
    ReceiptWithAdditionalNodeDataDTO receiptDTO = new ReceiptWithAdditionalNodeDataDTO();
    InstallmentDTO installmentDTO = new InstallmentDTO();

    ReceiptPagopaIngestionFlowFileResult result = new ReceiptPagopaIngestionFlowFileResult(receiptDTO, installmentDTO);

    Mockito.when(receiptPagopaIngestionActivityMock.processFile(ingestionFlowFileId)).thenReturn(result);

    Mockito.doThrow(new NotRetryableActivityException("DUMMY")).when(receiptPagopaNotifySilActivityMock).handleNotifySil(receiptDTO, installmentDTO);

    // When
    Assertions.assertDoesNotThrow(() -> wf.ingest(ingestionFlowFileId));

    // Then
    Mockito.verify(updateIngestionFlowStatusActivityMock, Mockito.times(1))
      .updateStatus(ingestionFlowFileId, IngestionFlowFileStatus.UPLOADED, IngestionFlowFileStatus.PROCESSING, null);
    Mockito.verify(updateIngestionFlowStatusActivityMock, Mockito.times(1))
      .updateStatus(ingestionFlowFileId, IngestionFlowFileStatus.PROCESSING, IngestionFlowFileStatus.COMPLETED, result);
    Mockito.verify(receiptPagopaSendEmailActivityMock, Mockito.times(1))
      .sendEmail(receiptDTO, installmentDTO);
    Mockito.verify(receiptPagopaNotifySilActivityMock, Mockito.times(1))
      .handleNotifySil(receiptDTO, installmentDTO);
  }
}
