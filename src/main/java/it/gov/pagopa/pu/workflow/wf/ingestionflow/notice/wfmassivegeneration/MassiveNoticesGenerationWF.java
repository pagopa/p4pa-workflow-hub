package it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.wfmassivegeneration;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface MassiveNoticesGenerationWF {
  @WorkflowMethod
  void generate(Long ingestionFlowFileId);
}
