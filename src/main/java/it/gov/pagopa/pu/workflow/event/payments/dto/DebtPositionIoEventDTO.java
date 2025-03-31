package it.gov.pagopa.pu.workflow.event.payments.dto;

import it.gov.pagopa.payhub.activities.dto.debtposition.DebtPositionIoNotificationDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class DebtPositionIoEventDTO extends PaymentEventDTO<DebtPositionIoNotificationDTO> {
}
