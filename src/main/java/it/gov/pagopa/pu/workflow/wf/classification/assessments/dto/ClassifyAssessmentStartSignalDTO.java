package it.gov.pagopa.pu.workflow.wf.classification.assessments.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClassifyAssessmentStartSignalDTO {
  private Long orgId;
  private String iuv;
  private String iud;
}
