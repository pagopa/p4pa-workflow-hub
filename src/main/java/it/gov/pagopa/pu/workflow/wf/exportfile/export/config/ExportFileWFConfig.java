package it.gov.pagopa.pu.workflow.wf.exportfile.export.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.exportflow.ExportFileActivity;
import it.gov.pagopa.payhub.activities.activity.exportflow.UpdateExportFileStatusActivity;
import it.gov.pagopa.payhub.activities.activity.exportflow.email.SendEmailExportFileActivity;
import it.gov.pagopa.pu.workflow.config.temporal.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.exportfile.export.activity.ScheduleExportFileExpirationActivity;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.export-file")
public class ExportFileWFConfig extends BaseWfConfig {

  public ExportFileActivity buildExportFileActivityStub() {
    return Workflow.newActivityStub(ExportFileActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public UpdateExportFileStatusActivity buildUpdateExportFileStatusActivityStub() {
    return Workflow.newActivityStub(UpdateExportFileStatusActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public SendEmailExportFileActivity buildSendEmailExportFileActivityStub() {
    return Workflow.newActivityStub(SendEmailExportFileActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public ScheduleExportFileExpirationActivity buildScheduleExportFileExpirationActivityStub() {
    return Workflow.newActivityStub(ScheduleExportFileExpirationActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(
      TaskQueueConstants.TASK_QUEUE_EXPORT_MEDIUM_PRIORITY_LOCAL,
      this));
  }
}
