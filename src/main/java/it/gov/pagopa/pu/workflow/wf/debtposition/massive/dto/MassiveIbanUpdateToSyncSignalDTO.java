package it.gov.pagopa.pu.workflow.wf.debtposition.massive.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MassiveIbanUpdateToSyncSignalDTO {
  private Long orgId;
  private Long dptoId;
  private String oldIban;
  private String newIban;
  private String oldPostalIban;
  private String newPostalIban;
}
