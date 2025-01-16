package it.gov.pagopa.pu.workflow.wf.classification.iuf.activity;

//import it.gov.pagopa.pu.workflow.wf.receiptclassification.iuf.TransferClassificationWFClient;
import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.classification.IufClassificationWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Lazy
@Service
@Slf4j
@ActivityImpl(taskQueues = IufClassificationWFImpl.TASK_QUEUE)
public class StartTransferClassificationActivityImpl implements StartTransferClassificationActivity {

    //  TODO: P4ADEV-1921 Uncomment the following lines
//  private TransferClassificationWFClient TransferClassificationWFClient;
//  public StartTransferClassificationActivityImpl(TransferClassificationWFClient TransferClassificationWFClient) {
//    this.TransferClassificationWFClient = TransferClassificationWFClient;
//  }

    @Override
    public void signalTransferClassificationWithStart(Long organizationId, String iuv, String iur, int transferIndex) {
        log.info("signalTransferClassificationWithStart - organizationId: {}, iuv: {}, iur: {}, transferIndex: {}", organizationId, iuv, iur, transferIndex);
        // TODO P4ADEV-1921 Uncomment the following line
        // TransferClassificationWFClient.transfer(organizationId, Long.valueOf(iuv), iur);
    }

}
