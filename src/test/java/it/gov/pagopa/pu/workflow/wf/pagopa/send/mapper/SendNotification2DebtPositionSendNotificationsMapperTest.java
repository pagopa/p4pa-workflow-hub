package it.gov.pagopa.pu.workflow.wf.pagopa.send.mapper;

import it.gov.pagopa.pu.sendnotification.dto.generated.SendNotificationDTO;
import it.gov.pagopa.pu.workflow.utils.faker.SendNotificationDTOFaker;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.dto.DebtPositionSendNotificationDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class SendNotification2DebtPositionSendNotificationsMapperTest {

  @Test
  void whenMapThenOk(){
    // Given
    SendNotificationDTO sendNotificationDTO = SendNotificationDTOFaker.buildSendNotificationDTO();

    // When
    List<DebtPositionSendNotificationDTO> result = SendNotification2DebtPositionSendNotificationsMapper.map(sendNotificationDTO);

    // Then
    Assertions.assertEquals(
      SendNotificationDTOFaker.buildListDebtPositionSendNotificationDTO(sendNotificationDTO),
      result
    );
  }
}
