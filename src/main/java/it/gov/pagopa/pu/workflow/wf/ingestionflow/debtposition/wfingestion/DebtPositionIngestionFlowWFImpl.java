package it.gov.pagopa.pu.workflow.wf.ingestionflow.debtposition.wfingestion;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.IngestionFlowFileProcessingLockerActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.debtposition.InstallmentIngestionFlowFileActivity;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.debtposition.config.DebtPositionIngestionFlowWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@Slf4j
@WorkflowImpl(taskQueues = DebtPositionIngestionFlowWFImpl.TASK_QUEUE_DEBT_POSITION_INGESTION_FLOW)
public class DebtPositionIngestionFlowWFImpl implements DebtPositionIngestionFlowWF, ApplicationContextAware {
  public static final String TASK_QUEUE_DEBT_POSITION_INGESTION_FLOW = "DebtPositionIngestionFlowWF";

  private IngestionFlowFileProcessingLockerActivity ingestionFlowFileProcessingLockerActivity;
  private InstallmentIngestionFlowFileActivity installmentIngestionFlowFileActivity;

  /**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link it.gov.pagopa.pu.workflow.config.TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    DebtPositionIngestionFlowWfConfig wfConfig = applicationContext.getBean(DebtPositionIngestionFlowWfConfig.class);

    ingestionFlowFileProcessingLockerActivity = wfConfig.buildIngestionFlowFileProcessingLockerActivityStub();
    installmentIngestionFlowFileActivity = wfConfig.buildInstallmentIngestionFlowFileActivityStub();
  }

  @Override
  public void ingest(Long ingestionFlowFileId) {
    log.info("Acquiring lock for ingestionFlowFileId {}", ingestionFlowFileId);

    boolean result = ingestionFlowFileProcessingLockerActivity.acquireProcessingLock(ingestionFlowFileId);
    log.info("Acquire Processing Lock result for ingestionFlowFileId {} is: {}", ingestionFlowFileId, result);

    if (result) {
      installmentIngestionFlowFileActivity.processFile(ingestionFlowFileId);
    }
  }
}
