package it.gov.pagopa.pu.workflow.wf.classification.iuf.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IufClassificationNotifyTreasurySignalDTO {
  // common fields
  private Long organizationId;
  private String iuf;

  // specific fields
  private String treasuryId;

}
