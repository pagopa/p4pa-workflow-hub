package it.gov.pagopa.pu.workflow.dto;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.model.executionconfig.WfExecutionConfig;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class SyncDebtPositionRequestDTO {
  @NotNull
  private DebtPositionDTO debtPosition;
  private WfExecutionConfig executionConfig;
}
