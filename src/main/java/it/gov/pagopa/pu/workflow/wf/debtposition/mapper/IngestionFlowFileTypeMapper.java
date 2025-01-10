package it.gov.pagopa.pu.workflow.wf.debtposition.mapper;

import it.gov.pagopa.payhub.activities.enums.IngestionFlowFileType;
import it.gov.pagopa.pu.workflow.dto.generated.IngestionFlowFileTypeRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IngestionFlowFileTypeMapper {

  IngestionFlowFileType map(IngestionFlowFileTypeRequest ingestionFlowFileTypeRequest);
}
