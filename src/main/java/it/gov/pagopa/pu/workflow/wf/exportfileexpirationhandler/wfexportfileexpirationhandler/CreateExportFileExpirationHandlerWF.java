package it.gov.pagopa.pu.workflow.wf.exportfileexpirationhandler.wfexportfileexpirationhandler;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface CreateExportFileExpirationHandlerWF {

  @WorkflowMethod
  void createExportFileExpirationHandler(Long exportFileId);
}
