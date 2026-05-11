package it.gov.pagopa.pu.workflow.wf.pagopa.send.delete.wfsendlegalfact;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Workflow interface for deleting the send legal fact file process.
 * <p>
 * This workflow coordinates a series of activities for deleting the expired legal facts
 * </p>
 * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/2626388056/Notifica+SEND+New>Confluence page</a>
 * */
@WorkflowInterface
public interface DeleteSendLegalFactFileWF {
  /**
   * Workflow method to delete expired send legal facts.
   *
   * @param sendNotificationId the unique identifier of the send notification
   */
  @WorkflowMethod
  void deleteSendLegalFactExpiredFiles(String sendNotificationId);
}
