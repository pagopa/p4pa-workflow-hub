package it.gov.pagopa.pu.workflow.wf.classification.iuf.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.wfclassification.IufClassificationWFImpl;
import it.gov.pagopa.pu.workflow.wf.classification.transfer.TransferClassificationWFClient;
import it.gov.pagopa.pu.workflow.wf.classification.transfer.dto.TransferClassificationStartSignalDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ActivityImpl(taskQueues = IufClassificationWFImpl.TASK_QUEUE_IUF_CLASSIFICATION_LOCAL_ACTIVITY)
public class StartTransferClassificationActivityImpl implements StartTransferClassificationActivity {

  private final TransferClassificationWFClient transferClassificationWFClient;

  public StartTransferClassificationActivityImpl(TransferClassificationWFClient transferClassificationWFClient) {
    this.transferClassificationWFClient = transferClassificationWFClient;
  }

    @Override
    public void signalTransferClassificationWithStart(Long organizationId, String iuv, String iur, int transferIndex) {
      log.info("signalTransferClassificationWithStart - organizationId: {}, iuv: {}, iur: {}, transferIndex: {}",
        organizationId, iuv, iur, transferIndex);
      TransferClassificationStartSignalDTO signalDTO = TransferClassificationStartSignalDTO.builder()
        .orgId(organizationId)
        .iuv(iuv)
        .iur(iur)
        .transferIndex(transferIndex)
        .build();
      transferClassificationWFClient.startTransferClassification(signalDTO);
    }

}
