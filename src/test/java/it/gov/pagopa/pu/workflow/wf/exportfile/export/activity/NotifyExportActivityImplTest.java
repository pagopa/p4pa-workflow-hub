package it.gov.pagopa.pu.workflow.wf.exportfile.export.activity;

import it.gov.pagopa.pu.workflow.dto.ExportDataDTO;
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
class NotifyExportActivityImplTest {

  @Mock
  private DataEventsProducerService dataEventsProducerServiceMock;

  private NotifyExportActivity notifyExportActivity;

  @BeforeEach
  void setUp() {
    notifyExportActivity = new NotifyExportActivityImpl(dataEventsProducerServiceMock);
  }

  @Test
  void whenNotifyExportEventThenOk() {
    //given
    ExportDataDTO exportDataDTO = new ExportDataDTO();
    DataEventRequestDTO dataEventRequestDTO = new DataEventRequestDTO();

    doNothing().when(dataEventsProducerServiceMock).notifyExportEvent(exportDataDTO, dataEventRequestDTO);
    //then
    assertDoesNotThrow(() -> notifyExportActivity.notifyExportEvent(exportDataDTO, dataEventRequestDTO));
  }
}
