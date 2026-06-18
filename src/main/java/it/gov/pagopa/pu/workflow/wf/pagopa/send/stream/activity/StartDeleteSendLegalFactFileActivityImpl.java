package it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.delete.DeleteSendLegalFactFileWFClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ActivityImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_SEND_RESERVED_STREAM_CONSUME_LOCAL)
public class StartDeleteSendLegalFactFileActivityImpl implements StartDeleteSendLegalFactFileActivity {

  private final DeleteSendLegalFactFileWFClient deleteSendLegalFactFileWFClient;

  public StartDeleteSendLegalFactFileActivityImpl(DeleteSendLegalFactFileWFClient deleteSendLegalFactFileWFClient) {
    this.deleteSendLegalFactFileWFClient = deleteSendLegalFactFileWFClient;
  }

  @Override
  public void startDeleteSendLegalFactExpiredFiles(String sendNotificationId) {
    log.info("startDeleteSendLegalFactExpiredFiles - sendNotificationId: {}", sendNotificationId);
    deleteSendLegalFactFileWFClient.startDeleteSendLegalFactExpiredFiles(sendNotificationId);
  }
}
