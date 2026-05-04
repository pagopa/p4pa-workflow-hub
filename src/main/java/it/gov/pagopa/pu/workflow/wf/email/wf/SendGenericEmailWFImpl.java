package it.gov.pagopa.pu.workflow.wf.email.wf;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.email.SendEmailActivity;
import it.gov.pagopa.payhub.activities.dto.email.EmailDTO;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.email.config.SendGenericEmailWFConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_LOW_PRIORITY)
public class SendGenericEmailWFImpl implements SendGenericEmailWF, ApplicationContextAware {


  private SendEmailActivity sendEmailActivity;

  /**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    SendGenericEmailWFConfig wfConfig = applicationContext.getBean(SendGenericEmailWFConfig.class);
    sendEmailActivity = wfConfig.buildSendEmailActivityStub();
  }

  @Override
  public void sendGenericEmail(EmailDTO emailDTO, Long brokerId) {
    log.info("Sending email");
    sendEmailActivity.sendEmail(emailDTO, brokerId);
    log.info("Completed email send");
  }

}
