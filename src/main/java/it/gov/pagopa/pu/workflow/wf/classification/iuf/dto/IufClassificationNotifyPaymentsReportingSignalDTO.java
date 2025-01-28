package it.gov.pagopa.pu.workflow.wf.classification.iuf.dto;


import it.gov.pagopa.payhub.activities.dto.classifications.PaymentsReportingTransferDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class IufClassificationNotifyPaymentsReportingSignalDTO {
  // common fields
  private String iuf;
  // specific fields
  private List<PaymentsReportingTransferDTO> transfers;

}
