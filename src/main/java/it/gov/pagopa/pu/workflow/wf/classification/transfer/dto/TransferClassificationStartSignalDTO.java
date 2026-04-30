package it.gov.pagopa.pu.workflow.wf.classification.transfer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransferClassificationStartSignalDTO {
  private Long orgId;
  private String iuv;
  private String iur;
  private int transferIndex;
}
