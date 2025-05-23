package it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.wfingestion;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWF;

/**
 * Workflow to ingest PaymentsReporting file
 * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1339031693/Classificazione+incassi#3.3.-Rendicontazione>Confluence page</a>
 */
@WorkflowInterface
public interface PaymentsReportingIngestionWF extends BaseIngestionFlowFileWF {
  @WorkflowMethod
  @Override
  void ingest(Long ingestionFlowFileId);
}
