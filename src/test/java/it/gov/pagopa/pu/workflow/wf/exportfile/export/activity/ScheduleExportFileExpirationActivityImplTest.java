package it.gov.pagopa.pu.workflow.wf.exportfile.export.activity;

import it.gov.pagopa.pu.workflow.wf.exportfile.expiration.ExportFileExpirationHandlerWFClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class ScheduleExportFileExpirationActivityImplTest {

  @Mock
  private ExportFileExpirationHandlerWFClient exportFileExpirationHandlerWFClientMock;

  private ScheduleExportFileExpirationActivity scheduleExportFileExpirationActivity;

  @BeforeEach
  void setUp() {
    scheduleExportFileExpirationActivity = new ScheduleExportFileExpirationActivityImpl(exportFileExpirationHandlerWFClientMock);
  }

  @Test
  void givenParamsWhenScheduleExportFileExpirationThenOk() {
    //given
    Long exportFileId = 1L;
    LocalDate dateTime = LocalDate.of(2025,1,1);
    Mockito.doNothing().when(exportFileExpirationHandlerWFClientMock).scheduleExportFileExpiration(exportFileId, dateTime);
    //then
    assertDoesNotThrow(() ->scheduleExportFileExpirationActivity.scheduleExportFileExpiration(exportFileId, dateTime));
  }
}
