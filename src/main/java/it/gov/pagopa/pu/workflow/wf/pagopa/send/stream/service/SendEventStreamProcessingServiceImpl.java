package it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.service;

import it.gov.pagopa.payhub.activities.activity.sendnotification.stream.processing.*;
import it.gov.pagopa.pu.sendnotification.dto.generated.LegalFactCategoryDTO;
import it.gov.pagopa.pu.sendnotification.dto.generated.NotificationStatus;
import it.gov.pagopa.pu.sendnotification.dto.generated.ProgressResponseElementV28DTO;
import it.gov.pagopa.pu.sendnotification.dto.generated.SendNotificationDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.create.activity.PublishSendNotificationPaymentEventActivity;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.create.mapper.SendNotification2DebtPositionSendNotificationsMapper;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.activity.StartDeleteSendLegalFactFileActivity;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.activity.StartDeleteSendNotificationFileActivity;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SendEventStreamProcessingServiceImpl implements SendEventStreamProcessingService {

  private final UpdateSendNotificationStatusActivity updateSendNotificationStatusActivity;
  private final ValidateSendNotificationStatusActivity validateSendNotificationStatusActivity;
  private final SendNotificationDateRetrieveActivity sendNotificationDateRetrieveActivity;
  private final PublishSendNotificationPaymentEventActivity publishSendNotificationPaymentEventActivity;
  private final FetchSendLegalFactActivity fetchSendLegalFactActivity;
  private final StartDeleteSendNotificationFileActivity startDeleteSendNotificationFileActivity;
  private final StartDeleteSendLegalFactFileActivity startDeleteSendLegalFactFileActivity;
  private final GetSendNotificationByNotificationRequestIdActivity getSendNotificationByNotificationRequestIdActivity;


  @SuppressWarnings("java:S107")
  public SendEventStreamProcessingServiceImpl(
    UpdateSendNotificationStatusActivity updateSendNotificationStatusActivity, ValidateSendNotificationStatusActivity validateSendNotificationStatusActivity,
    SendNotificationDateRetrieveActivity sendNotificationDateRetrieveActivity,
    PublishSendNotificationPaymentEventActivity publishSendNotificationPaymentEventActivity,
    FetchSendLegalFactActivity fetchSendLegalFactActivity,
    StartDeleteSendNotificationFileActivity startDeleteSendNotificationFileActivity,
    StartDeleteSendLegalFactFileActivity startDeleteSendLegalFactFileActivity, GetSendNotificationByNotificationRequestIdActivity getSendNotificationByNotificationRequestIdActivity) {
    this.updateSendNotificationStatusActivity = updateSendNotificationStatusActivity;
    this.validateSendNotificationStatusActivity = validateSendNotificationStatusActivity;
    this.sendNotificationDateRetrieveActivity = sendNotificationDateRetrieveActivity;
    this.publishSendNotificationPaymentEventActivity = publishSendNotificationPaymentEventActivity;
    this.fetchSendLegalFactActivity = fetchSendLegalFactActivity;
    this.startDeleteSendNotificationFileActivity = startDeleteSendNotificationFileActivity;
    this.startDeleteSendLegalFactFileActivity = startDeleteSendLegalFactFileActivity;
    this.getSendNotificationByNotificationRequestIdActivity = getSendNotificationByNotificationRequestIdActivity;
  }

  @Override
  public String processSendStreamEvent(String sendStreamId, ProgressResponseElementV28DTO streamEvent) {
    SendNotificationDTO sendNotification = this.getSendNotificationByNotificationRequestIdActivity
      .getSendNotificationByNotificationRequestId(streamEvent.getNotificationRequestId());
    String eventId = processNotificationEvent(sendStreamId, streamEvent, sendNotification);
    downloadAndArchiveNotificationLegalFact(streamEvent, sendNotification.getSendNotificationId());
    return eventId;
  }

  private String processNotificationEvent(String sendStreamId, ProgressResponseElementV28DTO streamEvent, SendNotificationDTO sendNotification) {
    if(streamEvent.getNewStatus()!=null && sendNotification.getStatus().getValue().equals(streamEvent.getNewStatus().getValue())) {
      return streamEvent.getEventId();
    }

    return switch (streamEvent.getNewStatus()) {
      case ACCEPTED -> {
        sendNotification = this.validateSendNotificationStatusActivity.validateSendNotificationStatus(streamEvent.getNotificationRequestId());
        this.publishSendEvent(sendNotification, new PaymentEventRequestDTO(PaymentEventType.SEND_NOTIFICATION_CREATED, null));
        yield streamEvent.getEventId();
      }
      case REFUSED -> {
        sendNotification = this.validateSendNotificationStatusActivity.validateSendNotificationStatus(streamEvent.getNotificationRequestId());
        this.publishSendErrorEvent(sendNotification, new PaymentEventRequestDTO(PaymentEventType.SEND_NOTIFICATION_ERROR, null));
        yield streamEvent.getEventId();
      }
      case DELIVERED -> {
        sendNotification = this.sendNotificationDateRetrieveActivity.sendNotificationDateRetrieve(streamEvent.getNotificationRequestId());
        this.updateSendNotificationStatusActivity.updateSendNotificationStatus(streamEvent.getNotificationRequestId(), NotificationStatus.DELIVERED);
        publishSendEvent(sendNotification, new PaymentEventRequestDTO(PaymentEventType.SEND_NOTIFICATION_DATE, null));
        startDeleteSendNotificationFileActivity.startDeleteSendNotificationExpiredFiles(sendNotification.getSendNotificationId());
        yield streamEvent.getEventId();
      }
      case UNREACHABLE -> {
        this.updateSendNotificationStatusActivity.updateSendNotificationStatus(streamEvent.getNotificationRequestId(),
          NotificationStatus.valueOf(streamEvent.getNewStatus().name()));
        startDeleteSendNotificationFileActivity.startDeleteSendNotificationExpiredFiles(sendNotification.getSendNotificationId());
        startDeleteSendLegalFactFileActivity.startDeleteSendLegalFactExpiredFiles(sendNotification.getSendNotificationId());
        yield streamEvent.getEventId();
      }
      case DELIVERING, VIEWED, EFFECTIVE_DATE, PAID, CANCELLED, RETURNED_TO_SENDER -> {
        this.updateSendNotificationStatusActivity.updateSendNotificationStatus(streamEvent.getNotificationRequestId(),
          NotificationStatus.valueOf(streamEvent.getNewStatus().name()));
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

  private void downloadAndArchiveNotificationLegalFact(ProgressResponseElementV28DTO streamEvent, String sendNotificationId) {
    if(streamEvent.getElement().getLegalFactsIds() == null || streamEvent.getElement().getLegalFactsIds().isEmpty()) {
      return;
    }
    streamEvent.getElement()
      .getLegalFactsIds()
      .forEach(lf -> {
        fetchSendLegalFactActivity.downloadAndArchiveSendLegalFact(
          streamEvent.getNotificationRequestId(),
          LegalFactCategoryDTO.valueOf(lf.getCategory()),
          Utilities.extractPolishedLegalFactId(lf)
        );
        startDeleteSendLegalFactFileActivity.startDeleteSendLegalFactExpiredFiles(sendNotificationId);
      }
    );
  }

}
