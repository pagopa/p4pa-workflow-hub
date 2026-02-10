package it.gov.pagopa.pu.workflow.service.wf.send;

import it.gov.pagopa.payhub.activities.activity.sendnotification.SendNotificationDateRetrieveActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.UpdateSendNotificationStatusActivity;
import it.gov.pagopa.pu.sendnotification.dto.generated.NotificationStatus;
import it.gov.pagopa.pu.sendnotification.dto.generated.ProgressResponseElementV25DTO;
import it.gov.pagopa.pu.sendnotification.dto.generated.SendNotificationDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.dto.SendEventStreamProcessResult;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.activity.PublishSendNotificationPaymentEventActivity;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.config.SendNotificationProcessWfConfig;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.mapper.SendNotification2DebtPositionSendNotificationsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class SendEventStreamProcessingServiceImpl implements SendEventStreamProcessingService {

  private final UpdateSendNotificationStatusActivity updateSendNotificationStatusActivity;
  private final SendNotificationDateRetrieveActivity sendNotificationDateRetrieveActivity;
  private final PublishSendNotificationPaymentEventActivity publishSendNotificationPaymentEventActivity;

  public SendEventStreamProcessingServiceImpl(
    UpdateSendNotificationStatusActivity updateSendNotificationStatusActivity,
    SendNotificationDateRetrieveActivity sendNotificationDateRetrieveActivity,
    PublishSendNotificationPaymentEventActivity publishSendNotificationPaymentEventActivity) {
    this.updateSendNotificationStatusActivity = updateSendNotificationStatusActivity;
    this.sendNotificationDateRetrieveActivity = sendNotificationDateRetrieveActivity;
    this.publishSendNotificationPaymentEventActivity = publishSendNotificationPaymentEventActivity;
  }

  @Override
  public SendEventStreamProcessResult processSendStreamEvent(String sendStreamId, ProgressResponseElementV25DTO streamEvent) {
    SendEventStreamProcessResult result = new SendEventStreamProcessResult();
    return switch (streamEvent.getNewStatus()) {
      case ACCEPTED -> {
        result.incrementProcessedEventCount();
        SendNotificationDTO sendNotification = this.updateSendNotificationStatusActivity.updateSendNotificationStatus(streamEvent.getNotificationRequestId());
        this.publishSendEvent(sendNotification, new PaymentEventRequestDTO(PaymentEventType.SEND_NOTIFICATION_CREATED, null), result);
        result.setLastProcessedEventId(streamEvent.getEventId());
        yield result;
      }
      case REFUSED -> {
        result.incrementProcessedEventCount();
        SendNotificationDTO sendNotification = this.updateSendNotificationStatusActivity.updateSendNotificationStatus(streamEvent.getNotificationRequestId());
        this.publishSendErrorEvent(sendNotification, new PaymentEventRequestDTO(PaymentEventType.SEND_NOTIFICATION_ERROR, null), result);
        result.setLastProcessedEventId(streamEvent.getEventId());
        yield result;
      }
      case VIEWED -> {
        result.incrementProcessedEventCount();
        this.sendNotificationDateRetrieveActivity.sendNotificationDateRetrieve(streamEvent.getNotificationRequestId());
        result.setLastProcessedEventId(streamEvent.getEventId());
        yield result;
      }
      case null -> {
        log.warn("Skipping event with status 'null' for SEND stream with id {}", sendStreamId);
        yield null;
      }
      default -> {
        log.warn("Skipping event with status {} for SEND stream with id {}", streamEvent.getNewStatus(), sendStreamId);
        yield null;
      }
    };
  }

  private void publishSendEvent(SendNotificationDTO sendNotificationDTO, PaymentEventRequestDTO eventRequestDTO, SendEventStreamProcessResult result) {
    SendNotification2DebtPositionSendNotificationsMapper.map(sendNotificationDTO)
      .forEach(p -> {
        result.incrementProcessedEventCount();
        publishSendNotificationPaymentEventActivity.publishSendNotificationEvent(p, eventRequestDTO);
      });
  }

  private void publishSendErrorEvent(SendNotificationDTO sendNotificationDTO, PaymentEventRequestDTO eventRequestDTO, SendEventStreamProcessResult result) {
    SendNotification2DebtPositionSendNotificationsMapper.map(sendNotificationDTO)
      .forEach(p -> {
        result.incrementProcessedEventCount();
        publishSendNotificationPaymentEventActivity.publishSendNotificationErrorEvent(p, eventRequestDTO);
      });
  }

}
