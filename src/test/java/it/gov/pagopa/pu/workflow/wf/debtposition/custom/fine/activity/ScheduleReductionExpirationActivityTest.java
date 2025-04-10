package it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.activity;

import it.gov.pagopa.payhub.activities.dto.IONotificationMessage;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.FineWfExecutionConfig;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.DebtPositionFineClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static it.gov.pagopa.pu.workflow.utils.TestUtils.OFFSET_DATE_TIME;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ScheduleReductionExpirationActivityTest {

  @Mock
  private DebtPositionFineClient clientMock;

  private ScheduleReductionExpirationActivity activity;

  @BeforeEach
  void init() {
    activity = new ScheduleReductionExpirationActivityImpl(clientMock);
  }

  @Test
  void whenScheduleExpireFineReduction(){
    //Given
    Long debtPositionId = 1L;
    IONotificationMessage ioNotificationMessage = new IONotificationMessage("subject", "message");
    FineWfExecutionConfig fineConfig = FineWfExecutionConfig.builder()
      .ioMessages(new FineWfExecutionConfig.IONotificationFineWfMessages(ioNotificationMessage, null))
      .build();
    String workflowId = "workflowId";

    Mockito.when(clientMock.scheduleExpireFineReduction(debtPositionId, fineConfig, OFFSET_DATE_TIME))
      .thenReturn(workflowId);

    //When
    String result = activity.scheduleExpireFineReduction(debtPositionId, fineConfig, OFFSET_DATE_TIME);

    //Then
    assertEquals(workflowId, result);
  }
}
