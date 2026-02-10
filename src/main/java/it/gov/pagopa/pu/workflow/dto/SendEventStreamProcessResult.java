package it.gov.pagopa.pu.workflow.dto;

import lombok.Data;

@Data
public class SendEventStreamProcessResult {
  private String lastProcessedEventId;
  private int activityExecutionCount = 0;

  public void incrementProcessedEventCount() {
    activityExecutionCount += 1;
  }
}
