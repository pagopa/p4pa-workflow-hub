package it.gov.pagopa.pu.workflow.service.ingestionflowfile;

import it.gov.pagopa.payhub.activities.exception.ingestionflow.IngestionFlowTypeNotSupportedException;
import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFile;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.debtposition.DebtPositionIngestionWFClient;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentnotification.PaymentNotificationIngestionWFClient;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.PaymentsReportingIngestionWFClient;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.receipt.pagopa.ReceiptPagopaIngestionWFClient;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.TreasuryOpiIngestionWFClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.function.Function;

@ExtendWith(MockitoExtension.class)
class IngestionFlowFileStarterServiceTest {

  @Mock
  private PaymentsReportingIngestionWFClient paymentsReportingIngestionWFClientMock;
  @Mock
  private TreasuryOpiIngestionWFClient treasuryOpiIngestionWFClientMock;
  @Mock
  private DebtPositionIngestionWFClient debtPositionIngestionWFClientMock;
  @Mock
  private ReceiptPagopaIngestionWFClient receiptPagopaIngestionWFClientMock;
  @Mock
  private PaymentNotificationIngestionWFClient paymentNotificationIngestionWFClientMock;

  private IngestionFlowFileStarterService service;

  private Map<IngestionFlowFile.IngestionFlowFileTypeEnum, Function<Long, String>> flowFileType2ClientInvoker;

  @BeforeEach
  void init(){
    this.service = new IngestionFlowFileStarterServiceImpl(
      paymentsReportingIngestionWFClientMock,
      treasuryOpiIngestionWFClientMock,
      debtPositionIngestionWFClientMock,
      receiptPagopaIngestionWFClientMock,
      paymentNotificationIngestionWFClientMock);

    flowFileType2ClientInvoker = Map.of(
      IngestionFlowFile.IngestionFlowFileTypeEnum.PAYMENTS_REPORTING, paymentsReportingIngestionWFClientMock::ingest,
      IngestionFlowFile.IngestionFlowFileTypeEnum.TREASURY_OPI, treasuryOpiIngestionWFClientMock::ingest,
      IngestionFlowFile.IngestionFlowFileTypeEnum.DP_INSTALLMENTS, debtPositionIngestionWFClientMock::ingest,
      IngestionFlowFile.IngestionFlowFileTypeEnum.RECEIPT_PAGOPA, receiptPagopaIngestionWFClientMock::ingest,
      IngestionFlowFile.IngestionFlowFileTypeEnum.PAYMENT_NOTIFICATION, paymentNotificationIngestionWFClientMock::ingest

    );
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      paymentsReportingIngestionWFClientMock,
      treasuryOpiIngestionWFClientMock,
      debtPositionIngestionWFClientMock,
      receiptPagopaIngestionWFClientMock,
      paymentNotificationIngestionWFClientMock
    );
  }

  @ParameterizedTest
  @EnumSource(IngestionFlowFile.IngestionFlowFileTypeEnum.class)
  void whenThenInvokeClientIfAny(IngestionFlowFile.IngestionFlowFileTypeEnum ingestionFlowFileType){
    // Given
    long ingestionFlowFileId = 1L;
    String expectedWorkflowId = "WORKFLOWID";

    Function<Long, String> expectedClientInvoker = flowFileType2ClientInvoker.get(ingestionFlowFileType);
    if(expectedClientInvoker!=null){
      Mockito.when(expectedClientInvoker.apply(ingestionFlowFileId))
        .thenReturn(expectedWorkflowId);
    }

    // When
    try{
      String wfId = service.ingest(ingestionFlowFileId, ingestionFlowFileType);

      // Then
      Assertions.assertEquals(expectedWorkflowId, wfId);
    } catch (IngestionFlowTypeNotSupportedException e){
      Assertions.assertNull(expectedClientInvoker, "IngestionFlowFileType expected to be supported: " + ingestionFlowFileType);
    }
  }

}
