package it.gov.pagopa.pu.workflow.wf.pagopa.send.mapper;

import it.gov.pagopa.pu.sendnotification.dto.generated.SendNotificationDTO;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.dto.DebtPositionSendNotificationDTO;

import java.util.List;

public class SendNotification2DebtPositionSendNotificationsMapper {
  private SendNotification2DebtPositionSendNotificationsMapper() {}

  public static List<DebtPositionSendNotificationDTO> map(SendNotificationDTO sendNotificationDTO) {
    return sendNotificationDTO.getPayments().stream()
      .map(p -> (DebtPositionSendNotificationDTO) DebtPositionSendNotificationDTO.builder()
        .organizationId(sendNotificationDTO.getOrganizationId())
        .debtPositionId(p.getDebtPositionId())
        .iun(sendNotificationDTO.getIun())
        .status(sendNotificationDTO.getStatus())
        .notificationDate(sendNotificationDTO.getNotificationDate())
        .noticeCodes(p.getNavList())
        .build()
      ).toList();
  }
}
