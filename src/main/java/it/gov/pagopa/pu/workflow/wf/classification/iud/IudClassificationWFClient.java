package it.gov.pagopa.pu.workflow.wf.classification.iud;

import io.temporal.client.WorkflowStub;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowInternalErrorException;
import it.gov.pagopa.pu.workflow.mapper.WorkflowCreatedMapper;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import it.gov.pagopa.pu.workflow.wf.classification.iud.dto.IudClassificationNotifyPaymentNotificationSignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iud.dto.IudClassificationNotifyReceiptSignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iud.wfclassification.IudClassificationWF;
import it.gov.pagopa.pu.workflow.wf.classification.iud.wfclassification.IudClassificationWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class IudClassificationWFClient {
  private final WorkflowService workflowService;

  public IudClassificationWFClient(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  public WorkflowCreatedDTO notifyReceipt(IudClassificationNotifyReceiptSignalDTO signalDTO) {
    String workflowId = generateWorkflowId(signalDTO.getOrganizationId(), signalDTO.getIud());

    WorkflowStub untypedWorkflowStub = workflowService.buildUntypedWorkflowStub(IudClassificationWFImpl.TASK_QUEUE_IUF_CLASSIFICATION_WF, workflowId);
    WorkflowCreatedDTO wfExec = WorkflowCreatedMapper.map(untypedWorkflowStub.signalWithStart(
      IudClassificationWF.SIGNAL_METHOD_NAME_NOTIFY_RECEIPT,
      new Object[]{signalDTO},
      new Object[]{}
    ));

    logWfExec(wfExec);
    return wfExec;
  }

  public WorkflowCreatedDTO notifyPaymentNotification(IudClassificationNotifyPaymentNotificationSignalDTO signalDTO) {
    String workflowId = generateWorkflowId(signalDTO.getOrganizationId(), signalDTO.getIud());

    WorkflowStub untypedWorkflowStub = workflowService.buildUntypedWorkflowStub(IudClassificationWFImpl.TASK_QUEUE_IUF_CLASSIFICATION_WF, workflowId);
    WorkflowCreatedDTO wfExec = WorkflowCreatedMapper.map(untypedWorkflowStub.signalWithStart(
      IudClassificationWF.SIGNAL_METHOD_NAME_NOTIFY_PAYMENT_NOTIFICATION,
      new Object[]{signalDTO},
      new Object[]{}
    ));

    logWfExec(wfExec);
    return wfExec;
  }

  private static String generateWorkflowId(Long organizationId, String iud) {
    if (organizationId == null || iud == null) {
      throw new WorkflowInternalErrorException("The organizationId or iud must not be null");
    }
    return Utilities.generateWorkflowId(String.format("%d-%s", organizationId, iud), IudClassificationWF.class);
  }

  private static void logWfExec(WorkflowCreatedDTO wfExec) {
    log.info("Started workflow: {}", wfExec);
  }
}
