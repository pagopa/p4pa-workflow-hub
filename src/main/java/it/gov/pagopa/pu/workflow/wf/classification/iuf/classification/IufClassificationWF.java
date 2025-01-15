package it.gov.pagopa.pu.workflow.wf.classification.iuf.classification;

import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.dto.IufClassificationNotifyPaymentsReportingSignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.dto.IufClassificationNotifyTreasurySignalDTO;

/**
 * Workflow interface for the IUF receipt classification workflow
 */
@WorkflowInterface
public interface IufClassificationWF {

  String  SIGNAL_METHOD_NAME_FOR_TREASURY="signalForTreasury";
  String  SIGNAL_METHOD_NAME_FOR_REPORTING="signalForReporting";

  @WorkflowMethod
  void classify();

  @SignalMethod
  void notifyTreasury(IufClassificationNotifyTreasurySignalDTO signalDTO);

  @SignalMethod
  void notifyPaymentsReporting(IufClassificationNotifyPaymentsReportingSignalDTO signalDTO);

}
