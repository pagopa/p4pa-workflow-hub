package it.gov.pagopa.pu.workflow.wf.pagopa.send.delete.wfsendlegalfact;

import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.sendnotification.delete.DeleteSendLegalFactFileActivity;
import it.gov.pagopa.payhub.activities.util.Utilities;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.delete.config.DeleteSendLegalFactFileWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_LOW_PRIORITY)
public class DeleteSendLegalFactFileWFImpl implements DeleteSendLegalFactFileWF, ApplicationContextAware {
  private DeleteSendLegalFactFileActivity deleteSendLegalFactFileActivity;

  /**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    DeleteSendLegalFactFileWfConfig wfConfig = applicationContext.getBean(DeleteSendLegalFactFileWfConfig.class);
    deleteSendLegalFactFileActivity = wfConfig.buildDeleteSendLegalFactFileActivityStub();
  }

  @Override
  public void deleteSendLegalFactExpiredFiles(String sendNotificationId) {
    log.info("Start deleteSendLegalFactExpiredFiles Workflow for sendNotificationId {}.", sendNotificationId);

    OffsetDateTime nextFileExpirationDate = deleteSendLegalFactFileActivity.deleteSendLegalFactFile(sendNotificationId);
    if(nextFileExpirationDate != null) {
      waitForNextExpirationDate(sendNotificationId, nextFileExpirationDate);
    }
  }

  private void waitForNextExpirationDate(String sendNotificationId, OffsetDateTime nextFileExpirationDate) {
    OffsetDateTime now = OffsetDateTime.ofInstant(
      Instant.ofEpochMilli(Workflow.currentTimeMillis()),
      Utilities.ZONEID
    );
    Duration waitDuration = Duration.between(now, nextFileExpirationDate);

    if (waitDuration.isPositive()) {
      log.info("Sleeping until {} for deleteSendLegalFactExpiredFiles of sendNotificationId {}", nextFileExpirationDate, sendNotificationId);
      Workflow.sleep(waitDuration);
    }
    Workflow.continueAsNew(sendNotificationId);
  }
}
