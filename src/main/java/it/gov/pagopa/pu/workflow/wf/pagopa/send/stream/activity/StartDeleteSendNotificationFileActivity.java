package it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface StartDeleteSendNotificationFileActivity {

  @ActivityMethod
  void startDeleteSendNotificationExpiredFiles(String sendNotificationId);
}
