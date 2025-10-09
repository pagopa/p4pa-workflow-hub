package it.gov.pagopa.pu.workflow.dto;

import it.gov.pagopa.pu.processexecutions.dto.generated.ExportFile.ExportFileTypeEnum;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ExportDataDTO {
  private Long organizationId;
  private Long exportFileId;
  private Long exportedRows;
  private LocalDate exportDate;
  private Long fileSize;
  private ExportFileTypeEnum exportFileType;
}
