package it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.activity;

import it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.CheckDebtPositionExpirationWfClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ScheduleCheckDpExpirationActivityTest {

  @Mock
  private CheckDebtPositionExpirationWfClient checkDebtPositionExpirationWfClientMock;

  private ScheduleCheckDpExpirationActivity activity;

  @BeforeEach
  void init() {
    activity = new ScheduleCheckDpExpirationActivityImpl(checkDebtPositionExpirationWfClientMock);
  }

  @Test
  void testScheduleNextCheckDpExpiration(){
    Long debtPositionId = 1L;
    OffsetDateTime dateTime = OffsetDateTime.of(2025,1,1,9,12,0, 0, ZoneOffset.UTC);
    assertDoesNotThrow(() -> activity.scheduleNextCheckDpExpiration(debtPositionId, dateTime));

    verify(checkDebtPositionExpirationWfClientMock).scheduleNextCheckDpExpiration(debtPositionId, dateTime);
  }

}
