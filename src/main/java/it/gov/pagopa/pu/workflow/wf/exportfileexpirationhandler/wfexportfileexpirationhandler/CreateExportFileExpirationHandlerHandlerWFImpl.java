package it.gov.pagopa.pu.workflow.wf.exportfileexpirationhandler.wfexportfileexpirationhandler;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.exportflow.ExportFileExpirationHandlerActivity;
import it.gov.pagopa.pu.workflow.wf.exportfileexpirationhandler.config.CreateExportFileExpirationHandlerWFConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@WorkflowImpl(taskQueues = CreateExportFileExpirationHandlerHandlerWFImpl.TASK_QUEUE_CREATE_EXPORT_FILE_EXPIRATION_HANDLER_WF)
@Slf4j
public class CreateExportFileExpirationHandlerHandlerWFImpl implements CreateExportFileExpirationHandlerWF, ApplicationContextAware {
  public static final String TASK_QUEUE_CREATE_EXPORT_FILE_EXPIRATION_HANDLER_WF = "CreateExportFileExpirationHandlerWF";

  private ExportFileExpirationHandlerActivity exportFileExpirationHandlerActivity;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    CreateExportFileExpirationHandlerWFConfig wfConfig = applicationContext.getBean(CreateExportFileExpirationHandlerWFConfig.class);
    exportFileExpirationHandlerActivity = wfConfig.buildExportFileExpirationHandlerActivityStub();
  }

  @Override
  public void createExportFileExpirationHandler(Long exportFileId) {
    log.info("Creating exportFileExpirationHandler for exportFileId: {}", exportFileId);

    exportFileExpirationHandlerActivity.handleExpiration(exportFileId);

    log.info("ExportFileExpirationHandler creation for exportFileId {} is completed", exportFileId);
  }
}
