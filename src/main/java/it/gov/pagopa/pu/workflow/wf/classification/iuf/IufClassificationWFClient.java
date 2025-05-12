package it.gov.pagopa.pu.workflow.wf.classification.iuf;

import io.temporal.client.WorkflowStub;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowInternalErrorException;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.dto.IufClassificationNotifyPaymentsReportingSignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.dto.IufClassificationNotifyTreasurySignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.wfclassification.IufClassificationWF;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.wfclassification.IufClassificationWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class IufClassificationWFClient {

  private final WorkflowService workflowService;
  private final WorkflowClientService workflowClientService;

  public IufClassificationWFClient(WorkflowService workflowService, WorkflowClientService workflowClientService) {
    this.workflowService = workflowService;
    this.workflowClientService = workflowClientService;
  }

  public WorkflowCreatedDTO notifyTreasury(IufClassificationNotifyTreasurySignalDTO signalDTO) {
    String workflowId = generateWorkflowId(signalDTO.getOrganizationId(), signalDTO.getIuf());

    WorkflowStub untypedWorkflowStub = workflowService.buildUntypedWorkflowStub(IufClassificationWFImpl.TASK_QUEUE_IUF_CLASSIFICATION_WF, workflowId);
    return workflowClientService.signalWithStart(
      untypedWorkflowStub,
      IufClassificationWF.SIGNAL_METHOD_NAME_NOTIFY_TREASURY,
      new Object[]{signalDTO},
      new Object[]{}
    );
  }

  public WorkflowCreatedDTO notifyPaymentsReporting(IufClassificationNotifyPaymentsReportingSignalDTO signalDTO) {
    String workflowId = generateWorkflowId(signalDTO.getOrganizationId(), signalDTO.getIuf());

    WorkflowStub untypedWorkflowStub = workflowService.buildUntypedWorkflowStub(IufClassificationWFImpl.TASK_QUEUE_IUF_CLASSIFICATION_WF, workflowId);
    return workflowClientService.signalWithStart(
      untypedWorkflowStub,
      IufClassificationWF.SIGNAL_METHOD_NAME_NOTIFY_PAYMENTS_REPORTING,
      new Object[]{signalDTO},
      new Object[]{}
    );
  }

  private static String generateWorkflowId(Long organizationId, String iuf) {
    if (organizationId == null || iuf == null) {
      throw new WorkflowInternalErrorException("The organizationId or iuf must not be null");
    }
    return Utilities.generateWorkflowId(String.format("%d-%s", organizationId, iuf), IufClassificationWF.class);
  }

}
