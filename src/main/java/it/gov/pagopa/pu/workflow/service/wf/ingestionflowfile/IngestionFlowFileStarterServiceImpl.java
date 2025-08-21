package it.gov.pagopa.pu.workflow.service.wf.ingestionflowfile;

import it.gov.pagopa.payhub.activities.exception.ingestionflow.IngestionFlowTypeNotSupportedException;
import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFile;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.assessments.AssessmentsIngestionWFClient;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.assessmentsregistry.AssessmentsRegistryIngestionWFClient;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.debtposition.DebtPositionIngestionWFClient;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.debtpositiontype.DebtPositionTypeIngestionWFClient;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.debtpositiontypeorg.DebtPositionTypeOrgIngestionWFClient;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.organization.OrganizationIngestionWFClient;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.orgsilservice.OrgSilServiceIngestionWFClient;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentnotification.PaymentNotificationIngestionWFClient;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.PaymentsReportingIngestionWFClient;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.receipt.csv.ReceiptIngestionWFClient;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.receipt.pagopa.ReceiptPagopaIngestionWFClient;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.csvcomplete.TreasuryCsvCompleteIngestionWFClient;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.TreasuryOpiIngestionWFClient;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Function;

@Service
public class IngestionFlowFileStarterServiceImpl implements IngestionFlowFileStarterService {

  private final Map<IngestionFlowFile.IngestionFlowFileTypeEnum, Function<Long, WorkflowCreatedDTO>> ingestionFlowFileType2WfStarter;

  public IngestionFlowFileStarterServiceImpl(
    PaymentsReportingIngestionWFClient paymentsReportingIngestionWFClient,
    TreasuryOpiIngestionWFClient treasuryOpiIngestionWFClient,
    TreasuryCsvCompleteIngestionWFClient treasuryCsvCompleteIngestionWFClient,
    DebtPositionIngestionWFClient debtPositionIngestionWFClient,
    ReceiptIngestionWFClient receiptIngestionWFClient,
    ReceiptPagopaIngestionWFClient receiptPagopaIngestionWFClient,
    PaymentNotificationIngestionWFClient paymentNotificationIngestionWFClient,
    OrganizationIngestionWFClient organizationIngestionWFClient,
    OrgSilServiceIngestionWFClient orgSilServiceIngestionWFClient,
    DebtPositionTypeIngestionWFClient debtPositionTypeIngestionWFClient,
    DebtPositionTypeOrgIngestionWFClient debtPositionTypeOrgIngestionWFClient,
    AssessmentsIngestionWFClient assessmentsIngestionWFClient,
    AssessmentsRegistryIngestionWFClient assessmentsRegistryIngestionWFClient
    ){
    ingestionFlowFileType2WfStarter = Map.ofEntries(
      Map.entry(IngestionFlowFile.IngestionFlowFileTypeEnum.PAYMENTS_REPORTING, paymentsReportingIngestionWFClient::ingest),
      Map.entry(IngestionFlowFile.IngestionFlowFileTypeEnum.PAYMENTS_REPORTING_PAGOPA, paymentsReportingIngestionWFClient::ingest),
      Map.entry(IngestionFlowFile.IngestionFlowFileTypeEnum.TREASURY_OPI, treasuryOpiIngestionWFClient::ingest),
      Map.entry(IngestionFlowFile.IngestionFlowFileTypeEnum.TREASURY_CSV_COMPLETE, treasuryCsvCompleteIngestionWFClient::ingest),
      Map.entry(IngestionFlowFile.IngestionFlowFileTypeEnum.DP_INSTALLMENTS, debtPositionIngestionWFClient::ingest),
      Map.entry(IngestionFlowFile.IngestionFlowFileTypeEnum.RECEIPT, receiptIngestionWFClient::ingest),
      Map.entry(IngestionFlowFile.IngestionFlowFileTypeEnum.RECEIPT_PAGOPA, receiptPagopaIngestionWFClient::ingest),
      Map.entry(IngestionFlowFile.IngestionFlowFileTypeEnum.PAYMENT_NOTIFICATION, paymentNotificationIngestionWFClient::ingest),
      Map.entry(IngestionFlowFile.IngestionFlowFileTypeEnum.ORGANIZATIONS, organizationIngestionWFClient::ingest),
      Map.entry(IngestionFlowFile.IngestionFlowFileTypeEnum.ORGANIZATIONS_SIL_SERVICE, orgSilServiceIngestionWFClient::ingest),
      Map.entry(IngestionFlowFile.IngestionFlowFileTypeEnum.DEBT_POSITIONS_TYPE, debtPositionTypeIngestionWFClient::ingest),
      Map.entry(IngestionFlowFile.IngestionFlowFileTypeEnum.DEBT_POSITIONS_TYPE_ORG, debtPositionTypeOrgIngestionWFClient::ingest),
      Map.entry(IngestionFlowFile.IngestionFlowFileTypeEnum.ASSESSMENTS, assessmentsIngestionWFClient::ingest),
      Map.entry(IngestionFlowFile.IngestionFlowFileTypeEnum.ASSESSMENTS_REGISTRY, assessmentsRegistryIngestionWFClient::ingest),
      Map.entry(IngestionFlowFile.IngestionFlowFileTypeEnum.SEND_NOTIFICATION, assessmentsRegistryIngestionWFClient::ingest)
    );
  }

  @Override
  public WorkflowCreatedDTO ingest(long ingestionFlowFileId, IngestionFlowFile.IngestionFlowFileTypeEnum flowFileType) {
    return ingestionFlowFileType2WfStarter.getOrDefault(flowFileType, x -> {
        throw new IngestionFlowTypeNotSupportedException("IngestionFlowFileType not supported: " + flowFileType);
      })
      .apply(ingestionFlowFileId);
  }

}
