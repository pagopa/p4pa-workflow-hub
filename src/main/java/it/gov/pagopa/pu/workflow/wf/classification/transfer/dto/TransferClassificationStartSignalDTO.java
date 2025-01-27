package it.gov.pagopa.pu.workflow.wf.classification.transfer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class TransferClassificationStartSignalDTO {
  private Long orgId;
  private String iuv;
  private String iur;
  private int transferIndex;
}
