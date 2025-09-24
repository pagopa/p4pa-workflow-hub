package it.gov.pagopa.pu.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class IngestionDataDTO {
  long organizationId;
  long ingestionFlowFileId;
}
