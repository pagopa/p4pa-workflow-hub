package it.gov.pagopa.pu.workflow.wf.receiptclassification.iuf.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class IufReceiptClassificationForTreasurySignalDTO   {
  public static final String  SIGNAL_METHOD_NAME="signalForTreasury";

  // common fields
  private Long organizationId;
  private String iuf;

  // specific fields
  private String treasuryId;

}
