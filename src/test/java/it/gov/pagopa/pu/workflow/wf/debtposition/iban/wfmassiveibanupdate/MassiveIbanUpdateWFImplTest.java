package it.gov.pagopa.pu.workflow.wf.debtposition.iban.wfmassiveibanupdate;

import it.gov.pagopa.payhub.activities.activity.debtposition.iban.MassiveIbanUpdateActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.iban.activity.ScheduleToSyncMassiveIbanUpdateWFActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.iban.config.MassiveDebtPositionWFConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
class MassiveIbanUpdateWFImplTest {

  public static final Long ORG_ID = 1L;
  public static final Long DPTO_ID = 2L;
  public static final String OLD_IBAN = "IT60X0542811101000000123456";
  public static final String NEW_IBAN = "IT60X0542811101000000654321";
  public static final String OLD_POSTAL_IBAN = "IT60X0760111100000000123456";
  public static final String NEW_POSTAL_IBAN = "IT60X0760111100000000654321";

  @Mock
  private MassiveIbanUpdateActivity massiveIbanUpdateActivityMock;
  @Mock
  private ScheduleToSyncMassiveIbanUpdateWFActivity scheduleToSyncMassiveIbanUpdateWFActivityMock;

  private MassiveIbanUpdateWFImpl wf;

  @BeforeEach
  void setUp() {
    MassiveDebtPositionWFConfig wfConfigMock = Mockito.mock(MassiveDebtPositionWFConfig.class);
    ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    Mockito.when(wfConfigMock.buildMassiveIbanUpdateActivityStub()).thenReturn(massiveIbanUpdateActivityMock);
    Mockito.when(wfConfigMock.buildScheduleToSyncMassiveIbanUpdateWFActivityStub()).thenReturn(scheduleToSyncMassiveIbanUpdateWFActivityMock);
    Mockito.when(applicationContextMock.getBean(MassiveDebtPositionWFConfig.class)).thenReturn(wfConfigMock);

    wf = new MassiveIbanUpdateWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(massiveIbanUpdateActivityMock, scheduleToSyncMassiveIbanUpdateWFActivityMock);
  }

  @Test
  void givenValidParamsWhenMassiveIbanUpdateThenActivityInvokedWithCorrectArgsAndNotSchedule() {
    Mockito.when(massiveIbanUpdateActivityMock.massiveIbanUpdateRetrieveAndUpdateDp(ORG_ID, DPTO_ID, OLD_IBAN, NEW_IBAN, OLD_POSTAL_IBAN, NEW_POSTAL_IBAN))
      .thenReturn(false);

    // When
    wf.massiveIbanUpdate(ORG_ID, DPTO_ID, OLD_IBAN, NEW_IBAN, OLD_POSTAL_IBAN, NEW_POSTAL_IBAN);

    // Then
    Mockito.verify(massiveIbanUpdateActivityMock)
      .massiveIbanUpdateRetrieveAndUpdateDp(ORG_ID, DPTO_ID, OLD_IBAN, NEW_IBAN, OLD_POSTAL_IBAN, NEW_POSTAL_IBAN);
  }

  @Test
  void givenValidParamsWhenMassiveIbanUpdateThenActivityInvokedWithCorrectArgsAndSchedule() {
    Mockito.when(massiveIbanUpdateActivityMock.massiveIbanUpdateRetrieveAndUpdateDp(ORG_ID, DPTO_ID, OLD_IBAN, NEW_IBAN, OLD_POSTAL_IBAN, NEW_POSTAL_IBAN))
      .thenReturn(true);
    Mockito.doNothing().when(scheduleToSyncMassiveIbanUpdateWFActivityMock).scheduleToSyncMassiveIbanUpdateWF(ORG_ID, DPTO_ID, OLD_IBAN, NEW_IBAN, OLD_POSTAL_IBAN, NEW_POSTAL_IBAN);

    Assertions.assertDoesNotThrow(() -> wf.massiveIbanUpdate(ORG_ID, DPTO_ID, OLD_IBAN, NEW_IBAN, OLD_POSTAL_IBAN, NEW_POSTAL_IBAN));
  }
}
