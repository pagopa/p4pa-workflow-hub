package it.gov.pagopa.pu.workflow.wf.pagopa.send.wfsendnotification;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.sendnotification.*;
import it.gov.pagopa.pu.workflow.config.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.config.SendNotificationProcessWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@Slf4j
@WorkflowImpl(taskQueues = SendNotificationDateRetrieveWFImpl.TASK_QUEUE_SEND_NOTIFICATION_DATE_RETRIEVE)
public class SendNotificationDateRetrieveWFImpl implements SendNotificationDateRetrieveWF, ApplicationContextAware {
  public static final String TASK_QUEUE_SEND_NOTIFICATION_DATE_RETRIEVE = "SendNotificationDateRetrieveWF";

  private SendNotificationDateRetrieveActivity sendNotificationDateRetrieveActivity;

  /**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    SendNotificationProcessWfConfig wfConfig = applicationContext.getBean(SendNotificationProcessWfConfig.class);

    sendNotificationDateRetrieveActivity = wfConfig.buildSendNotificationDateRetrieveActivityStub();
  }

  @Override
  public void sendNotificationDateRetrieve(String sendNotificationId) {

    sendNotificationDateRetrieveActivity.sendNotificationDateRetrieve(sendNotificationId);
  }
}
