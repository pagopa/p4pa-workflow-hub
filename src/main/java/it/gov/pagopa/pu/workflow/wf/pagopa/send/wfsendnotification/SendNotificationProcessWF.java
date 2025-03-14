package it.gov.pagopa.pu.workflow.wf.pagopa.send.wfsendnotification;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Workflow interface for managing the notification sending process.
 * <p>
 * This workflow coordinates a series of activities including the preloading,
 * uploading, and delivery of files associated with a notification, as well as
 * checking the notification status.
 * </p>
 * <p>
 * The process is designed to handle sending operations, implementing
 * retry mechanisms if the notification is not accepted.
 * </p>
 * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1601241160/Notifica+SEND>Confluence page</a>
 * */
@WorkflowInterface
public interface SendNotificationProcessWF {

  /**
   * Workflow method to manage the notification sending process.
   * <p>
   * Initiates a series of activities including preloading, uploading, and
   * delivering the notification, followed by a check on its acceptance status.
   * If the notification status is not "ACCEPTED," the method will perform retries
   * up to the configured maximum number of attempts.
   * </p>
   *
   * @param sendNotificationId the unique identifier of the notification to be sent
   */
  @WorkflowMethod
  void sendNotificationProcess(String sendNotificationId);
}
