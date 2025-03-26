package it.gov.pagopa.pu.workflow.event.payments.dto;

import it.gov.pagopa.pu.workflow.wf.pagopa.send.dto.DebtPositionSendNotificationDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class DebtPositionSendEventDTO extends PaymentEventDTO<DebtPositionSendNotificationDTO> {
}
