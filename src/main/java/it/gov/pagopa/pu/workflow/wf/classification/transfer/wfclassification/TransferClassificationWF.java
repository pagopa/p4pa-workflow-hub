package it.gov.pagopa.pu.workflow.wf.classification.transfer.wfclassification;

import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.workflow.wf.classification.transfer.dto.TransferClassificationStartSignalDTO;

/**
 * Workflow interface for the Transfer classification
 * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1339031693/Classificazione+incassi#3.5.3.-Classificazione-Transfer>Confluence page</a>
 * */

@WorkflowInterface
public interface TransferClassificationWF {
    String SIGNAL_METHOD_NAME_START_TRANSFER_CLASSIFICATION ="startTransferClassification";

    @WorkflowMethod
    void classify();

    @SignalMethod
    void startTransferClassification(TransferClassificationStartSignalDTO signalDTO);
}
