package it.gov.pagopa.pu.workflow.wf.assessments;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.assessments.wfassessments.CreateAssessmentsRegistryWF;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CreateAssessmentsRegistryWFClient {

  private final WorkflowService workflowService;
  private final WorkflowClientService workflowClientService;

  public CreateAssessmentsRegistryWFClient(WorkflowService workflowService, WorkflowClientService workflowClientService) {
    this.workflowService = workflowService;
    this.workflowClientService = workflowClientService;
  }

  public void createAssessmentsRegistry(String eventId, DebtPositionDTO debtPositionDTO, List<String> iudList) {
      String taskQueue = TaskQueueConstants.TASK_QUEUE_ASSESSMENTS;
      String workflowId = generateWorkflowId(eventId, CreateAssessmentsRegistryWF.class);

      CreateAssessmentsRegistryWF workflow = workflowService.buildWorkflowStubToStartNew(
        CreateAssessmentsRegistryWF.class,
        taskQueue,
        workflowId);
      workflowClientService.start(workflow::createAssessmentsRegistry,debtPositionDTO, iudList);
    }
  }
