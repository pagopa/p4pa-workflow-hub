package it.gov.pagopa.pu.workflow.dto;

import it.gov.pagopa.pu.sendnotification.dto.generated.ProgressResponseElementV28DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Data
public class SendStreamEventsDTO implements Serializable {
  private String sendStreamId;
  private List<ProgressResponseElementV28DTO> streamEventBatch;
}
