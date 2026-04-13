package it.gov.pagopa.pu.workflow.wf.pagopa.send.create.wf;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.sendnotification.create.DeliveryNotificationActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.create.GetSendNotificationActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.create.PreloadSendFileActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.create.UploadSendFileActivity;
import it.gov.pagopa.payhub.activities.exception.sendnotification.SendNotificationConflictException;
import it.gov.pagopa.pu.sendnotification.dto.generated.SendNotificationDTO;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowInternalErrorException;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.create.activity.PublishSendNotificationPaymentEventActivity;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.create.config.SendNotificationProcessWfConfig;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.create.dto.DebtPositionSendNotificationDTO;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.create.mapper.SendNotification2DebtPositionSendNotificationsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_SEND_RESERVED_NOTIFICATION)
public class SendNotificationProcessWFImpl implements SendNotificationProcessWF, ApplicationContextAware {

  private PreloadSendFileActivity preloadSendFileActivity;
  private UploadSendFileActivity uploadSendFileActivity;
  private DeliveryNotificationActivity deliveryNotificationActivity;
  private GetSendNotificationActivity getSendNotificationActivity;
  private PublishSendNotificationPaymentEventActivity publishSendNotificationPaymentEventActivity;

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
    getSendNotificationActivity = wfConfig.buildGetSendNotificationActivityStub();
    publishSendNotificationPaymentEventActivity = wfConfig.buildPublishSendNotificationPaymentEventActivityStub();
  }

  @Override
  public void sendNotificationProcess(String sendNotificationId) {
    log.info("Start sendNotificationProcess Workflow for sendNotificationId {}", sendNotificationId);

    try {
      preloadSendFileActivity.preloadSendFile(sendNotificationId);
      uploadSendFileActivity.uploadSendFile(sendNotificationId);
      deliveryNotificationActivity.deliverySendNotification(sendNotificationId);
    } catch (SendNotificationConflictException e) {
      log.error("Conflict on delivery for sendNotificationId {}", sendNotificationId);
      throw new WorkflowInternalErrorException("[SEND_DELIVERY_CONFLICT] Workflow terminated during deliverySendNotification for sendNotificationId " + sendNotificationId);
    } catch (RuntimeException e) {
      SendNotificationDTO notification = getSendNotificationActivity.getSendNotification(sendNotificationId);
      if (notification != null && !notification.getPayments().isEmpty()) {
        for (DebtPositionSendNotificationDTO p : SendNotification2DebtPositionSendNotificationsMapper.map(notification)) {
          publishSendNotificationPaymentEventActivity.publishSendNotificationErrorEvent(p,
            new PaymentEventRequestDTO(PaymentEventType.SEND_NOTIFICATION_ERROR, Utilities.getWorkflowExceptionMessage(e)));
        }
      } else {
        log.info("Provided unknown sendNotificationId {}", sendNotificationId);
      }
      throw e;
    }
  }

}
