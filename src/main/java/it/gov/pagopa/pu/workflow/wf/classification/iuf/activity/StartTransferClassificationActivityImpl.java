package it.gov.pagopa.pu.workflow.wf.classification.iuf.activity;

//import it.gov.pagopa.pu.workflow.wf.receiptclassification.iuf.TransferClassificationWFClient;
import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.classification.IufClassificationWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * This class handles the start of the transfer classification workflow.
 *
 * @see <a href="https://pagopa.atlassian.net/browse/P4ADEV-1921">P4ADEV-1921</a>
 */
@Lazy
@Service
@Slf4j
@ActivityImpl(taskQueues = IufClassificationWFImpl.TASK_QUEUE)
public class StartTransferClassificationActivityImpl implements StartTransferClassificationActivity {

//  TODO: Uncomment the following lines
//  private TransferClassificationWFClient TransferClassificationWFClient;

//  public StartTransferClassificationActivityImpl(TransferClassificationWFClient TransferClassificationWFClient) {
//    this.TransferClassificationWFClient = TransferClassificationWFClient;
//  }


  @Override
  public void signalTransferClassificationWithStart(Long organizationId, String iuv, String iur, int transferIndex) {

    log.info("signalTransferClassificationWithStart - organizationId: {}, iuv: {}, iur: {}, transferIndex: {}", organizationId, iuv, iur, transferIndex);
    // TODO Uncomment the following line
    // TransferClassificationWFClient.transfer(organizationId, Long.valueOf(iuv), iur);

  }
}
