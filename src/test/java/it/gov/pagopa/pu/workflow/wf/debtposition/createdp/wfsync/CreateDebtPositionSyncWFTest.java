package it.gov.pagopa.pu.workflow.wf.debtposition.createdp.wfsync;

import it.gov.pagopa.payhub.activities.activity.debtposition.ionotification.SendDebtPositionIONotificationActivity;
import it.gov.pagopa.payhub.activities.dto.debtposition.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.wf.debtposition.createdp.config.CreateDebtPositionWfConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import static it.gov.pagopa.pu.workflow.utilities.faker.DebtPositionFaker.buildDebtPositionDTO;

@ExtendWith(MockitoExtension.class)
class CreateDebtPositionSyncWFTest {

  @Mock
  private SendDebtPositionIONotificationActivity sendDebtPositionIONotificationActivityMock;

  private CreateDebtPositionSyncWFImpl wf;

  @BeforeEach
  void init() {
    CreateDebtPositionWfConfig createDebtPositionWfConfigMock = Mockito.mock(CreateDebtPositionWfConfig.class);
    ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    Mockito.when(createDebtPositionWfConfigMock.buildSendDebtPositionIONotificationActivityStub())
      .thenReturn(sendDebtPositionIONotificationActivityMock);

    Mockito.when(applicationContextMock.getBean(CreateDebtPositionWfConfig.class))
      .thenReturn(createDebtPositionWfConfigMock);

    wf = new CreateDebtPositionSyncWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(sendDebtPositionIONotificationActivityMock);
  }

  @Test
  void givenCreateDPSyncThenSuccess() {
    DebtPositionDTO debtPosition = buildDebtPositionDTO();

    Mockito.doNothing().when(sendDebtPositionIONotificationActivityMock)
      .sendMessage(debtPosition);

    wf.createDPSync(debtPosition);

    Mockito.verify(sendDebtPositionIONotificationActivityMock).sendMessage(debtPosition);
  }
}
