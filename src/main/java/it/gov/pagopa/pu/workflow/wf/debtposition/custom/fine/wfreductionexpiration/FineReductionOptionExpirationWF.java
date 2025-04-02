package it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.wfreductionexpiration;

import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.FineWfExecutionConfig;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;

public interface FineReductionOptionExpirationWF {

  String handleFineReductionExpiration(Long debtPositionId, PaymentEventRequestDTO paymentEventRequestDTO, FineWfExecutionConfig executionParams);
}
