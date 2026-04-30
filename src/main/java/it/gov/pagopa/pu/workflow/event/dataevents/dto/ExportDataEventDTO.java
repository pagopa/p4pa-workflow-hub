package it.gov.pagopa.pu.workflow.event.dataevents.dto;

import it.gov.pagopa.pu.workflow.dto.ExportDataDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class ExportDataEventDTO extends DataEventDTO<ExportDataDTO> {
}
