package it.gov.pagopa.pu.workflow.utils.faker;

import it.gov.pagopa.pu.sendnotification.dto.generated.SendNotificationDTO;
import it.gov.pagopa.pu.sendnotification.dto.generated.SendNotificationPaymentsDTO;
import it.gov.pagopa.pu.workflow.utils.TestUtils;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.create.dto.DebtPositionSendNotificationDTO;

import java.time.OffsetDateTime;
import java.util.List;

public class SendNotificationDTOFaker {

  public static SendNotificationDTO buildSendNotificationDTO(){
    return TestUtils.getPodamFactory().manufacturePojo(SendNotificationDTO.class)
      .payments(List.of(
        new SendNotificationPaymentsDTO(1L, List.of("NAV1", "NAV2"), OffsetDateTime.now()),
        new SendNotificationPaymentsDTO(2L, List.of("NAV3"), OffsetDateTime.now())
      ));
  }

  public static List<DebtPositionSendNotificationDTO> buildListDebtPositionSendNotificationDTO(SendNotificationDTO sendNotificationDTO){
    return List.of(
      DebtPositionSendNotificationDTO.builder()
        .sendNotificationId(sendNotificationDTO.getSendNotificationId())
        .organizationId(sendNotificationDTO.getOrganizationId())
        .iun(sendNotificationDTO.getIun())
        .status(sendNotificationDTO.getStatus())
        .debtPositionId(1L)
        .noticeCodes(List.of("NAV1", "NAV2"))
        .build(),
      DebtPositionSendNotificationDTO.builder()
        .sendNotificationId(sendNotificationDTO.getSendNotificationId())
        .organizationId(sendNotificationDTO.getOrganizationId())
        .iun(sendNotificationDTO.getIun())
        .status(sendNotificationDTO.getStatus())
        .debtPositionId(2L)
        .noticeCodes(List.of("NAV3"))
        .build()
    );
  }

}
