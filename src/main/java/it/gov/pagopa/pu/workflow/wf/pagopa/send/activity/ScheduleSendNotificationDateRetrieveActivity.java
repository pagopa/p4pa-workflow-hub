package it.gov.pagopa.pu.workflow.wf.pagopa.send.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface ScheduleSendNotificationDateRetrieveActivity {
  @ActivityMethod
  void scheduleSendNotificationDateRetrieve(String sendNotificationId);
}
