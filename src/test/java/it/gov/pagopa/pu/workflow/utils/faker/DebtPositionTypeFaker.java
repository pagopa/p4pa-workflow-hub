package it.gov.pagopa.pu.workflow.utils.faker;

import it.gov.pagopa.payhub.activities.dto.debtposition.DebtPositionTypeDTO;
import it.gov.pagopa.pu.workflow.dto.generated.DebtPositionTypeRequestDTO;

public class DebtPositionTypeFaker {

    public static DebtPositionTypeDTO buildDebtPositionType(){
        return DebtPositionTypeDTO.builder()
                .debtTypePositionId(1L)
                .brokerId(2L)
                .code("code")
                .taxonomyCode("taxonomyCode")
                .macroArea("macroArea")
                .serviceType("serviceType")
                .collectingReason("collectingReason")
                .flagPrintDueDate(true)
                .flagAnonymousFiscalCode(false)
                .flagMandatoryDueDate(true)
                .description("description")
                .build();
    }

  public static DebtPositionTypeRequestDTO buildDebtPositionTypeRequestDTO(){
    return DebtPositionTypeRequestDTO.builder()
      .debtTypePositionId(1L)
      .brokerId(2L)
      .code("code")
      .taxonomyCode("taxonomyCode")
      .macroArea("macroArea")
      .serviceType("serviceType")
      .collectingReason("collectingReason")
      .flagPrintDueDate(true)
      .flagAnonymousFiscalCode(false)
      .flagMandatoryDueDate(true)
      .description("description")
      .build();
  }
}
