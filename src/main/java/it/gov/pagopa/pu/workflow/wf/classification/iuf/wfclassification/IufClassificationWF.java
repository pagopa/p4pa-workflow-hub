package it.gov.pagopa.pu.workflow.wf.classification.iuf.wfclassification;

import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.dto.IufClassificationNotifyPaymentsReportingSignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.dto.IufClassificationNotifyTreasurySignalDTO;

/**
 * Workflow to handle IUF receipt classification
 * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1339031693/Classificazione+incassi#3.5.2.-Classificazione-IUF>Confluence page</a>
 */
@WorkflowInterface
public interface IufClassificationWF {

  String SIGNAL_METHOD_NAME_NOTIFY_TREASURY ="notifyTreasury";
  String SIGNAL_METHOD_NAME_NOTIFY_PAYMENTS_REPORTING ="notifyPaymentsReporting";

  @WorkflowMethod
  void classify();

  @SignalMethod
  void notifyTreasury(IufClassificationNotifyTreasurySignalDTO signalDTO);

  @SignalMethod
  void notifyPaymentsReporting(IufClassificationNotifyPaymentsReportingSignalDTO signalDTO);

}
