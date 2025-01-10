package it.gov.pagopa.pu.workflow.wf.receiptclassification.iuf;


import it.gov.pagopa.payhub.activities.dto.classifications.Transfer2ClassifyDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class IufReceiptClassificationSignalDTO {
  public static final String  SIGNAL_METHOD_NAME="setSignalDTO";

  private IufReceiptClassificationSignalType type;

  // common fields
  private Long organizationId;
  private String iuf;

  // specific fields for treasury
  private Long treasuryId;

  // specific fields for reporting
  private String outcomeCode;
  private List<Transfer2ClassifyDTO> transfers2classify;

}
