package it.gov.pagopa.pu.workflow.wf.receiptclassification.iuf.helper;

//import it.gov.pagopa.pu.workflow.wf.receiptclassification.iuf.TransferClassificationWFClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Lazy
@Service
@Slf4j
public class TransferClassificationWfHelperActivityImpl implements TransferClassificationWfHelperActivity {

  // TODO: Uncomment the following lines
 // private TransferClassificationWFClient TransferClassificationWFClient;

//  public TransferClassificationWfHelperActivityImpl(TransferClassificationWFClient TransferClassificationWFClient) {
//    this.TransferClassificationWFClient = TransferClassificationWFClient;
//  }


  @Override
  public void signalTransferClassificationWithStart(Long organizationId, String iuv, String iur, int transferIndex) {

    log.info("signalTransferClassificationWithStart - organizationId: {}, iuv: {}, iur: {}, transferIndex: {}", organizationId, iuv, iur, transferIndex);
    // TODO Uncomment the following line
    // TransferClassificationWFClient.transfer(organizationId, Long.valueOf(iuv), iur);

  }
}
