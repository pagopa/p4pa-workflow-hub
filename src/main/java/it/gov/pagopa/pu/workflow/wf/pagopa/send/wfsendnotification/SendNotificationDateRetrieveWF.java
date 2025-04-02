package it.gov.pagopa.pu.workflow.wf.pagopa.send.wfsendnotification;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.sendnotification.dto.generated.SendNotificationDTO;

/**
 * Workflow interface for retrieve and save date notification.
 * <p>
 * This workflow retrieves the notification date from the send-notification
 * service and stores it for each nav entry provided as input.
 * </p>
 * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1601241160/Notifica+SEND>Confluence page</a>
 * */
@WorkflowInterface
public interface SendNotificationDateRetrieveWF {

  /**
   * Interface for a workflow that retrieves and saves notification date.
   * <p>
   * Initiates an activity to retrieve the notification date. If the date is
   * not present, it returns a null object. If the date is present, it calls
   * the debt-positions microservice to store the date for each nav entry
   * provided as input.
   * </p>
   *
   * @param sendNotificationId the unique identifier of the notification to be sent
   */
  @WorkflowMethod
  SendNotificationDTO sendNotificationDateRetrieve(String sendNotificationId);
}
