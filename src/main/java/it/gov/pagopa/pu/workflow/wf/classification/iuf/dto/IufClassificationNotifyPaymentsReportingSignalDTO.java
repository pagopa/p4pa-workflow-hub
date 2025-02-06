package it.gov.pagopa.pu.workflow.wf.classification.iuf.dto;


import it.gov.pagopa.payhub.activities.dto.classifications.PaymentsReportingTransferDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IufClassificationNotifyPaymentsReportingSignalDTO {
  // common fields
  private Long organizationId;
  private String iuf;
  // specific fields
  private List<PaymentsReportingTransferDTO> transfers;

}
