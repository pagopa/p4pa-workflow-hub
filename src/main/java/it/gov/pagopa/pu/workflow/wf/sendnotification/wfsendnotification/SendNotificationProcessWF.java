package it.gov.pagopa.pu.workflow.wf.sendnotification.wfsendnotification;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Workflow interface for Send Notification Workflow
 * */
@WorkflowInterface
public interface SendNotificationProcessWF {

  /**
   * Workflow method for Send Notification Workflow
   * @param sendNotificationId the id of the send notification
   * */
  @WorkflowMethod
  void sendNotificationProcess(String sendNotificationId);
}
