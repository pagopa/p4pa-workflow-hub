package it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentnotification.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.classification.iud.IudClassificationWFClient;
import it.gov.pagopa.pu.workflow.wf.classification.iud.dto.IudClassificationNotifyPaymentNotificationSignalDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ActivityImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY_LOCAL)
public class NotifyPaymentNotificationToIudClassificationActivityImpl implements NotifyPaymentNotificationToIudClassificationActivity {
  private final IudClassificationWFClient iudClassificationWFClient;

  public NotifyPaymentNotificationToIudClassificationActivityImpl(IudClassificationWFClient iudClassificationWFClient) {
    this.iudClassificationWFClient = iudClassificationWFClient;
  }

  @Override
  public void signalPaymentNotificationIudClassificationWithStart(Long organizationId, String iud) {
    IudClassificationNotifyPaymentNotificationSignalDTO signalDTO =
      IudClassificationNotifyPaymentNotificationSignalDTO.builder()
        .organizationId(organizationId)
        .iud(iud)
        .build();
    iudClassificationWFClient.notifyPaymentNotification(signalDTO);
  }
}

