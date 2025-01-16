package it.gov.pagopa.pu.workflow.wf.classification.iuf;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowStub;
import it.gov.pagopa.payhub.activities.dto.classifications.Transfer2ClassifyDTO;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowInternalErrorException;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.classification.IufClassificationWF;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.classification.IufClassificationWFImpl;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.dto.IufClassificationNotifyPaymentsReportingSignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.dto.IufClassificationNotifyTreasurySignalDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class IufClassificationWFClient {

  private final WorkflowService workflowService;

  public IufClassificationWFClient(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  public String notifyTreasury(IufClassificationNotifyTreasurySignalDTO signalDTO) {

//    IufClassificationNotifyTreasurySignalDTO signalDTO = IufClassificationNotifyTreasurySignalDTO.builder()
//      .organizationId(organizationId)
//      .iuf(iuf)
//      .treasuryId(treasuryId)
//      .build();

    String workflowId = generateWorkflowId(IufClassificationWFImpl.TASK_QUEUE, signalDTO.getOrganizationId(), signalDTO.getIuf());

    WorkflowStub untypedWorkflowStub = workflowService.buildUntypedWorkflowStub(IufClassificationWFImpl.TASK_QUEUE, workflowId);
    WorkflowExecution wfExecution = untypedWorkflowStub.signalWithStart(
      IufClassificationWF.SIGNAL_METHOD_NAME_NOTIFY_TREASURY,
      new Object[]{signalDTO},
      new Object[]{}
    );

    log.info("IUF receipt classification Workflow for Treasury started with workflowId: {}", wfExecution.getWorkflowId());
    return workflowId;

  }

  public String notifyPaymentsReporting(IufClassificationNotifyPaymentsReportingSignalDTO signalDTO) {

//    IufClassificationNotifyPaymentsReportingSignalDTO signalDTO = IufClassificationNotifyPaymentsReportingSignalDTO.builder()
//      .organizationId(organizationId)
//      .iuf(iuf)
//      .transfers2classify(transfers2classify)
//      .build();

    String workflowId = generateWorkflowId(IufClassificationWFImpl.TASK_QUEUE, signalDTO.getOrganizationId(), signalDTO.getIuf());

    WorkflowStub untypedWorkflowStub = workflowService.buildUntypedWorkflowStub(IufClassificationWFImpl.TASK_QUEUE, workflowId);
    WorkflowExecution wfExecution = untypedWorkflowStub.signalWithStart(
      IufClassificationWF.SIGNAL_METHOD_NAME_NOTIFY_PAYMENTS_REPORTING,
      new Object[]{signalDTO},
      new Object[]{}
    );

    log.info("IUF receipt classification Workflow for reporting started with workflowId: {}", wfExecution.getWorkflowId());
    return workflowId;

  }

 private static String generateWorkflowId(String workflow, Long organizationId, String iuf) {
  if (workflow == null || organizationId == null || iuf == null) {
    throw new WorkflowInternalErrorException("The workflow or organizationId or iuf must not be null");
  }
  return String.format("%s-%d-%s", workflow, organizationId, iuf);
}

}
