package it.gov.pagopa.pu.workflow.wf.pagopa.send.wfsendnotification;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Workflow interface for managing the stream events consume process.
 * <p>
 * This workflow coordinates a series of activities for consuming the notification
 * events in the SEND stream.
 * </p>
 * <p>
 * The process is designed to handle notification events from SEND stream.
 * </p>
 * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1601241160/Notifica+SEND>Confluence page</a>
 * */
@WorkflowInterface
public interface SendNotificationStreamConsumeWF {
  /**
   * Workflow method to handle notification events from stream.
   *
   * @param sendStreamId the unique identifier of the stream to read from
   */
  @WorkflowMethod
  void readSendStream(String sendStreamId);
}
