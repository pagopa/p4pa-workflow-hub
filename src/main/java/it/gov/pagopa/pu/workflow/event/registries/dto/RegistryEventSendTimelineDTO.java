package it.gov.pagopa.pu.workflow.event.registries.dto;

import it.gov.pagopa.pu.registries.dto.generated.RegistryEventSubType;
import it.gov.pagopa.pu.registries.dto.generated.RegistryOutcome;
import it.gov.pagopa.pu.sendnotification.dto.generated.TimelineElementCategoryV27DTO;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class RegistryEventSendTimelineDTO {
  @NotNull
  private String registryId;
  private String registryOrigin;
  @NotNull
  private String registryType;
  @NotNull
  private OffsetDateTime dateTime;
  @NotNull
  private String traceId;
  @NotNull
  private RegistryEventSubType eventSubType;
  @NotNull
  private String requestorId;
  @NotNull
  private String grantorId;

  @NotNull
  private Long organizationId;
  @NotNull
  private String streamId;
  @NotNull
  private String campaignId;
  @NotNull
  private String eventId;
  @NotNull
  private TimelineElementCategoryV27DTO eventType;
  @NotNull
  private String sendNotificationId;
  @NotNull
  private String notificationRequestId;
  private String iun;
  private Integer recipientIndex;
  private String newStatus;
  private OffsetDateTime eventTimestamp;
  private List<String> legalFactIds;

  @NotNull
  private RegistryOutcome outcome;
  private String body;
}
