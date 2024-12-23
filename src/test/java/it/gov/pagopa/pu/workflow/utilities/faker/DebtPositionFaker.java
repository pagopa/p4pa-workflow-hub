package it.gov.pagopa.pu.workflow.utilities.faker;

import it.gov.pagopa.payhub.activities.dto.debtposition.DebtPositionDTO;

public class DebtPositionFaker {

    public static DebtPositionDTO buildDebtPositionDTO(){
        return DebtPositionDTO.builder()
                .debtPositionId(1L)
                .iupdOrg("codeIud")
                .iupdPagopa("gpdIupd")
                .status("statusCode")
                .ingestionFlowFileLineNumber(1L)
                .gpdStatus('G')
                .build();
    }
}
