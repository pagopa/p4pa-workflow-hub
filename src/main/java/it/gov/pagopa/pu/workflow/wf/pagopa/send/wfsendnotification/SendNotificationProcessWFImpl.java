package it.gov.pagopa.pu.workflow.wf.pagopa.send.wfsendnotification;

import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.sendnotification.*;
import it.gov.pagopa.pu.sendnotification.dto.generated.NotificationStatus;
import it.gov.pagopa.pu.sendnotification.dto.generated.SendNotificationDTO;
import it.gov.pagopa.pu.workflow.config.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowInternalErrorException;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.activity.PublishSendNotificationPaymentEventActivity;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.activity.ScheduleSendNotificationDateRetrieveActivity;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.config.SendNotificationProcessWfConfig;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.dto.DebtPositionSendNotificationDTO;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.mapper.SendNotification2DebtPositionSendNotificationsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.time.Duration;

@Slf4j
@WorkflowImpl(taskQueues = SendNotificationProcessWFImpl.TASK_QUEUE_SEND_NOTIFICATION_PROCESS)
public class SendNotificationProcessWFImpl implements SendNotificationProcessWF, ApplicationContextAware {
  public static final String TASK_QUEUE_SEND_NOTIFICATION_PROCESS = "SendNotificationProcessWF";
  public static final String TASK_QUEUE_SEND_NOTIFICATION_PROCESS_LOCAL_ACTIVITY = "SendNotificationProcessWF_LOCAL";

  private static final int MAX_RETRIES = 10;
  private static final Duration RETRY_INTERVAL = Duration.ofMinutes(5);
  private static final Duration NOTIFICATION_DATE_RETRIEVE_DELAY = Duration.ofMinutes(30);

  private PreloadSendFileActivity preloadSendFileActivity;
  private UploadSendFileActivity uploadSendFileActivity;
  private DeliveryNotificationActivity deliveryNotificationActivity;
  private NotificationStatusActivity notificationStatusActivity;
  private GetSendNotificationActivity getSendNotificationActivity;
  private PublishSendNotificationPaymentEventActivity publishSendNotificationPaymentEventActivity;
  private ScheduleSendNotificationDateRetrieveActivity scheduleSendNotificationDateRetrieveActivity;

  /**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    SendNotificationProcessWfConfig wfConfig = applicationContext.getBean(SendNotificationProcessWfConfig.class);

    preloadSendFileActivity = wfConfig.buildPreloadSendFileActivityStub();
    uploadSendFileActivity = wfConfig.buildUploadSendFileActivityStub();
    deliveryNotificationActivity = wfConfig.buildDeliveryNotificationActivityStub();
    notificationStatusActivity = wfConfig.buildNotificationStatusActivityStub();
    getSendNotificationActivity = wfConfig.buildGetSendNotificationActivityStub();
    publishSendNotificationPaymentEventActivity = wfConfig.buildPublishSendNotificationPaymentEventActivityStub();
    scheduleSendNotificationDateRetrieveActivity = wfConfig.buildScheduleSendNotificationDateRetrieveActivityStub();
  }

  @Override
  public void sendNotificationProcess(String sendNotificationId) {
    log.info("Start sendNotificationProcess Workflow for sendNotificationId {}", sendNotificationId);

    try {
      preloadSendFileActivity.preloadSendFile(sendNotificationId);
      uploadSendFileActivity.uploadSendFile(sendNotificationId);
      deliveryNotificationActivity.deliveryNotification(sendNotificationId);

      SendNotificationDTO sendNotificationDTO = waitDeliveryAcceptance(sendNotificationId);

      publishSendEvent(sendNotificationDTO, new PaymentEventRequestDTO(PaymentEventType.SEND_NOTIFICATION_CREATED, null));

      scheduleSendNotificationDateRetrieveActivity.scheduleSendNotificationDateRetrieveWF(sendNotificationId, NOTIFICATION_DATE_RETRIEVE_DELAY);
    } catch (RuntimeException e){
      SendNotificationDTO notification = getSendNotificationActivity.getSendNotification(sendNotificationId);
      if (notification != null) {
        for (DebtPositionSendNotificationDTO p : SendNotification2DebtPositionSendNotificationsMapper.map(notification)) {
          publishSendNotificationPaymentEventActivity.publishSendNotificationErrorEvent(p,
            new PaymentEventRequestDTO(PaymentEventType.SEND_NOTIFICATION_ERROR, e.getMessage()));
        }
      } else {
        log.info("Provided unknown sendNotificationId {}", sendNotificationId);
      }
      throw e;
    }
  }

  private void publishSendEvent(SendNotificationDTO sendNotificationDTO, PaymentEventRequestDTO eventRequestDTO) {
    SendNotification2DebtPositionSendNotificationsMapper.map(sendNotificationDTO).forEach(p ->
      publishSendNotificationPaymentEventActivity.publishSendNotificationEvent(p, eventRequestDTO));
  }

  private SendNotificationDTO waitDeliveryAcceptance(String sendNotificationId) {
    int attemptCounter = 0;
    SendNotificationDTO notification = null;
    Duration retryInterval = Duration.ofSeconds(30);

    while (attemptCounter < MAX_RETRIES) {
      attemptCounter++;

      notification = notificationStatusActivity.getSendNotificationStatus(sendNotificationId);

      if (notification != null && NotificationStatus.ACCEPTED.equals(notification.getStatus())) {
        log.info("Notification status is ACCEPTED for sendNotificationId {}", sendNotificationId);
        return notification;
      }

      log.info("Notification status not ACCEPTED, retry attempt {} for sendNotificationId {}, waiting {} seconds until next retry", attemptCounter, sendNotificationId, retryInterval);

      retryInterval = retryInterval.multipliedBy(2);
      Workflow.sleep(retryInterval);
    }

    throw new WorkflowInternalErrorException("Exceeded max retry attempts to wait for ACCEPTED status (attempts:" + attemptCounter + ") on sendNotificationId " + sendNotificationId + ". Last status was: " + (notification!=null?notification.getStatus():"null"));
  }
}
