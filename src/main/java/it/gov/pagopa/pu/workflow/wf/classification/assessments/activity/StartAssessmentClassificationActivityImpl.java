package it.gov.pagopa.pu.workflow.wf.classification.assessments.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.classification.assessments.ClassifyAssessmentsWFClient;
import it.gov.pagopa.pu.workflow.wf.classification.assessments.dto.ClassifyAssessmentStartSignalDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ActivityImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_CLASSIFICATION_MEDIUM_PRIORITY_LOCAL)
public class StartAssessmentClassificationActivityImpl implements StartAssessmentClassificationActivity {

  private final ClassifyAssessmentsWFClient classifyAssessmentsWFClient;

  public StartAssessmentClassificationActivityImpl(
    ClassifyAssessmentsWFClient classifyAssessmentsWFClient) {
    this.classifyAssessmentsWFClient = classifyAssessmentsWFClient;
  }

  @Override
  public void signalAssessmentClassificationWithStart(Long organizationId, String iuv, String iud) {
    log.info("signalAssessmentClassificationWithStart - organizationId: {}, iuv: {}, iud: {}", organizationId, iuv, iud);
    ClassifyAssessmentStartSignalDTO signalDTO = ClassifyAssessmentStartSignalDTO.builder()
      .orgId(organizationId)
      .iuv(iuv)
      .iud(iud)
      .build();
    classifyAssessmentsWFClient.startAssessmentsClassification(signalDTO);
  }
}
