package it.gov.pagopa.pu.workflow.event.dataevents.dto;

import it.gov.pagopa.pu.workflow.enums.DataEventType;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Data
public class DataEventRequestDTO implements Serializable {
  private DataEventType dataEventType;
  private String eventDescription;
}
