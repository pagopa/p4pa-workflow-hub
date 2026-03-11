package it.gov.pagopa.pu.workflow.wf.pagopa.send.service;

import it.gov.pagopa.payhub.activities.activity.sendnotification.FetchSendLegalFactActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.SendNotificationDateRetrieveActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.UpdateSendNotificationStatusActivity;
import it.gov.pagopa.pu.sendnotification.dto.generated.LegalFactCategoryDTO;
import it.gov.pagopa.pu.sendnotification.dto.generated.ProgressResponseElementV28DTO;
import it.gov.pagopa.pu.sendnotification.dto.generated.SendNotificationDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.activity.PublishSendNotificationPaymentEventActivity;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.mapper.SendNotification2DebtPositionSendNotificationsMapper;
import lombok.extern.slf4j.Slf4j;

import static it.gov.pagopa.pu.workflow.utilities.Constants.LEGAL_FACT_ID_PREFIX;

@Slf4j
public class SendEventStreamProcessingServiceImpl implements SendEventStreamProcessingService {

  private final UpdateSendNotificationStatusActivity updateSendNotificationStatusActivity;
  private final SendNotificationDateRetrieveActivity sendNotificationDateRetrieveActivity;
  private final PublishSendNotificationPaymentEventActivity publishSendNotificationPaymentEventActivity;
  private final FetchSendLegalFactActivity fetchSendLegalFactActivity;

  public SendEventStreamProcessingServiceImpl(
    UpdateSendNotificationStatusActivity updateSendNotificationStatusActivity,
    SendNotificationDateRetrieveActivity sendNotificationDateRetrieveActivity,
    PublishSendNotificationPaymentEventActivity publishSendNotificationPaymentEventActivity,
    FetchSendLegalFactActivity fetchSendLegalFactActivity) {
    this.updateSendNotificationStatusActivity = updateSendNotificationStatusActivity;
    this.sendNotificationDateRetrieveActivity = sendNotificationDateRetrieveActivity;
    this.publishSendNotificationPaymentEventActivity = publishSendNotificationPaymentEventActivity;
    this.fetchSendLegalFactActivity = fetchSendLegalFactActivity;
  }

  @Override
  public String processSendStreamEvent(String sendStreamId, ProgressResponseElementV28DTO streamEvent) {
    String eventiId = processNotificationEvent(sendStreamId, streamEvent);
    downloadAndArchiveNotificationLegalFact(streamEvent);
    return eventiId;
  }

  private String processNotificationEvent(String sendStreamId, ProgressResponseElementV28DTO streamEvent) {
    return switch (streamEvent.getNewStatus()) {
      case ACCEPTED -> {
        SendNotificationDTO sendNotification = this.updateSendNotificationStatusActivity.updateSendNotificationStatus(streamEvent.getNotificationRequestId());
        this.publishSendEvent(sendNotification, new PaymentEventRequestDTO(PaymentEventType.SEND_NOTIFICATION_CREATED, null));
        yield streamEvent.getEventId();
      }
      case REFUSED -> {
        SendNotificationDTO sendNotification = this.updateSendNotificationStatusActivity.updateSendNotificationStatus(streamEvent.getNotificationRequestId());
        this.publishSendErrorEvent(sendNotification, new PaymentEventRequestDTO(PaymentEventType.SEND_NOTIFICATION_ERROR, null));
        yield streamEvent.getEventId();
      }
      case VIEWED -> {
        SendNotificationDTO sendNotification = this.sendNotificationDateRetrieveActivity.sendNotificationDateRetrieve(streamEvent.getNotificationRequestId());
        publishSendEvent(sendNotification, new PaymentEventRequestDTO(PaymentEventType.SEND_NOTIFICATION_DATE, null));
        yield streamEvent.getEventId();
      }
      case null -> {
        log.info("Skipping event with status 'null' for SEND stream with id {}", sendStreamId);
        yield streamEvent.getEventId();
      }
      default -> {
        log.info("Skipping event with status {} for SEND stream with id {}", streamEvent.getNewStatus(), sendStreamId);
        yield streamEvent.getEventId();
      }
    };
  }

  private void publishSendEvent(SendNotificationDTO sendNotificationDTO, PaymentEventRequestDTO eventRequestDTO) {
    SendNotification2DebtPositionSendNotificationsMapper.map(sendNotificationDTO)
      .forEach(p ->
        publishSendNotificationPaymentEventActivity.publishSendNotificationEvent(p, eventRequestDTO)
      );
  }

  private void publishSendErrorEvent(SendNotificationDTO sendNotificationDTO, PaymentEventRequestDTO eventRequestDTO) {
    SendNotification2DebtPositionSendNotificationsMapper.map(sendNotificationDTO)
      .forEach(p ->
        publishSendNotificationPaymentEventActivity.publishSendNotificationErrorEvent(p, eventRequestDTO)
      );
  }

  private void downloadAndArchiveNotificationLegalFact(ProgressResponseElementV28DTO streamEvent) {
    if(streamEvent.getElement().getLegalFactsIds() == null || streamEvent.getElement().getLegalFactsIds().isEmpty()) {
      return;
    }
    streamEvent.getElement()
      .getLegalFactsIds()
      .forEach(lf ->
        fetchSendLegalFactActivity.downloadAndArchiveSendLegalFact(
          streamEvent.getNotificationRequestId(),
          LegalFactCategoryDTO.valueOf(lf.getCategory()),
          lf.getKey().replace(LEGAL_FACT_ID_PREFIX, "")
        )
      );
  }

}
