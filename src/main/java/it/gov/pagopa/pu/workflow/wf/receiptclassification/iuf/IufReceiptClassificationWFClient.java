package it.gov.pagopa.pu.workflow.wf.receiptclassification.iuf;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowStub;
import it.gov.pagopa.payhub.activities.dto.classifications.Transfer2ClassifyDTO;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowInternalErrorException;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.receiptclassification.iuf.classification.IufReceiptClassificationWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;
import static it.gov.pagopa.pu.workflow.wf.receiptclassification.iuf.IufReceiptClassificationSignalDTO.SIGNAL_METHOD_NAME;

@Slf4j
@Service
public class IufReceiptClassificationWFClient {

  private final WorkflowService workflowService;

  public IufReceiptClassificationWFClient(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  public String classifyForTreasury(Long organizationId, Long treasuryId, String iuf) {

    IufReceiptClassificationSignalDTO signalDTO = IufReceiptClassificationSignalDTO.builder()
      .type(IufReceiptClassificationSignalType.TESORERIA)
      .organizationId(organizationId)
      .iuf(iuf)
      .treasuryId(treasuryId)
      .build();

    String workflowId = generateWorkflowIdForTreasure(organizationId, treasuryId, iuf, IufReceiptClassificationWFImpl.TASK_QUEUE);

    WorkflowStub untypedWorkflowStub = workflowService.buildUntypedWorkflowStub(IufReceiptClassificationWFImpl.TASK_QUEUE, workflowId);
    WorkflowExecution wfExecution = untypedWorkflowStub.signalWithStart(SIGNAL_METHOD_NAME,
      new Object[]{signalDTO},
      new Object[]{signalDTO}
    );

    log.info("IUF receipt classification Workflow for Treasury started with workflowId: {}", wfExecution.getWorkflowId());

    return workflowId;

  }

  public String classifyForReporting(Long organizationId, String iuf, String outcomeCode, List<Transfer2ClassifyDTO> transfers2classify) {
    IufReceiptClassificationSignalDTO signalDTO = IufReceiptClassificationSignalDTO.builder()
      .type(IufReceiptClassificationSignalType.RENDICONTAZIONE)
      .organizationId(organizationId)
      .iuf(iuf)
      .transfers2classify(transfers2classify)
      .build();

    String workflowId = generateWorkflowIdForReporting(organizationId, iuf, outcomeCode, transfers2classify, IufReceiptClassificationWFImpl.TASK_QUEUE);

    WorkflowStub untypedWorkflowStub = workflowService.buildUntypedWorkflowStub(IufReceiptClassificationWFImpl.TASK_QUEUE, workflowId);
    WorkflowExecution wfExecution = untypedWorkflowStub.signalWithStart(SIGNAL_METHOD_NAME,
      new Object[]{signalDTO},
      new Object[]{signalDTO}
    );

    log.info("IUF receipt classification Workflow for reporting started with workflowId: {}", wfExecution.getWorkflowId());

    return workflowId;

  }


  ///  private methods

  private static String generateWorkflowIdForTreasure(Long organizationId, Long treasuryId, String iuf, String workflow) {

    if (organizationId == null || treasuryId == null || iuf == null || workflow == null) {
      throw new WorkflowInternalErrorException("The organizationId or treasuryId or iuf or the workflow must not be null");
    }
    return String.format("%s-%s-%d-%s", workflow, organizationId, treasuryId, iuf);

  }


  private static String generateWorkflowIdForReporting(Long organizationId, String iuf, String outcomeCode,
                                                       List<Transfer2ClassifyDTO> transfers2classify, String workflow) {

    if (organizationId == null || iuf == null || outcomeCode == null || transfers2classify == null || workflow == null) {
      throw new WorkflowInternalErrorException("The organizationId or treasuryId or iuf or transfers2classify or the workflow must not be null");
    }
    return String.format("%s-%d-%s-%s-%d", workflow, organizationId, outcomeCode, iuf, transfers2classify.hashCode());

  }

}



