package it.gov.pagopa.pu.workflow.wf.pagopa.send.dto;

import it.gov.pagopa.pu.sendnotification.dto.generated.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@SuperBuilder
public class DebtPositionSendNotificationDTO {
  private String sendNotificationId;
  private Long debtPositionId;
  private Long organizationId;
  private String iun;
  private NotificationStatus status;
  private List<String> noticeCodes;
}
