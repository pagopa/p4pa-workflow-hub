package it.gov.pagopa.pu.workflow.wf.ingestionflow.debtpositiontypeorg.wfingestion;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWF;

/**
 * Workflow to ingest DebtPositionTypeOrg file
 */
@WorkflowInterface
public interface DebtPositionTypeOrgIngestionWF extends BaseIngestionFlowFileWF {
  @Override
  @WorkflowMethod
  void ingest(Long ingestionFlowFileId);
}
