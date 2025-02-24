package it.gov.pagopa.pu.workflow.service.ingestionflowfile;

import it.gov.pagopa.payhub.activities.exception.ingestionflow.IngestionFlowTypeNotSupportedException;
import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFile;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.debtposition.DebtPositionIngestionWFClient;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.PaymentsReportingIngestionWFClient;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.TreasuryOpiIngestionWFClient;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Function;

@Service
public class IngestionFlowFileStarterServiceImpl implements IngestionFlowFileStarterService {

  private final Map<IngestionFlowFile.FlowFileTypeEnum, Function<Long, String>> ingestionFlowFileType2WfStarter;

  public IngestionFlowFileStarterServiceImpl(PaymentsReportingIngestionWFClient paymentsReportingIngestionWFClient, TreasuryOpiIngestionWFClient treasuryOpiIngestionWFClient, DebtPositionIngestionWFClient debtPositionIngestionWFClient) {
    ingestionFlowFileType2WfStarter = Map.of(
      IngestionFlowFile.FlowFileTypeEnum.PAYMENTS_REPORTING, paymentsReportingIngestionWFClient::ingest,
      IngestionFlowFile.FlowFileTypeEnum.TREASURY_OPI, treasuryOpiIngestionWFClient::ingest,
      IngestionFlowFile.FlowFileTypeEnum.DP_INSTALLMENTS, debtPositionIngestionWFClient::ingest
    );
  }

  @Override
  public String ingest(long ingestionFlowFileId, IngestionFlowFile.FlowFileTypeEnum flowFileType) {
    return ingestionFlowFileType2WfStarter.getOrDefault(flowFileType, x -> {
        throw new IngestionFlowTypeNotSupportedException("IngestionFlowFileType not supported: " + flowFileType);
      })
      .apply(ingestionFlowFileId);
  }

}
