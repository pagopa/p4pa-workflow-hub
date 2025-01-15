package it.gov.pagopa.pu.workflow.wf.classification.iuf.dto;


import it.gov.pagopa.payhub.activities.dto.classifications.Transfer2ClassifyDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class IufReceiptClassificationForReportingSignalDTO {
  public static final String SIGNAL_METHOD_NAME = "signalForReporting";

  // common fields
  private Long organizationId;
  private String iuf;

  // specific fields
  private String outcomeCode;
  private List<Transfer2ClassifyDTO> transfers2classify;

}
