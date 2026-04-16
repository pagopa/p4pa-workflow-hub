package it.gov.pagopa.pu.workflow.wf.classification.iuf;

import io.temporal.client.WorkflowStub;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.workflow.service.organization.OrganizationRetrieverService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.ErrorCodeConstants;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.dto.IufClassificationNotifyPaymentsReportingSignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.dto.IufClassificationNotifyTreasurySignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.wfclassification.IufClassificationWF;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class IufClassificationWFClient {

  private final WorkflowService workflowService;
  private final WorkflowClientService workflowClientService;
  private final OrganizationRetrieverService organizationRetrieverService;

  public IufClassificationWFClient(WorkflowService workflowService, WorkflowClientService workflowClientService, OrganizationRetrieverService organizationRetrieverService) {
    this.workflowService = workflowService;
    this.workflowClientService = workflowClientService;
    this.organizationRetrieverService = organizationRetrieverService;
  }

  public WorkflowCreatedDTO notifyTreasury(IufClassificationNotifyTreasurySignalDTO signalDTO) {
    Long organizationId = signalDTO.getOrganizationId();
    if (!organizationRetrieverService.isClassificationEnabled(organizationId)) {
      log.info("Skipping IUF Classification by treasury: organization {} has flag_classification_enabled = false", organizationId);
      return null;
    }

    String workflowId = generateWorkflowId(signalDTO.getOrganizationId(), signalDTO.getIuf());

    String taskQueue = TaskQueueConstants.TASK_QUEUE_CLASSIFICATION_MEDIUM_PRIORITY;
    WorkflowStub untypedWorkflowStub = workflowService.buildUntypedWorkflowStub(IufClassificationWF.class, taskQueue, workflowId);
    return workflowClientService.signalWithStart(
      untypedWorkflowStub,
      IufClassificationWF.SIGNAL_METHOD_NAME_NOTIFY_TREASURY,
      new Object[]{signalDTO},
      new Object[]{}
    );
  }

  public WorkflowCreatedDTO notifyPaymentsReporting(IufClassificationNotifyPaymentsReportingSignalDTO signalDTO) {
    Long organizationId = signalDTO.getOrganizationId();
    if (!organizationRetrieverService.isClassificationEnabled(organizationId)) {
      log.info("Skipping IUF Classification by payments: organization {} has flag_classification_enabled = false", organizationId);
      return null;
    }

    String workflowId = generateWorkflowId(signalDTO.getOrganizationId(), signalDTO.getIuf());

    String taskQueue = TaskQueueConstants.TASK_QUEUE_CLASSIFICATION_MEDIUM_PRIORITY;
    WorkflowStub untypedWorkflowStub = workflowService.buildUntypedWorkflowStub(IufClassificationWF.class, taskQueue, workflowId);
    return workflowClientService.signalWithStart(
      untypedWorkflowStub,
      IufClassificationWF.SIGNAL_METHOD_NAME_NOTIFY_PAYMENTS_REPORTING,
      new Object[]{signalDTO},
      new Object[]{}
    );
  }

  private static String generateWorkflowId(Long organizationId, String iuf) {
    if (organizationId == null || iuf == null) {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_ORGANIZATION_ID_OR_IUF, "The organizationId or iuf must not be null");
    }
    return Utilities.generateWorkflowId(String.format("%d-%s", organizationId, iuf), IufClassificationWF.class);
  }

}
