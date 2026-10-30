package it.gov.pagopa.pu.workflow.mapper;

import it.gov.pagopa.payhub.activities.connector.debtposition.ReceiptService;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.sendnotification.dto.generated.*;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import tools.jackson.databind.json.JsonMapper;
import it.gov.pagopa.pu.registries.dto.generated.RegistryEventSubType;
import it.gov.pagopa.pu.registries.dto.generated.RegistryOutcome;
import it.gov.pagopa.pu.workflow.event.registries.dto.RegistryEventSendTimelineDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class SendTimelineRegistryEventMapper {

  public static final String REGISTRY_SEND = "REGISTRY_SEND";
  public static final String REGISTRY_ORIGIN = "workflow-hub";
  public static final String REQUESTOR_ID = "SEND";
  public static final String REQUESTOR_ID_PAYMENT = "NodoDeiPagamentiSPC";

  private final JsonMapper jsonMapper;
  private final ReceiptService receiptService;

  public SendTimelineRegistryEventMapper(JsonMapper jsonMapper, ReceiptService receiptService) {
    this.jsonMapper = jsonMapper;
    this.receiptService = receiptService;
  }

  public RegistryEventSendTimelineDTO mapSuccess(
    ProgressResponseElementV28DTO progressResponseElementV28DTO,
    SendNotificationDTO sendNotificationDTO,
    String streamId,
    String workflowId,
    String traceId) {
    return map(progressResponseElementV28DTO, sendNotificationDTO, streamId, workflowId, traceId, RegistryOutcome.OK, null);
  }

  public RegistryEventSendTimelineDTO mapError(
    ProgressResponseElementV28DTO progressResponseElementV28DTO,
    SendNotificationDTO sendNotificationDTO,
    String streamId,
    String workflowId,
    String traceId) {
    return map(progressResponseElementV28DTO, sendNotificationDTO, streamId, workflowId, traceId, RegistryOutcome.KO, progressResponseElementV28DTO.getElement().getDetails()!=null? progressResponseElementV28DTO.getElement().getDetails().getRefusalReasons() : null);
  }

  private RegistryEventSendTimelineDTO map(
    ProgressResponseElementV28DTO progressResponseElementV28DTO,
    SendNotificationDTO sendNotificationDTO,
    String streamId,
    String workflowId,
    String traceId,
    RegistryOutcome outcome,
    List<NotificationRefusedErrorV27DTO> errors) {
    String registryId = String.join(
      "-",
      streamId,
      progressResponseElementV28DTO.getEventId()
    );
    return RegistryEventSendTimelineDTO.builder()
      .registryId(registryId)
      .registryOrigin(REGISTRY_ORIGIN)
      .registryType(REGISTRY_SEND)
      .dateTime(OffsetDateTime.now())
      .traceId(traceId)
      .eventSubType(RegistryEventSubType.RESP)
      .requestorId(REQUESTOR_ID)
      .grantorId(workflowId)
      .organizationId(sendNotificationDTO.getOrganizationId())
      .streamId(streamId)
      .campaignId(sendNotificationDTO.getCampaignId())
      .eventId(progressResponseElementV28DTO.getEventId())
      .eventType(progressResponseElementV28DTO.getElement().getCategory())
      .sendNotificationId(sendNotificationDTO.getSendNotificationId())
      .notificationRequestId(progressResponseElementV28DTO.getNotificationRequestId())
      .iun(progressResponseElementV28DTO.getIun())
      .recipientIndex(extractRecipientIndex(progressResponseElementV28DTO))
      .newStatus(Optional.ofNullable(progressResponseElementV28DTO.getNewStatus()).map(NotificationStatusV26DTO::name).orElse(null))
      .eventTimestamp(progressResponseElementV28DTO.getElement().getEventTimestamp())
      .legalFactIds(
        Optional.ofNullable(progressResponseElementV28DTO.getElement().getLegalFactsIds())
          .map(Utilities::extractPolishedLegalFactIds)
          .orElse(null)
      )
      .errors(errors)
      .outcome(outcome)
      .body(
        serializeObjectToJson(
          progressResponseElementV28DTO.getElement(),
          streamId,
          progressResponseElementV28DTO.getEventId()
        )
      )
      .build();
  }

  private static Integer extractRecipientIndex(ProgressResponseElementV28DTO progressResponseElementV28DTO) {
    return Optional.ofNullable(progressResponseElementV28DTO.getElement().getDetails())
      .map(TimelineElementDetailsV27DTO::getRecIndex)
      .orElse(0);
  }

  private String serializeObjectToJson(Object object, String streamId, String eventId) {
    try {
      return jsonMapper.writeValueAsString(object);
    } catch (Exception e) {
      String warnMessage = "Error in serializing object to JSON, for event %s from stream %s".formatted(eventId, streamId);
      log.warn(warnMessage, e);
      return null;
    }
  }

  public RegistryEventSendTimelineDTO mapPayment(SendNotificationDTO sendNotificationDTO, Long receiptId, String workflowId) {
    ReceiptDTO receipt = receiptService.getByReceiptId(receiptId);
    return RegistryEventSendTimelineDTO.builder()
      .registryId(UUID.randomUUID().toString())
      .registryOrigin(REGISTRY_ORIGIN)
      .registryType(REGISTRY_SEND)
      .dateTime(OffsetDateTime.now())
      .traceId(it.gov.pagopa.payhub.activities.util.Utilities.getTraceId())
      .eventSubType(RegistryEventSubType.RESP)
      .requestorId(REQUESTOR_ID_PAYMENT)
      .grantorId(workflowId)
      .organizationId(sendNotificationDTO.getOrganizationId())
      .streamId(String.valueOf(sendNotificationDTO.getOrganizationId()))
      .eventId(sendNotificationDTO.getSendNotificationId())
      .eventType(TimelineElementCategoryV27DTO.PU_PAYMENT)
      .sendNotificationId(sendNotificationDTO.getSendNotificationId())
      .notificationRequestId(sendNotificationDTO.getNotificationRequestId())
      .iun(sendNotificationDTO.getIun())
      .recipientIndex(null)
      .newStatus(NotificationStatusV26DTO.PU_PAID.name())
      .eventTimestamp(receipt.getPaymentDateTime())
      .legalFactIds(null)
      .outcome(RegistryOutcome.OK)
      .body("INTERNAL_EVENT") // The body cannot be null because it will be encrypted and a null value will cause a NullPointerException
      .build();
  }
}
