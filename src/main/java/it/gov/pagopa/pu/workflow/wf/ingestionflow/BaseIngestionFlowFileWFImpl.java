package it.gov.pagopa.pu.workflow.wf.ingestionflow;

import it.gov.pagopa.payhub.activities.activity.ingestionflow.UpdateIngestionFlowStatusActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.email.SendEmailIngestionFlowActivity;
import it.gov.pagopa.payhub.activities.dto.ingestion.IngestionFlowFileResult;
import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFileStatus;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.config.BaseIngestionFlowFileWFConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.function.Function;

@Slf4j
public abstract class BaseIngestionFlowFileWFImpl<T extends IngestionFlowFileResult> implements BaseIngestionFlowFileWF, ApplicationContextAware {

  private Function<Long, T> ingestionFlowFileProcessorActivity;
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
    BaseIngestionFlowFileWFConfig wfConfig = applicationContext.getBean(BaseIngestionFlowFileWFConfig.class);

    sendEmailIngestionFlowActivity = wfConfig.buildSendEmailIngestionFlowActivityStub();
    updateIngestionFlowStatusActivity = wfConfig.buildUpdateIngestionFlowStatusActivityStub();

    ingestionFlowFileProcessorActivity = buildActivityStubs(applicationContext);
  }

  /** To be overridden by extended class in order to build further required activities */
  protected abstract Function<Long, T> buildActivityStubs(ApplicationContext applicationContext);

  @Override
  public void ingest(Long ingestionFlowFileId) {
    log.info("Starting {} on IngestingFlowFileId {}", getClass().getSimpleName(), ingestionFlowFileId);

    setProcessingStatus(ingestionFlowFileId);
    IngestionFlowFileResult result = processFile(ingestionFlowFileId);
    finallyAfterProcessing(ingestionFlowFileId, result);
    boolean success = finalizeStatus(ingestionFlowFileId, result);
    sendEmail(ingestionFlowFileId, success);

    log.info("{} ingestion completed for file with ID {} with success {} and errorDescription {}",
      getClass().getSimpleName(), ingestionFlowFileId, success, result.getErrorDescription());
  }

  /** To act after processing if the activity has not thrown any exception */
  protected void afterProcessing(Long ingestionFlowFileId, T result){
    // Do Nothing
  }

  /** To act finally after processing. If the processing activity has not thrown an exception, the <i>result</i> parameter would be of {@link T} type */
  protected void finallyAfterProcessing(Long ingestionFlowFileId, IngestionFlowFileResult result){
    // Do Nothing
  }

  private IngestionFlowFileResult processFile(Long ingestionFlowFileId) {
    IngestionFlowFileResult out = null;
    try {
      T result = ingestionFlowFileProcessorActivity.apply(ingestionFlowFileId);
      out = result;
      afterProcessing(ingestionFlowFileId, result);
      return out;
    } catch (Exception e) {
      if(out == null){
        out = new IngestionFlowFileResult();
      }
      out.setErrorDescription(Utilities.getWorkflowExceptionMessage(e));
      return out;
    }
  }

  protected void setProcessingStatus(Long ingestionFlowFileId) {
    updateIngestionFlowStatusActivity.updateStatus(ingestionFlowFileId, IngestionFlowFileStatus.UPLOADED, IngestionFlowFileStatus.PROCESSING, null);
  }

  protected boolean finalizeStatus(Long ingestionFlowFileId, IngestionFlowFileResult result) {
    boolean success = result.getErrorDescription() == null;
    updateIngestionFlowStatusActivity.updateStatus(ingestionFlowFileId,
      IngestionFlowFileStatus.PROCESSING,
      success
        ? IngestionFlowFileStatus.COMPLETED
        : IngestionFlowFileStatus.ERROR,
      result);
    return success;
  }

  protected void sendEmail(Long ingestionFlowFileId, boolean success) {
    sendEmailIngestionFlowActivity.sendEmail(ingestionFlowFileId, success);
  }

}
