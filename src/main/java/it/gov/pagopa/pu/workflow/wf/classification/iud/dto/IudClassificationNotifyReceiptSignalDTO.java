package it.gov.pagopa.pu.workflow.wf.classification.iud.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IudClassificationNotifyReceiptSignalDTO {
  private Long organizationId;
  private String iud;
  private String iuv;
  private String iur;
  private List<Integer> transferIndexes;
}
