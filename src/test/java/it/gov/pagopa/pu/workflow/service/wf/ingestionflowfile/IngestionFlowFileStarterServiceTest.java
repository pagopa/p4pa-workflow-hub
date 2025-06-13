package it.gov.pagopa.pu.workflow.service.wf.ingestionflowfile;

import it.gov.pagopa.payhub.activities.exception.ingestionflow.IngestionFlowTypeNotSupportedException;
import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFile;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.debtposition.DebtPositionIngestionWFClient;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.debtpositiontype.DebtPositionTypeIngestionWFClient;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.organization.OrganizationIngestionWFClient;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentnotification.PaymentNotificationIngestionWFClient;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.PaymentsReportingIngestionWFClient;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.receipt.csv.ReceiptIngestionWFClient;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.receipt.pagopa.ReceiptPagopaIngestionWFClient;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.csvcomplete.TreasuryCsvCompleteIngestionWFClient;
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
  private TreasuryCsvCompleteIngestionWFClient treasuryCsvCompleteIngestionWFClientMock;
  @Mock
  private DebtPositionIngestionWFClient debtPositionIngestionWFClientMock;
  @Mock
  private ReceiptIngestionWFClient receiptIngestionWFClientMock;
  @Mock
  private ReceiptPagopaIngestionWFClient receiptPagopaIngestionWFClientMock;
  @Mock
  private PaymentNotificationIngestionWFClient paymentNotificationIngestionWFClientMock;
  @Mock
  private OrganizationIngestionWFClient organizationIngestionWFClientMock;
  @Mock
  private DebtPositionTypeIngestionWFClient debtPositionTypeIngestionWFClientMock;

  private IngestionFlowFileStarterService service;

  private Map<IngestionFlowFile.IngestionFlowFileTypeEnum, Function<Long, WorkflowCreatedDTO>> flowFileType2ClientInvoker;

  @BeforeEach
  void init(){
    this.service = new IngestionFlowFileStarterServiceImpl(
      paymentsReportingIngestionWFClientMock,
      treasuryOpiIngestionWFClientMock,
      treasuryCsvCompleteIngestionWFClientMock,
      debtPositionIngestionWFClientMock,
      receiptIngestionWFClientMock,
      receiptPagopaIngestionWFClientMock,
      paymentNotificationIngestionWFClientMock,
      organizationIngestionWFClientMock,
      debtPositionTypeIngestionWFClientMock);

    flowFileType2ClientInvoker = Map.of(
      IngestionFlowFile.IngestionFlowFileTypeEnum.PAYMENTS_REPORTING, paymentsReportingIngestionWFClientMock::ingest,
      IngestionFlowFile.IngestionFlowFileTypeEnum.TREASURY_OPI, treasuryOpiIngestionWFClientMock::ingest,
      IngestionFlowFile.IngestionFlowFileTypeEnum.TREASURY_CSV_COMPLETE, treasuryCsvCompleteIngestionWFClientMock::ingest,
      IngestionFlowFile.IngestionFlowFileTypeEnum.DP_INSTALLMENTS, debtPositionIngestionWFClientMock::ingest,
      IngestionFlowFile.IngestionFlowFileTypeEnum.RECEIPT, receiptIngestionWFClientMock::ingest,
      IngestionFlowFile.IngestionFlowFileTypeEnum.RECEIPT_PAGOPA, receiptPagopaIngestionWFClientMock::ingest,
      IngestionFlowFile.IngestionFlowFileTypeEnum.PAYMENT_NOTIFICATION, paymentNotificationIngestionWFClientMock::ingest,
      IngestionFlowFile.IngestionFlowFileTypeEnum.ORGANIZATIONS, organizationIngestionWFClientMock::ingest,
      IngestionFlowFile.IngestionFlowFileTypeEnum.DEBT_POSITIONS_TYPE, debtPositionTypeIngestionWFClientMock::ingest
    );
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      paymentsReportingIngestionWFClientMock,
      treasuryOpiIngestionWFClientMock,
      treasuryCsvCompleteIngestionWFClientMock,
      debtPositionIngestionWFClientMock,
      receiptIngestionWFClientMock,
      receiptPagopaIngestionWFClientMock,
      paymentNotificationIngestionWFClientMock,
      organizationIngestionWFClientMock
    );
  }

  @ParameterizedTest
  @EnumSource(IngestionFlowFile.IngestionFlowFileTypeEnum.class)
  void whenThenInvokeClientIfAny(IngestionFlowFile.IngestionFlowFileTypeEnum ingestionFlowFileType){
    // Given
    long ingestionFlowFileId = 1L;
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("WORKFLOWID", "RUNID");

    Function<Long, WorkflowCreatedDTO> expectedClientInvoker = flowFileType2ClientInvoker.get(ingestionFlowFileType);
    if(expectedClientInvoker!=null){
      Mockito.when(expectedClientInvoker.apply(ingestionFlowFileId))
        .thenReturn(expectedResult);
    }

    // When
    try{
      WorkflowCreatedDTO wfExec = service.ingest(ingestionFlowFileId, ingestionFlowFileType);

      // Then
      Assertions.assertEquals(expectedResult, wfExec);
    } catch (IngestionFlowTypeNotSupportedException e){
      Assertions.assertNull(expectedClientInvoker, "IngestionFlowFileType expected to be supported: " + ingestionFlowFileType);
    }
  }

}
