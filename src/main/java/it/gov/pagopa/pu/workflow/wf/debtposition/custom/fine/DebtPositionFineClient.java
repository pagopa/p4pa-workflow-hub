package it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine;

import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.FineWfExecutionConfig;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;

public interface DebtPositionFineClient {

  String handleFineReductionExpiration(Long debtPositionId, PaymentEventRequestDTO paymentEventRequestDTO, FineWfExecutionConfig executionParams);
}
