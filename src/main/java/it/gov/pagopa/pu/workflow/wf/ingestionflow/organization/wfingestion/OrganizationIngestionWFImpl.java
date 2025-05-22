package it.gov.pagopa.pu.workflow.wf.ingestionflow.organization.wfingestion;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.UpdateIngestionFlowStatusActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.email.SendEmailIngestionFlowActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.organization.OrganizationIngestionActivity;
import it.gov.pagopa.payhub.activities.dto.ingestion.organization.OrganizationIngestionFlowFileResult;
import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFileStatus;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.organization.config.OrganizationIngestionWFConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@Slf4j
@WorkflowImpl(taskQueues = OrganizationIngestionWFImpl.TASK_QUEUE_ORGANIZATION_INGESTION_WF)
public class OrganizationIngestionWFImpl implements OrganizationIngestionWF, ApplicationContextAware {
  public static final String TASK_QUEUE_ORGANIZATION_INGESTION_WF = "OrganizationIngestionWF";

  private OrganizationIngestionActivity organizationIngestionActivity;
  private SendEmailIngestionFlowActivity sendEmailIngestionFlowActivity;
  private UpdateIngestionFlowStatusActivity updateIngestionFlowStatusActivity;

  /**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    OrganizationIngestionWFConfig wfConfig = applicationContext.getBean(OrganizationIngestionWFConfig.class);

    organizationIngestionActivity = wfConfig.buildOrganizationIngestionActivityStub();
    sendEmailIngestionFlowActivity = wfConfig.buildSendEmailIngestionFlowActivityStub();
    updateIngestionFlowStatusActivity = wfConfig.buildUpdateIngestionFlowStatusActivityStub();
  }


  @Override
  public void ingest(Long ingestionFlowFileId) {
    log.info("Handling Organization IngestingFlowFileId {}", ingestionFlowFileId);

    updateIngestionFlowStatusActivity.updateStatus(ingestionFlowFileId, IngestionFlowFileStatus.UPLOADED, IngestionFlowFileStatus.PROCESSING, null);
    OrganizationIngestionFlowFileResult ingestionFlowFileResult = processFile(ingestionFlowFileId);

    boolean success = ingestionFlowFileResult.getErrorDescription() == null;
    updateIngestionFlowStatusActivity.updateStatus(ingestionFlowFileId,
      IngestionFlowFileStatus.PROCESSING,
      success
        ? IngestionFlowFileStatus.COMPLETED
        : IngestionFlowFileStatus.ERROR,
      ingestionFlowFileResult);
    sendEmailIngestionFlowActivity.sendEmail(ingestionFlowFileId, success);

    log.info("Organization Ingestion completed for file with ID {} with success {} and errorDescription {}",
      ingestionFlowFileId, success, ingestionFlowFileResult.getErrorDescription());
  }

  private OrganizationIngestionFlowFileResult processFile(Long ingestionFlowFileId) {
    try {
      return organizationIngestionActivity.processFile(ingestionFlowFileId);
    } catch (Exception e) {
      OrganizationIngestionFlowFileResult result = new OrganizationIngestionFlowFileResult();
      result.setErrorDescription(Utilities.getWorkflowExceptionMessage(e));
      return result;
    }
  }


}
