package it.gov.pagopa.pu.workflow.dto;

import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.FineWfExecutionConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Data
public class FineReductionExpirationRequestDTO {

  private PaymentEventRequestDTO paymentEventRequest;
  private FineWfExecutionConfig executionParams;
}
