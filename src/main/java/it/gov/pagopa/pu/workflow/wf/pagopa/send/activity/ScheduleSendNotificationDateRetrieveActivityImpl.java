package it.gov.pagopa.pu.workflow.wf.pagopa.send.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.SendNotificationWFClient;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.wfsendnotification.SendNotificationProcessWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
@ActivityImpl(taskQueues = SendNotificationProcessWFImpl.TASK_QUEUE_SEND_NOTIFICATION_PROCESS_LOCAL_ACTIVITY)
public class ScheduleSendNotificationDateRetrieveActivityImpl implements ScheduleSendNotificationDateRetrieveActivity {

  private final SendNotificationWFClient sendNotificationWFClient;

  public ScheduleSendNotificationDateRetrieveActivityImpl(SendNotificationWFClient sendNotificationWFClient) {
    this.sendNotificationWFClient = sendNotificationWFClient;
  }

  @Override
  public void scheduleSendNotificationDateRetrieveWF(String sendNotificationId, Duration nextSchedule) {
    sendNotificationWFClient.scheduleSendNotificationDateRetrieve(sendNotificationId, nextSchedule);
  }
}
