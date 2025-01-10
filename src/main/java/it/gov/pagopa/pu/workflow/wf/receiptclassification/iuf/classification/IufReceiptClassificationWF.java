package it.gov.pagopa.pu.workflow.wf.receiptclassification.iuf.classification;

import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.workflow.wf.receiptclassification.iuf.IufReceiptClassificationSignalDTO;

/**
 * Workflow interface for the Payments Reporting Ingestion Workflow
 * */

@WorkflowInterface
public interface IufReceiptClassificationWF {



  @WorkflowMethod
  void classify(IufReceiptClassificationSignalDTO signalDTO);


  @SignalMethod
  void setSignalDTO(IufReceiptClassificationSignalDTO signalDTO);

}
