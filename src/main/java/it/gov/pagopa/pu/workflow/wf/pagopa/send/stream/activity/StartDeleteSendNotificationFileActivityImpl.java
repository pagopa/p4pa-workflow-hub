package it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.delete.DeleteSendNotificationFileWFClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ActivityImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_SEND_RESERVED_STREAM_CONSUME_LOCAL)
public class StartDeleteSendNotificationFileActivityImpl implements StartDeleteSendNotificationFileActivity {

  private final DeleteSendNotificationFileWFClient deleteSendNotificationFileWFClient;

  public StartDeleteSendNotificationFileActivityImpl(
    DeleteSendNotificationFileWFClient deleteSendNotificationFileWFClient) {
    this.deleteSendNotificationFileWFClient = deleteSendNotificationFileWFClient;
  }

  @Override
  public void startDeleteSendNotificationExpiredFiles(String sendNotificationId) {
    log.info("startDeleteSendNotificationExpiredFiles - sendNotificationId: {}", sendNotificationId);
    deleteSendNotificationFileWFClient.startDeleteSendNotificationExpiredFiles(sendNotificationId);
  }
}
