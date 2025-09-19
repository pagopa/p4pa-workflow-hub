package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.csv.wfingestion;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWF;

/**
 * Workflow to ingest treasury csv file
 */
@WorkflowInterface
public interface TreasuryCsvIngestionWF extends BaseIngestionFlowFileWF {
  @Override
  @WorkflowMethod
  void ingest(Long ingestionFlowFileId);
}
