package it.gov.pagopa.pu.workflow.wf.classification.assessments.wfclassification;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.assessments.AssessmentsClassificationActivity;
import it.gov.pagopa.payhub.activities.dto.assessments.AssessmentsClassificationSemanticKeyDTO;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowServiceImpl;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.classification.assessments.config.ClassifyAssessmentsWfConfig;
import it.gov.pagopa.pu.workflow.wf.classification.assessments.dto.ClassifyAssessmentStartSignalDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_CLASSIFICATION_MEDIUM_PRIORITY_LOCAL)
public class ClassifyAssessmentsWFImpl implements ClassifyAssessmentsWF, ApplicationContextAware {

  private AssessmentsClassificationActivity assessmentsClassificationActivity;

  private final Collection<AssessmentsClassificationSemanticKeyDTO> toClassify = new ConcurrentLinkedQueue<>();

  /**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    ClassifyAssessmentsWfConfig wfConfig = applicationContext.getBean(ClassifyAssessmentsWfConfig.class);
    assessmentsClassificationActivity = wfConfig.buildAssessmentsClassificationActivityStub();
  }

  @Override
  public void classify() {
    WorkflowServiceImpl.waitForSignalMethods();

    log.info("Classifying Assessments: {}", toClassify);

    toClassify.stream().distinct()
      .forEach(item -> {
        log.info("Handling Assessment classification for semantic key {}", item);
        assessmentsClassificationActivity.classifyAssessment(item);
        log.info("Ingestion to classify Assessment with semantic key {} is completed", item);
      });
  }

  @Override
  public void startAssessmentClassification(ClassifyAssessmentStartSignalDTO signalDTO) {
    log.info("Starting Assessment classification with signal {}", signalDTO);
    AssessmentsClassificationSemanticKeyDTO assessmentsClassificationSemanticKeyDTO = AssessmentsClassificationSemanticKeyDTO.builder()
      .orgId(signalDTO.getOrgId())
      .iuv(signalDTO.getIuv())
      .iud(signalDTO.getIud())
      .build();
    toClassify.add(assessmentsClassificationSemanticKeyDTO);
  }
}
