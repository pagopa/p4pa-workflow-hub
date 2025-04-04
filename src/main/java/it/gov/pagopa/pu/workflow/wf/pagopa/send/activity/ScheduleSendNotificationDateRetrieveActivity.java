package it.gov.pagopa.pu.workflow.wf.pagopa.send.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

import java.time.Duration;

@ActivityInterface
public interface ScheduleSendNotificationDateRetrieveActivity {
  @ActivityMethod
  void scheduleSendNotificationDateRetrieveWF(String sendNotificationId, Duration nextSchedule);
}
