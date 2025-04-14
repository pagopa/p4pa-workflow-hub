package it.gov.pagopa.pu.workflow.wf.classification.iud.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IudClassificationNotifyPaymentNotificationSignalDTO {
  private String iud;
  private Long organizationId;
}
