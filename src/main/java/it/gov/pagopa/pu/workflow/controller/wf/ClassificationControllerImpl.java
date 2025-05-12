package it.gov.pagopa.pu.workflow.controller.wf;

import it.gov.pagopa.pu.workflow.controller.generated.ClassificationApi;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iud.IudClassificationWFClient;
import it.gov.pagopa.pu.workflow.wf.classification.iud.dto.IudClassificationNotifyPaymentNotificationSignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iud.dto.IudClassificationNotifyReceiptSignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.transfer.TransferClassificationWFClient;
import it.gov.pagopa.pu.workflow.wf.classification.transfer.dto.TransferClassificationStartSignalDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class ClassificationControllerImpl implements ClassificationApi {
  private final TransferClassificationWFClient transferClassificationWFClient;
  private final IudClassificationWFClient iudClassificationWFClient;

	public ClassificationControllerImpl(TransferClassificationWFClient transferClassificationWFClient,
                                      IudClassificationWFClient iudClassificationWFClient) {
		this.transferClassificationWFClient = transferClassificationWFClient;
    this.iudClassificationWFClient = iudClassificationWFClient;
  }

	@Override
  public ResponseEntity<WorkflowCreatedDTO> transferClassification(Long orgId, String iuv, String iur, Integer transferIndex) {
    log.info("Creating transfer classification Workflow for organization id {} and iuv {} and iur {} and transfer index {}", orgId, iuv, iur, transferIndex);
    WorkflowCreatedDTO wfExec = transferClassificationWFClient.startTransferClassification(new TransferClassificationStartSignalDTO(orgId, iuv, iur, transferIndex));
    return ResponseEntity.status(201).body(wfExec);
  }

  @Override
  public ResponseEntity<WorkflowCreatedDTO> iudClassificationByPaymentNotificationSignal(Long orgId, String iud) {
    log.info("Creating iud classification Workflow for organization id {} and iud {}", orgId, iud);
    IudClassificationNotifyPaymentNotificationSignalDTO signalDTO = new IudClassificationNotifyPaymentNotificationSignalDTO(iud, orgId);
    WorkflowCreatedDTO wfExec =  iudClassificationWFClient.notifyPaymentNotification(signalDTO);

    return ResponseEntity.status(201).body(wfExec);
  }

  @Override
  public ResponseEntity<WorkflowCreatedDTO> iudClassificationByReceiptSignal(Long orgId, String iud, String iuv, String iur, List<Integer> transferIndexes) {
    log.info("Creating iud classification Workflow for organization id {} and iud {} and iuv {} and iur {} and transfer indexes {}", orgId, iud, iuv, iur, transferIndexes);
    IudClassificationNotifyReceiptSignalDTO signalDTO = new IudClassificationNotifyReceiptSignalDTO(orgId, iud, iuv, iur, transferIndexes);
    WorkflowCreatedDTO wfExec = iudClassificationWFClient.notifyReceipt(signalDTO);

    return ResponseEntity.status(201).body(wfExec);
  }
}
