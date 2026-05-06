package it.gov.pagopa.pu.workflow.wf.pagopa.send.delete.wf;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Workflow interface for deleting the send notification file process.
 * <p>
 * This workflow coordinates a series of activities for deleting the expired notification files
 * </p>
 * */
@WorkflowInterface
public interface DeleteSendNotificationFileWF {
  /**
   * Workflow method to delete expired send notification files.
   *
   * @param sendNotificationId the unique identifier of the send notification
   */
  @WorkflowMethod
  void deleteSendNotificationExpiredFiles(String sendNotificationId);
}
