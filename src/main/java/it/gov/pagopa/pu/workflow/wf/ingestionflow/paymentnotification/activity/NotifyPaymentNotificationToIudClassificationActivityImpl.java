package it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentnotification.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.workflow.wf.classification.iud.IudClassificationWFClient;
import it.gov.pagopa.pu.workflow.wf.classification.iud.dto.IudClassificationNotifyPaymentNotificationSignalDTO;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentnotification.wfingestion.PaymentNotificationIngestionWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ActivityImpl(taskQueues = PaymentNotificationIngestionWFImpl.TASK_QUEUE_PAYMENT_NOTIFICATION_INGESTION_LOCAL_ACTIVITY)
public class NotifyPaymentNotificationToIudClassificationActivityImpl implements NotifyPaymentNotificationToIudClassificationActivity {
  private final IudClassificationWFClient iudClassificationWFClient;

  public NotifyPaymentNotificationToIudClassificationActivityImpl(IudClassificationWFClient iudClassificationWFClient) {
    this.iudClassificationWFClient = iudClassificationWFClient;
  }

  @Override
  public void signalIudClassificationWithStart(Long organizationId, String iud) {
    IudClassificationNotifyPaymentNotificationSignalDTO signalDTO =
      IudClassificationNotifyPaymentNotificationSignalDTO.builder()
        .organizationId(organizationId)
        .iud(iud)
        .build();
    iudClassificationWFClient.notifyPaymentNotification(signalDTO);
  }
}

