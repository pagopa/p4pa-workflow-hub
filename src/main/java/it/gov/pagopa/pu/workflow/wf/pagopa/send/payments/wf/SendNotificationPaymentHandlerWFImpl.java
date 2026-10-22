package it.gov.pagopa.pu.workflow.wf.pagopa.send.payments.wf;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.sendnotification.payments.NotifyPaymentActivity;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.sendnotification.dto.generated.*;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.payments.config.SendNotificationPaymentHandlerWFConfig;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.activity.PublishSendTimelineEventActivity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import  it.gov.pagopa.pu.sendnotification.dto.generated.SendNotificationDTO;

import java.util.Objects;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_SEND_RESERVED_PAYMENT_HANDLER)
public class SendNotificationPaymentHandlerWFImpl implements SendNotificationPaymentHandlerWF, ApplicationContextAware {

  private NotifyPaymentActivity notifyPaymentActivity;
  private PublishSendTimelineEventActivity publishSendTimelineEventActivity;

  /**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    SendNotificationPaymentHandlerWFConfig wfConfig = applicationContext.getBean(SendNotificationPaymentHandlerWFConfig.class);

    notifyPaymentActivity = wfConfig.buildNotifyPaymentActivityStub();
    publishSendTimelineEventActivity = wfConfig.buildPublishSendTimelineEventActivityStub();
  }

  @Override
  public void notifyPayment(DebtPositionDTO debtPosition, Long receiptId) {
    log.info("Start notifyPayment Workflow for payment having receiptId {}", receiptId);

    SendNotificationDTO sendNotificationDTO = notifyPaymentActivity.notifyPayment(debtPosition, receiptId);
    if (sendNotificationDTO == null) {
      log.debug("No SEND notification found for receiptId {}, skipping payment notification", receiptId);
      return;
    }

    if (hasPaidHistoryEvent(sendNotificationDTO)) {
      log.info("Publishing PU_PAYMENT event for receiptId {}", receiptId);
      publishSendTimelineEventActivity.publishPaymentTimelineEvent(sendNotificationDTO, receiptId);
    }

    log.info("Payment notification for receiptId {} completed", receiptId);
  }

  private boolean hasPaidHistoryEvent(SendNotificationDTO dto) {
    if (dto == null || dto.getHistory() == null || dto.getHistory().isEmpty()) {
      return false;
    }

    return dto.getHistory().stream()
      .filter(Objects::nonNull)
      .anyMatch(event ->
        event.getNewNotificationStatus() == NotificationStatusV26DTO.PU_PAID
          && event.getTimelineElementCategory() == TimelineElementCategoryV27DTO.PU_PAYMENT
      );
  }
}
