package it.gov.pagopa.pu.workflow.utils.faker;

import it.gov.pagopa.payhub.activities.enums.IngestionFlowFileType;
import it.gov.pagopa.pu.workflow.dto.generated.IngestionFlowFileTypeRequest;

public class IngestionFlowFileTypeFaker {

  public static IngestionFlowFileType buildIngestionFlowFileType(){
    return IngestionFlowFileType.PAYMENTS_REPORTING;
  }

  public static IngestionFlowFileTypeRequest buildIngestionFlowFileTypeRequest(){
    return IngestionFlowFileTypeRequest.PAYMENTS_REPORTING;
  }
}
