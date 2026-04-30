package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.pu.workflow.config.temporal.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.activity.NotifyTreasuryToIufClassificationActivity;

public abstract class TreasuryIngestionWfConfig extends BaseWfConfig {

  public NotifyTreasuryToIufClassificationActivity buildNotifyTreasuryToIufClassificationActivityStub() {
    return Workflow.newActivityStub(NotifyTreasuryToIufClassificationActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(
      TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY_LOCAL,
      this));
  }
}
