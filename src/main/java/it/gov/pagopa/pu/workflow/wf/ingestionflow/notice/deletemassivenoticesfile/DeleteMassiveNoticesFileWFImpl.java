package it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.deletemassivenoticesfile;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.notice.DeleteMassiveNoticesFileActivity;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.config.DeleteMassiveNoticesFileWFConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY)
public class DeleteMassiveNoticesFileWFImpl implements DeleteMassiveNoticesFileWF, ApplicationContextAware {

  private DeleteMassiveNoticesFileActivity deleteMassiveNoticesFileActivity;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    DeleteMassiveNoticesFileWFConfig wfConfig = applicationContext.getBean(DeleteMassiveNoticesFileWFConfig.class);
    this.deleteMassiveNoticesFileActivity = wfConfig.buildDeleteMassiveNoticesFileActivityStub();
  }

  @Override
  public void deleteMassiveNoticesFile(Long ingestionFlowFileId) {
    log.info("START deleteMassiveNoticesFile for ingestionFlowFileId={}", ingestionFlowFileId);

    deleteMassiveNoticesFileActivity.deleteMassiveNoticesFile(ingestionFlowFileId);

    log.info("END deleteMassiveNoticesFile for ingestionFlowFileId={}", ingestionFlowFileId);
  }
}
