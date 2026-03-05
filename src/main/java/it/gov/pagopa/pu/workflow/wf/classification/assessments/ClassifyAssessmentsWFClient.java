package it.gov.pagopa.pu.workflow.wf.classification.assessments;

import io.temporal.client.WorkflowStub;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowInternalErrorException;
import it.gov.pagopa.pu.workflow.service.organization.OrganizationRetrieverService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import it.gov.pagopa.pu.workflow.wf.classification.assessments.dto.ClassifyAssessmentStartSignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.assessments.wfclassification.ClassifyAssessmentsWF;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ClassifyAssessmentsWFClient {

  private final WorkflowService workflowService;
  private final WorkflowClientService workflowClientService;
  private final OrganizationRetrieverService organizationRetrieverService;

  public ClassifyAssessmentsWFClient(WorkflowService workflowService, WorkflowClientService workflowClientService, OrganizationRetrieverService organizationRetrieverService) {
    this.workflowService = workflowService;
    this.workflowClientService = workflowClientService;
    this.organizationRetrieverService = organizationRetrieverService;
  }

  public WorkflowCreatedDTO startAssessmentsClassification(ClassifyAssessmentStartSignalDTO signalDTO) {
    log.info("Starting Assessments Classification for semantic key: {}", signalDTO);

    Long organizationId = signalDTO.getOrgId();
    if (organizationRetrieverService.isClassificationDisabled(organizationId)) {
      log.info("Skipping Assessments Classification: organization {} has flag_classification_enabled = false", organizationId);
      throw new ValidationException("Classification disabled for organization " + organizationId);
    }

    String workflowId = generateWorkflowId(organizationId, signalDTO.getIuv(), signalDTO.getIud());
    String taskQueue = TaskQueueConstants.TASK_QUEUE_ASSESSMENTS_CLASSIFICATION;

    WorkflowStub untypedWorkflowStub = workflowService.buildUntypedWorkflowStub(
      ClassifyAssessmentsWF.class,
      taskQueue,
      workflowId
    );

    return workflowClientService.signalWithStart(
      untypedWorkflowStub,
      ClassifyAssessmentsWF.SIGNAL_METHOD_NAME_START_ASSESSMENTS_CLASSIFICATION,
      new Object[]{signalDTO},
      new Object[]{}
    );
  }

  private String generateWorkflowId(Long orgId, String iuv, String iud) {
    if (orgId == null || iuv == null || iud == null) {
      throw new WorkflowInternalErrorException("[INVALID_WORKFLOW_ID] The ID or the workflow must not be null");
    }
    return Utilities.generateWorkflowId(String.format("%d-%s-%s", orgId, iuv, iud), ClassifyAssessmentsWF.class);
  }
}
