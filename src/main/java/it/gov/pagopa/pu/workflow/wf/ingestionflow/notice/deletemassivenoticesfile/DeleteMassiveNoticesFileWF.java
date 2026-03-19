package it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.deletemassivenoticesfile;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface DeleteMassiveNoticesFileWF {
  @WorkflowMethod
  void deleteMassiveNoticesFile(Long ingestionFlowFileId);
}
