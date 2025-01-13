package it.gov.pagopa.pu.workflow.wf.receiptclassification.iuf.classification;

import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.workflow.wf.receiptclassification.iuf.dto.IufReceiptClassificationForReportingSignalDTO;
import it.gov.pagopa.pu.workflow.wf.receiptclassification.iuf.dto.IufReceiptClassificationForTreasurySignalDTO;

/**
 * Workflow interface for the IUF receipt classification workflow
 */
@WorkflowInterface
public interface IufReceiptClassificationWF {

  @WorkflowMethod
  void classify();

  @SignalMethod
  void signalForTreasury(IufReceiptClassificationForTreasurySignalDTO signalDTO);

  @SignalMethod
  void signalForReporting(IufReceiptClassificationForReportingSignalDTO signalDTO);

}
