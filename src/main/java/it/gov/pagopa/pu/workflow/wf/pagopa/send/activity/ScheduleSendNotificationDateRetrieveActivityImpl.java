package it.gov.pagopa.pu.workflow.wf.pagopa.send.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.SendNotificationWFClient;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.wfsendnotification.SendNotificationProcessWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ActivityImpl(taskQueues = SendNotificationProcessWFImpl.TASK_QUEUE_SEND_NOTIFICATION_DATE_RETRIEVE_LOCAL_ACTIVITY)
public class ScheduleSendNotificationDateRetrieveActivityImpl implements ScheduleSendNotificationDateRetrieveActivity {

  private final SendNotificationWFClient sendNotificationWFClient;

  public ScheduleSendNotificationDateRetrieveActivityImpl(SendNotificationWFClient sendNotificationWFClient) {
    this.sendNotificationWFClient = sendNotificationWFClient;
  }

  @Override
  public void scheduleSendNotificationDateRetrieve(String sendNotificationId) {
    sendNotificationWFClient.scheduleSendNotificationDateRetrieve(sendNotificationId);
  }
}
