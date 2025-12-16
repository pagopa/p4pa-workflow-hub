package it.gov.pagopa.pu.workflow.wf.dataevents;

import it.gov.pagopa.payhub.activities.dto.assessments.AssessmentEventDTO;
import it.gov.pagopa.pu.workflow.dto.ExportDataDTO;
import it.gov.pagopa.pu.workflow.dto.IngestionDataDTO;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.event.dataevents.dto.DataEventRequestDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.dataevents.wfassessments.PublishPaymentAssessmentsEventWF;
import it.gov.pagopa.pu.workflow.wf.dataevents.wfexport.PublishExportFileEventWF;
import it.gov.pagopa.pu.workflow.wf.dataevents.wfingestion.PublishIngestionFlowFileWF;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
public class PublishDataEventsClient {
  private final WorkflowService workflowService;
  private final WorkflowClientService workflowClientService;
  private static final String ON_DEMAND = "ON-DEMAND";

  public PublishDataEventsClient(WorkflowService workflowService, WorkflowClientService workflowClientService) {
    this.workflowService = workflowService;
    this.workflowClientService = workflowClientService;
  }

  public WorkflowCreatedDTO publishExportFileEventClient(ExportDataDTO exportDataDTO, DataEventRequestDTO dataEventRequestDTO) {
    String taskQueue = TaskQueueConstants.TASK_QUEUE_LOW_PRIORITY;
    String workflowId = generateWorkflowId(ON_DEMAND, PublishExportFileEventWF.class);

    PublishExportFileEventWF workflow = workflowService.buildWorkflowStubToStartNew(
      PublishExportFileEventWF.class,
      taskQueue,
      workflowId
    );
    return workflowClientService.start(workflow::publishExportFileEvent, exportDataDTO, dataEventRequestDTO);
  }

  public WorkflowCreatedDTO publishIngestionFlowFileEventClient(IngestionDataDTO ingestionDataDTO, DataEventRequestDTO dataEventRequest) {
    String taskQueue = TaskQueueConstants.TASK_QUEUE_LOW_PRIORITY;
    String workflowId = generateWorkflowId(ON_DEMAND, PublishIngestionFlowFileWF.class);

    PublishIngestionFlowFileWF workflow = workflowService.buildWorkflowStubToStartNew(
      PublishIngestionFlowFileWF.class,
      taskQueue,
      workflowId
    );
    return workflowClientService.start(workflow::publishIngestionFlowFileEvent, ingestionDataDTO, dataEventRequest);
  }

  public WorkflowCreatedDTO publishPaymentAssessmentsEventClient(AssessmentEventDTO assessmentsEventDTO, DataEventRequestDTO dataEventRequest) {
    String taskQueue = TaskQueueConstants.TASK_QUEUE_LOW_PRIORITY;
    String workflowId = generateWorkflowId(ON_DEMAND, PublishPaymentAssessmentsEventWF.class);

    PublishPaymentAssessmentsEventWF workflow = workflowService.buildWorkflowStubToStartNew(
      PublishPaymentAssessmentsEventWF.class,
      taskQueue,
      workflowId
    );
    return workflowClientService.start(workflow::publishPaymentAssessmentsEvent, assessmentsEventDTO, dataEventRequest);
  }
}
