package it.gov.pagopa.pu.workflow.wf.debtposition.sync.activity;

import it.gov.pagopa.payhub.activities.dto.IONotificationMessage;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtposition.dto.generated.SyncCompleteDTO;
import it.gov.pagopa.pu.workflow.wf.debtposition.ionotification.IoNotificationWFClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static it.gov.pagopa.pu.workflow.utils.faker.DebtPositionFaker.buildDebtPositionDTO;

@ExtendWith(MockitoExtension.class)
class InvokeIONotificationActivityTest {

  @Mock
  private IoNotificationWFClient clientMock;

  private InvokeIONotificationActivity activity;

  @BeforeEach
  void init(){
    activity = new InvokeIONotificationActivityImpl(clientMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(clientMock);
  }

  @Test
  void whenInvokeIONotificationActivityThenInvokeClient(){
    // Given
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    Map<String, SyncCompleteDTO> iudSyncCompleteDTOMap = new HashMap<>();
    iudSyncCompleteDTOMap.put("SYNC_IUD", SyncCompleteDTO.builder().newStatus(InstallmentStatus.UNPAID).build());
    GenericWfExecutionConfig.IONotificationBaseOpsMessages ioMessage =
      new GenericWfExecutionConfig.IONotificationBaseOpsMessages(new IONotificationMessage("subject", "message"), null, null);

    // When
    activity.invokeIONotification(debtPositionDTO, iudSyncCompleteDTOMap, ioMessage);

    // Then
    Mockito.verify(clientMock)
      .sendIoNotification(debtPositionDTO, iudSyncCompleteDTOMap, ioMessage);
  }
}
