package it.gov.pagopa.pu.workflow.dto;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.model.executionconfig.WfExecutionConfig;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SyncDebtPositionRequestDTO {
  @NotNull
  private DebtPositionDTO debtPosition;
  private WfExecutionConfig executionConfig;
}
