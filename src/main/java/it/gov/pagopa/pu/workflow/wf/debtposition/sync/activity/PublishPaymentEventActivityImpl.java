package it.gov.pagopa.pu.workflow.wf.debtposition.sync.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.payhub.activities.dto.debtposition.DebtPositionIoNotificationDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.event.payments.producer.PaymentsProducerService;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.config.SynchronizeDebtPositionWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ActivityImpl(taskQueues = SynchronizeDebtPositionWfConfig.TASK_QUEUE_SYNCHRONIZE_DP_LOCAL_ACTIVITY)
public class PublishPaymentEventActivityImpl implements PublishPaymentEventActivity {

  private final PaymentsProducerService paymentsProducerService;

  public PublishPaymentEventActivityImpl(PaymentsProducerService paymentsProducerService) {
    this.paymentsProducerService = paymentsProducerService;
  }

  @Override
  public void publishDebtPositionEvent(DebtPositionDTO debtPositionDTO, PaymentEventRequestDTO paymentEventRequest) {
    log.info("Publishing event {} on debtPosition: {}", paymentEventRequest.getPaymentEventType(), debtPositionDTO.getDebtPositionId());
    paymentsProducerService.notifyDebtPositionPaymentsEvent(debtPositionDTO, paymentEventRequest);
  }

  @Override
  public void publishDebtPositionErrorEvent(DebtPositionDTO debtPositionDTO, PaymentEventRequestDTO paymentEventRequest) {
    log.info("Publishing error event {} on debtPosition: {}", paymentEventRequest.getPaymentEventType(), debtPositionDTO.getDebtPositionId());
    paymentsProducerService.notifyDebtPositionPaymentsEvent(debtPositionDTO, paymentEventRequest);
  }

  @Override
  public void publishDebtPositionIoNotificationEvent(DebtPositionIoNotificationDTO ioNotification, PaymentEventRequestDTO paymentEventRequest) {
    log.info("Publishing IO Notification event {} on debtPosition: {}", paymentEventRequest.getPaymentEventType(), ioNotification.getDebtPositionId());
    paymentsProducerService.notifyDebtPositionIoEvent(ioNotification, paymentEventRequest);
  }
}
