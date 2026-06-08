package it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface StartDeleteSendLegalFactFileActivity {

  @ActivityMethod
  void startDeleteSendLegalFactExpiredFiles(String sendNotificationId);
}
