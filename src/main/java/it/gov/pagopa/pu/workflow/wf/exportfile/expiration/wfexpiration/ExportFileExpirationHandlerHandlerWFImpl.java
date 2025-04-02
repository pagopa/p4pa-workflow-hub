package it.gov.pagopa.pu.workflow.wf.exportfile.expiration.wfexpiration;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.exportflow.ExportFileExpirationHandlerActivity;
import it.gov.pagopa.pu.workflow.wf.exportfile.expiration.config.ExportFileExpirationHandlerWFConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@WorkflowImpl(taskQueues = ExportFileExpirationHandlerHandlerWFImpl.TASK_QUEUE_EXPORT_FILE_EXPIRATION_HANDLER_WF)
@Slf4j
public class ExportFileExpirationHandlerHandlerWFImpl implements ExportFileExpirationHandlerWF, ApplicationContextAware {
  public static final String TASK_QUEUE_EXPORT_FILE_EXPIRATION_HANDLER_WF = "ExportFileExpirationHandlerWF";

  private ExportFileExpirationHandlerActivity exportFileExpirationHandlerActivity;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    ExportFileExpirationHandlerWFConfig wfConfig = applicationContext.getBean(ExportFileExpirationHandlerWFConfig.class);
    exportFileExpirationHandlerActivity = wfConfig.buildExportFileExpirationHandlerActivityStub();
  }

  @Override
  public void exportFileExpirationHandler(Long exportFileId) {
    log.info("Creating exportFileExpirationHandler for exportFileId: {}", exportFileId);

    exportFileExpirationHandlerActivity.handleExpiration(exportFileId);

    log.info("ExportFileExpirationHandler creation for exportFileId {} is completed", exportFileId);
  }
}
