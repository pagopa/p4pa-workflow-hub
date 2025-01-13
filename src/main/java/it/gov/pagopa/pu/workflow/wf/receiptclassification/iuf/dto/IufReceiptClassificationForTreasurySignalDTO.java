package it.gov.pagopa.pu.workflow.wf.receiptclassification.iuf.dto;


import it.gov.pagopa.payhub.activities.dto.classifications.Transfer2ClassifyDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class IufReceiptClassificationForTreasurySignalDTO   {
  public static final String  SIGNAL_METHOD_NAME="signalForTreasury";

  // common fields
  private Long organizationId;
  private String iuf;

  // specific fields
  private Long treasuryId;

}
