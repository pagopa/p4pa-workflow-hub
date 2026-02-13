package it.gov.pagopa.pu.workflow.wf.pagopa.send.wfsendnotification;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.sendnotification.FetchSendLegalFactActivity;
import it.gov.pagopa.pu.sendnotification.dto.generated.LegalFactCategoryDTO;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.config.SendNotificationProcessWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.IOException;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_SEND_LEGAL_FACTS)
public class SendLegalFactProcessWFImpl implements SendLegalFactProcessWF, ApplicationContextAware {

  private FetchSendLegalFactActivity fetchSendLegalFactActivity;

  /**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    SendNotificationProcessWfConfig wfConfig = applicationContext.getBean(SendNotificationProcessWfConfig.class);

    fetchSendLegalFactActivity = wfConfig.buildPFetchSendLegalFactActivityStub();
  }

  @Override
  public void fetchSendLegalFact(String sendNotificationId, String legalFactId, LegalFactCategoryDTO category) {
    try {
      fetchSendLegalFactActivity.downloadAndCacheSendLegalFact(sendNotificationId, category, legalFactId);
    } catch (IOException e) {
      log.error("Error in fetchSendLegalFact: %s".formatted(e.getMessage()), e);
    }
  }
}
