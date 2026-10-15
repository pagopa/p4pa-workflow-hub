package it.gov.pagopa.pu.workflow.wf.pagopa.send.payments.wf;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.sendnotification.payments.NotifyPaymentActivity;
import it.gov.pagopa.payhub.activities.util.Utilities;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.sendnotification.dto.generated.*;
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
  private PublishSendTimelineEventActivity publishSendTimelineEventActivity;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    SendNotificationPaymentHandlerWFConfig sendNotificationPaymentHandlerWFConfig = applicationContext.getBean(SendNotificationPaymentHandlerWFConfig.class);

    publishSendTimelineEventActivity = sendNotificationPaymentHandlerWFConfig.buildPublishSendTimelineEventActivityStub();
    notifyPaymentActivity = sendNotificationPaymentHandlerWFConfig.buildNotifyPaymentActivityStub();
  }

  @Override
  public void notifyPayment(DebtPositionDTO debtPosition, Long receiptId) {
    String traceId = Utilities.getTraceId();

    SendNotificationDTO sendNotificationDTO = notifyPaymentActivity.notifyPayment(debtPosition, receiptId);

    if (isLastHistoryPaid(sendNotificationDTO)) {
      log.info("Publishing PU_PAYMENT event for receiptId {}", receiptId);

      ProgressResponseElementV28DTO progressResponseElement = mapSendNotificationDTOToProgressElement(sendNotificationDTO);

      publishSendTimelineEventActivity.publishSendTimelineEvent(progressResponseElement, sendNotificationDTO, null, traceId);
    }

    log.debug("Payment notification for receiptId {} completed", receiptId);
  }

  private static ProgressResponseElementV28DTO mapSendNotificationDTOToProgressElement(SendNotificationDTO sendNotificationDTO) {
    StreamEventSummaryDTO lastHistory = sendNotificationDTO.getHistory().getLast();

    TimelineElementV27DTO element = new TimelineElementV27DTO();
//    element.setEventTimestamp(receipt.getPaymentDate);
    element.setCategory(lastHistory.getTimelineElementCategory());

    ProgressResponseElementV28DTO progressResponseElement = new ProgressResponseElementV28DTO();
    progressResponseElement.setEventId(sendNotificationDTO.getSendNotificationId());
    progressResponseElement.setNotificationRequestId(sendNotificationDTO.getNotificationRequestId());
    progressResponseElement.setIun(sendNotificationDTO.getIun());
    progressResponseElement.setNewStatus(lastHistory.getNewNotificationStatus());
    progressResponseElement.setElement(element);

    return progressResponseElement;
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
