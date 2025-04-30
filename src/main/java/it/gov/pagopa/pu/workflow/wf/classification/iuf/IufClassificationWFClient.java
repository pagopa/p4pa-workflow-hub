package it.gov.pagopa.pu.workflow.wf.classification.iuf;

import io.temporal.client.WorkflowStub;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowInternalErrorException;
import it.gov.pagopa.pu.workflow.mapper.WorkflowCreatedMapper;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
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

  public IufClassificationWFClient(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  public WorkflowCreatedDTO notifyTreasury(IufClassificationNotifyTreasurySignalDTO signalDTO) {
    String workflowId = generateWorkflowId(signalDTO.getOrganizationId(), signalDTO.getIuf());

    WorkflowStub untypedWorkflowStub = workflowService.buildUntypedWorkflowStub(IufClassificationWFImpl.TASK_QUEUE_IUF_CLASSIFICATION_WF, workflowId);
    WorkflowCreatedDTO wfExec = WorkflowCreatedMapper.map(untypedWorkflowStub.signalWithStart(
      IufClassificationWF.SIGNAL_METHOD_NAME_NOTIFY_TREASURY,
      new Object[]{signalDTO},
      new Object[]{}
    ));

    logWfExec(wfExec);
    return wfExec;

  }

  public WorkflowCreatedDTO notifyPaymentsReporting(IufClassificationNotifyPaymentsReportingSignalDTO signalDTO) {
    String workflowId = generateWorkflowId(signalDTO.getOrganizationId(), signalDTO.getIuf());

    WorkflowStub untypedWorkflowStub = workflowService.buildUntypedWorkflowStub(IufClassificationWFImpl.TASK_QUEUE_IUF_CLASSIFICATION_WF, workflowId);
    WorkflowCreatedDTO wfExec = WorkflowCreatedMapper.map(untypedWorkflowStub.signalWithStart(
      IufClassificationWF.SIGNAL_METHOD_NAME_NOTIFY_PAYMENTS_REPORTING,
      new Object[]{signalDTO},
      new Object[]{}
    ));

    logWfExec(wfExec);
    return wfExec;
  }

 private static String generateWorkflowId(Long organizationId, String iuf) {
  if (organizationId == null || iuf == null) {
    throw new WorkflowInternalErrorException("The organizationId or iuf must not be null");
  }
  return Utilities.generateWorkflowId(String.format("%d-%s", organizationId, iuf), IufClassificationWF.class);
}

  private static void logWfExec(WorkflowCreatedDTO wfExec) {
    log.info("Started workflow: {}", wfExec);
  }

}
