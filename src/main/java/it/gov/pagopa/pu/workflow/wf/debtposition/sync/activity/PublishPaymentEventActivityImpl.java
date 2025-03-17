package it.gov.pagopa.pu.workflow.wf.debtposition.sync.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
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
  public void publishErrorEvent(DebtPositionDTO debtPositionDTO, PaymentEventType paymentEventType, String paymentEventDescription) {
    log.info("Publishing event {} on debtPosition: {}", paymentEventType, debtPositionDTO.getDebtPositionId());
    paymentsProducerService.notifyPaymentsEvent(debtPositionDTO, paymentEventType, paymentEventDescription);
  }
}
