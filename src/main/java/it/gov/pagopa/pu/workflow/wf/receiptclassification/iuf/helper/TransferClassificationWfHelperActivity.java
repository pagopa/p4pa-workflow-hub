package it.gov.pagopa.pu.workflow.wf.receiptclassification.iuf.helper;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface TransferClassificationWfHelperActivity {

  public void signalTransferClassificationWithStart(Long organizationId, String iuv, String iur, int transferIndex);

}
