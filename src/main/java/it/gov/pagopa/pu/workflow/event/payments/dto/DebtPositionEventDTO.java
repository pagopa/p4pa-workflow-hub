package it.gov.pagopa.pu.workflow.event.payments.dto;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class DebtPositionEventDTO extends PaymentEventDTO<DebtPositionDTO> {
}
