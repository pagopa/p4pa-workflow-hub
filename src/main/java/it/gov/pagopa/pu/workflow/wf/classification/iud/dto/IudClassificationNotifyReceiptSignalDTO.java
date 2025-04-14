package it.gov.pagopa.pu.workflow.wf.classification.iud.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IudClassificationNotifyReceiptSignalDTO {
  private Long orgId;
  private String iud;
  private String iuv;
  private String iur;
  private int transferIndex;
}
