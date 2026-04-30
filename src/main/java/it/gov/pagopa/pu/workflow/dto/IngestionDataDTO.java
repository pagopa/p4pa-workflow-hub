package it.gov.pagopa.pu.workflow.dto;

import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFileStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class IngestionDataDTO {
  private long organizationId;
  private long ingestionFlowFileId;
  private long totalRows;
  private long processedRows;
  private IngestionFlowFileStatus status;
  private long fileSize;
  private String operatorExternalUserId;
}
