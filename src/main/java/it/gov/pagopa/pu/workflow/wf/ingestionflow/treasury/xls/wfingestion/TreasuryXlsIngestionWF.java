package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.xls.wfingestion;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWF;

/**
 * Workflow to ingest treasury xls file
 */
@WorkflowInterface
public interface TreasuryXlsIngestionWF extends BaseIngestionFlowFileWF {
  @Override
  @WorkflowMethod
  void ingest(Long ingestionFlowFileId);
}
