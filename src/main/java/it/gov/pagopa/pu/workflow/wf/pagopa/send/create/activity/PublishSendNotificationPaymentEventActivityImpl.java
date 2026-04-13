package it.gov.pagopa.pu.workflow.wf.pagopa.send.create.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.event.payments.producer.PaymentsProducerService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.create.dto.DebtPositionSendNotificationDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ActivityImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_SEND_RESERVED_PUBLISH_EVENT_LOCAL)
public class PublishSendNotificationPaymentEventActivityImpl implements PublishSendNotificationPaymentEventActivity {

  private final PaymentsProducerService paymentsProducerService;

  public PublishSendNotificationPaymentEventActivityImpl(PaymentsProducerService paymentsProducerService) {
    this.paymentsProducerService = paymentsProducerService;
  }

  @Override
  public void publishSendNotificationEvent(DebtPositionSendNotificationDTO sendNotification, PaymentEventRequestDTO sendEventRequest) {
    log.info("Publishing SendNotification event {} (IUN {}) on debtPosition: {} (NAV: {})", sendEventRequest.getPaymentEventType(), sendNotification.getIun(), sendNotification.getDebtPositionId(), sendNotification.getNoticeCodes());
    paymentsProducerService.notifyDebtPositionSendEvent(sendNotification, sendEventRequest);
  }

  @Override
  public void publishSendNotificationErrorEvent(DebtPositionSendNotificationDTO sendNotification, PaymentEventRequestDTO sendEventRequest) {
    log.info("Publishing SendNotification error event {} (IUN {}) on debtPosition {} (NAV: {})", sendEventRequest.getPaymentEventType(), sendNotification.getIun(), sendNotification.getDebtPositionId(), sendNotification.getNoticeCodes());
    paymentsProducerService.notifyDebtPositionSendEvent(sendNotification, sendEventRequest);
  }
}
