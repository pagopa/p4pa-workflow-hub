package it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.wf;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import it.gov.pagopa.pu.workflow.dto.SendStreamEventsDTO;

/**
 * Child Workflow interface for managing the stream events consume process.
 * <p>
 * This Child workflow elaborates notification events in the SEND stream.
 * </p>
 * <p>
 * The process is designed to handle notification events from SEND stream.
 * </p>
 * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/2626388056/Notifica+SEND+New#SendNotificationEventsConsumerWF-Child-workflow>Confluence page</a>
 * */
@WorkflowInterface
public interface SendNotificationEventsConsumerWF {
  /**
   * Workflow method to handle notification events from stream.
   *
   * @param sendStreamEventsDTO contains: organization id, SEND stream id, list of SEND events
   * @return last processed event id
   */
  @WorkflowMethod
  String processingStreamEvents(SendStreamEventsDTO sendStreamEventsDTO);
}
