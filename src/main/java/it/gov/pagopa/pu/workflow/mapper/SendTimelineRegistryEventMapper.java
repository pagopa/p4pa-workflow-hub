package it.gov.pagopa.pu.workflow.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.pu.registries.dto.generated.RegistryEventSubType;
import it.gov.pagopa.pu.registries.dto.generated.RegistryOutcome;
import it.gov.pagopa.pu.sendnotification.dto.generated.NotificationStatusV26DTO;
import it.gov.pagopa.pu.sendnotification.dto.generated.ProgressResponseElementV28DTO;
import it.gov.pagopa.pu.workflow.event.registries.dto.RegistryEventSendTimelineDTO;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowInternalErrorException;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.wfsendnotification.SendNotificationStreamConsumeWF;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
public class SendTimelineRegistryEventMapper {

  public static final String REGISTRY_SEND = "REGISTRY_SEND";
  public static final String REGISTRY_ORIGIN = "workflow-hub";
  public static final String GRANTOR_ID = "SEND";

  private final ObjectMapper objectMapper;

  public SendTimelineRegistryEventMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public RegistryEventSendTimelineDTO mapSuccess(
    ProgressResponseElementV28DTO progressResponseElementV28DTO,
    String streamId,
    String traceId) {
    return map(progressResponseElementV28DTO, streamId, traceId, RegistryOutcome.OK);
  }

  public RegistryEventSendTimelineDTO mapError(
    ProgressResponseElementV28DTO progressResponseElementV28DTO,
    String streamId,
    String traceId) {
    return map(progressResponseElementV28DTO, streamId, traceId, RegistryOutcome.KO);
  }

  private RegistryEventSendTimelineDTO map(
    ProgressResponseElementV28DTO progressResponseElementV28DTO,
    String streamId,
    String traceId,
    RegistryOutcome outcome) {
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
      .requestorId(generateWorkflowId(streamId, SendNotificationStreamConsumeWF.class))
      .grantorId(GRANTOR_ID)
      .eventId(progressResponseElementV28DTO.getEventId())
      .notificationRequestId(progressResponseElementV28DTO.getNotificationRequestId())
      .iun(progressResponseElementV28DTO.getIun())
      .newStatus(Optional.ofNullable(progressResponseElementV28DTO.getNewStatus()).map(NotificationStatusV26DTO::name).orElse(null))
      .outcome(outcome)
      .body(serializeObjectToJson(progressResponseElementV28DTO.getElement()))
      .build();
  }

  private String serializeObjectToJson(Object object) {
    try {
      return objectMapper.writeValueAsString(object);
    } catch (Exception e) {
      log.error("Error serializing object to JSON", e);
      throw new WorkflowInternalErrorException("Error serializing object to JSON");
    }
  }

}
