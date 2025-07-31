package it.gov.pagopa.pu.workflow.wf.ingestionflow.receipt.pagopa.wfingestion;

import it.gov.pagopa.payhub.activities.activity.ingestionflow.UpdateIngestionFlowStatusActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.receipt.ReceiptPagopaIngestionActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.receipt.ReceiptPagopaNotifySilActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.receipt.ReceiptPagopaSendEmailActivity;
import it.gov.pagopa.payhub.activities.dto.ingestion.IngestionFlowFileResult;
import it.gov.pagopa.payhub.activities.dto.ingestion.receipt.ReceiptPagopaIngestionFlowFileResult;
import it.gov.pagopa.payhub.activities.exception.NotRetryableActivityException;
import it.gov.pagopa.pu.debtposition.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFileStatus;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.config.BaseIngestionFlowFileWFConfig;
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
    BaseIngestionFlowFileWFConfig baseIngestionFlowFileWFConfigMock = mock(BaseIngestionFlowFileWFConfig.class);
    ReceiptPagopaIngestionWfConfig receiptPagopaIngestionWfConfigMock = mock(ReceiptPagopaIngestionWfConfig.class);
    ApplicationContext applicationContextMock = mock(ApplicationContext.class);

    Mockito.doReturn(baseIngestionFlowFileWFConfigMock)
      .when(applicationContextMock)
      .getBean(BaseIngestionFlowFileWFConfig.class);

    Mockito.doReturn(receiptPagopaIngestionWfConfigMock)
      .when(applicationContextMock)
      .getBean(ReceiptPagopaIngestionWfConfig.class);

    Mockito.when(receiptPagopaIngestionWfConfigMock.buildReceiptPagopaIngestionActivityStub())
      .thenReturn(receiptPagopaIngestionActivityMock);
    Mockito.when(receiptPagopaIngestionWfConfigMock.buildReceiptPagopaNotifySilActivityStub())
      .thenReturn(receiptPagopaNotifySilActivityMock);
    Mockito.when(receiptPagopaIngestionWfConfigMock.buildReceiptPagopaSendEmailActivityStub())
      .thenReturn(receiptPagopaSendEmailActivityMock);

    Mockito.when(baseIngestionFlowFileWFConfigMock.buildUpdateIngestionFlowStatusActivityStub())
      .thenReturn(updateIngestionFlowStatusActivityMock);

    wf = new ReceiptPagopaIngestionWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @Test
  void givenSuccessfulProcessingConditionWhenIngestThenOk(){
    // Given
    long ingestionFlowFileId = 1L;
    ReceiptWithAdditionalNodeDataDTO receiptDTO = new ReceiptWithAdditionalNodeDataDTO();
    InstallmentDTO installmentDTO = new InstallmentDTO();

    ReceiptPagopaIngestionFlowFileResult result = new ReceiptPagopaIngestionFlowFileResult(receiptDTO);

    Mockito.when(receiptPagopaIngestionActivityMock.processFile(ingestionFlowFileId)).thenReturn(result);
    Mockito.when(receiptPagopaNotifySilActivityMock.notifyReceiptToSil(receiptDTO)).thenReturn(installmentDTO);
    // When
    Assertions.assertDoesNotThrow(() -> wf.ingest(ingestionFlowFileId));

    // Then
    Mockito.verify(updateIngestionFlowStatusActivityMock, Mockito.times(1))
      .updateIngestionFlowFileStatus(ingestionFlowFileId, IngestionFlowFileStatus.UPLOADED, IngestionFlowFileStatus.PROCESSING, null);
    Mockito.verify(updateIngestionFlowStatusActivityMock, Mockito.times(1))
      .updateIngestionFlowFileStatus(ingestionFlowFileId, IngestionFlowFileStatus.PROCESSING, IngestionFlowFileStatus.COMPLETED, result);
    Mockito.verify(receiptPagopaSendEmailActivityMock, Mockito.times(1))
      .sendReceiptHandledEmail(receiptDTO, installmentDTO);
    Mockito.verify(receiptPagopaNotifySilActivityMock, Mockito.times(1))
      .notifyReceiptToSil(receiptDTO);
  }

  @Test
  void givenUnexpectedExceptionWhenIngestThenKo(){
    // Given
    long ingestionFlowFileId = 1L;

    Mockito.when(receiptPagopaIngestionActivityMock.processFile(ingestionFlowFileId))
      .thenThrow(new NotRetryableActivityException("DUMMY"));

    IngestionFlowFileResult ingestionFlowFileResult = IngestionFlowFileResult.builder()
      .errorDescription("DUMMY")
      .build();

    // When
    wf.ingest(ingestionFlowFileId);

    // Then
    Mockito.verify(updateIngestionFlowStatusActivityMock, Mockito.times(1))
      .updateIngestionFlowFileStatus(ingestionFlowFileId, IngestionFlowFileStatus.UPLOADED, IngestionFlowFileStatus.PROCESSING, null);
    Mockito.verify(updateIngestionFlowStatusActivityMock, Mockito.times(1))
      .updateIngestionFlowFileStatus(ingestionFlowFileId, IngestionFlowFileStatus.PROCESSING, IngestionFlowFileStatus.ERROR, ingestionFlowFileResult);
    Mockito.verifyNoInteractions(receiptPagopaNotifySilActivityMock, receiptPagopaSendEmailActivityMock);
  }

  @Test
  void givenUnexpectedExceptionInHandleNotifySilWhenIngestThenOk(){
    // Given
    long ingestionFlowFileId = 1L;
    ReceiptWithAdditionalNodeDataDTO receiptDTO = new ReceiptWithAdditionalNodeDataDTO();

    ReceiptPagopaIngestionFlowFileResult result = new ReceiptPagopaIngestionFlowFileResult(receiptDTO);

    Mockito.when(receiptPagopaIngestionActivityMock.processFile(ingestionFlowFileId)).thenReturn(result);

    Mockito.doThrow(new NotRetryableActivityException("DUMMY")).when(receiptPagopaNotifySilActivityMock)
      .notifyReceiptToSil(receiptDTO);

    // When
    Assertions.assertDoesNotThrow(() -> wf.ingest(ingestionFlowFileId));

    // Then
    Mockito.verify(updateIngestionFlowStatusActivityMock, Mockito.times(1))
      .updateIngestionFlowFileStatus(ingestionFlowFileId, IngestionFlowFileStatus.UPLOADED, IngestionFlowFileStatus.PROCESSING, null);
    Mockito.verify(updateIngestionFlowStatusActivityMock, Mockito.times(1))
      .updateIngestionFlowFileStatus(ingestionFlowFileId, IngestionFlowFileStatus.PROCESSING, IngestionFlowFileStatus.COMPLETED, result);
    Mockito.verify(receiptPagopaSendEmailActivityMock, Mockito.times(1))
      .sendReceiptHandledEmail(receiptDTO, null);
    Mockito.verify(receiptPagopaNotifySilActivityMock, Mockito.times(1))
      .notifyReceiptToSil(receiptDTO);
  }
}
