package it.gov.pagopa.pu.workflow.wf.debtposition.sync.activity;

import it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.CheckDebtPositionExpirationWfClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CancelCheckDpExpirationScheduleActivityTest {

  @Mock
  private CheckDebtPositionExpirationWfClient clientMock;

  private CancelCheckDpExpirationScheduleActivity activity;

  @BeforeEach
  void init(){
    activity = new CancelCheckDpExpirationScheduleActivityImpl(clientMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(clientMock);
  }

  @Test
  void whenCancelExpirationScheduleThenInvokeClient(){
    // Given
    long debtPositionId = 1L;

    // When
    activity.cancelExpirationSchedule(debtPositionId);

    // Then
    Mockito.verify(clientMock)
      .cancelScheduling(debtPositionId);
  }
}
