package it.gov.pagopa.pu.workflow.wf.pagopa.send.payments.wf;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.debtposition.receipt.ReceiptRetrieverActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.payments.NotifyPaymentActivity;
import it.gov.pagopa.payhub.activities.util.Utilities;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
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

import java.util.List;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_SEND_RESERVED_PAYMENT_HANDLER)
public class SendNotificationPaymentHandlerWFImpl implements SendNotificationPaymentHandlerWF, ApplicationContextAware {

  private NotifyPaymentActivity notifyPaymentActivity;
  private ReceiptRetrieverActivity receiptRetrieverActivity;
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
    receiptRetrieverActivity = wfConfig.buildReceiptRetrieverActivityStub();
    publishSendTimelineEventActivity = wfConfig.buildPublishSendTimelineEventActivityStub();
  }

  @Override
  public void notifyPayment(DebtPositionDTO debtPosition, Long receiptId) {
    log.info("Start notifyPayment Workflow for payment having receiptId {}", receiptId);

    String traceId = Utilities.getTraceId();

    SendNotificationDTO sendNotificationDTO = notifyPaymentActivity.notifyPayment(debtPosition, receiptId);
    if (sendNotificationDTO == null) {
      log.debug("No SEND notification found for receiptId {}, skipping payment notification", receiptId);
      return;
    }

    if (isLastHistoryPaid(sendNotificationDTO)) {
      log.info("Publishing PU_PAYMENT event for receiptId {}", receiptId);

      ReceiptDTO receipt = receiptRetrieverActivity.retrieveReceipt(receiptId);
      publishSendTimelineEventActivity.publishPaymentTimelineEvent(sendNotificationDTO, receipt.getPaymentDateTime(), traceId);
    }

    log.info("Payment notification for receiptId {} completed", receiptId);
  }

  private boolean isLastHistoryPaid(SendNotificationDTO dto) {
    if (dto == null || dto.getHistory() == null || dto.getHistory().isEmpty()) {
      return false;
    }

    List<StreamEventSummaryDTO> history = dto.getHistory();
    StreamEventSummaryDTO last = history.getLast();

    return last != null
      && last.getNewNotificationStatus() == NotificationStatusV26DTO.PU_PAID
      && last.getTimelineElementCategory() == TimelineElementCategoryV27DTO.PU_PAYMENT;
  }
}
