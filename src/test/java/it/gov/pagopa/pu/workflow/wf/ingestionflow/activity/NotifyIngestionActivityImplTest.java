package it.gov.pagopa.pu.workflow.wf.ingestionflow.activity;

import it.gov.pagopa.pu.workflow.dto.IngestionDataDTO;
import it.gov.pagopa.pu.workflow.event.dataevents.dto.DataEventRequestDTO;
import it.gov.pagopa.pu.workflow.event.dataevents.producer.DataEventsProducerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
class NotifyIngestionActivityImplTest {

  @Mock
  private DataEventsProducerService dataEventsProducerServiceMock;

  private NotifyIngestionActivity notifyIngestionActivity;

  @BeforeEach
  void setUp() {
    notifyIngestionActivity = new NotifyIngestionActivityImpl(dataEventsProducerServiceMock);
  }

  @Test
  void whenNotifyIngestionEventThenOk() {
    //given
    IngestionDataDTO ingestionDataDTO = new IngestionDataDTO();
    DataEventRequestDTO dataEventRequestDTO = new DataEventRequestDTO();

    doNothing().when(dataEventsProducerServiceMock).notifyIngestionEvent(ingestionDataDTO, dataEventRequestDTO);
    //then
    assertDoesNotThrow(() -> notifyIngestionActivity.notifyIngestionEvent(ingestionDataDTO, dataEventRequestDTO));
  }
}
