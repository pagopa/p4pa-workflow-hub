package it.gov.pagopa.pu.workflow.wf.assessments.wfassessments;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.assessments.AssessmentsCreationActivity;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.wf.assessments.config.CreateAssessmentsWFConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@Slf4j
@WorkflowImpl(taskQueues = CreateAssessmentsWFImpl.TASK_QUEUE_CREATE_ASSESSMENTS_WF)
public class CreateAssessmentsWFImpl implements CreateAssessmentsWF, ApplicationContextAware {
  public static final String TASK_QUEUE_CREATE_ASSESSMENTS_WF = "CreateAssessmentsWF";

  private AssessmentsCreationActivity assessmentsCreationActivity;

  /**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    CreateAssessmentsWFConfig wfConfig = applicationContext.getBean(CreateAssessmentsWFConfig.class);
    assessmentsCreationActivity = wfConfig.buildAssessmentsCreationActivityStub();
  }

  @Override
  public void createAssessment(Long receiptId) {
    log.info("Creating assessment for receiptId: {}", receiptId);

    assessmentsCreationActivity.createAssessments(receiptId);

    log.info("Assessments creation for receiptId {} is completed", receiptId);
  }
}
