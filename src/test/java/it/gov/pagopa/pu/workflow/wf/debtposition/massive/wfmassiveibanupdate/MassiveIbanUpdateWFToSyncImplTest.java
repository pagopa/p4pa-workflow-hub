package it.gov.pagopa.pu.workflow.wf.debtposition.massive.wfmassiveibanupdate;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.debtposition.massive.MassiveIbanUpdateActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.massive.config.MassiveDebtPositionWFConfig;
import it.gov.pagopa.pu.workflow.wf.debtposition.massive.wfmassiveibanupdatetosync.MassiveIbanUpdateWFToSyncImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

@ExtendWith(MockitoExtension.class)
class MassiveIbanUpdateWFToSyncImplTest {
  public static final Long ORG_ID = 1L;
  public static final Long DPTO_ID = 2L;
  public static final String OLD_IBAN = "IT60X0542811101000000123456";
  public static final String NEW_IBAN = "IT60X0542811101000000654321";
  public static final String OLD_POSTAL_IBAN = "IT60X0760111100000000123456";
  public static final String NEW_POSTAL_IBAN = "IT60X0760111100000000654321";

  @Mock
  private MassiveIbanUpdateActivity massiveIbanUpdateActivityMock;

  private MassiveIbanUpdateWFToSyncImpl wf;

  @BeforeEach
  void setUp() {
    MassiveDebtPositionWFConfig wfConfigMock = Mockito.mock(MassiveDebtPositionWFConfig.class);
    ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    Mockito.when(wfConfigMock.buildMassiveIbanUpdateActivityStub()).thenReturn(massiveIbanUpdateActivityMock);
    Mockito.when(applicationContextMock.getBean(MassiveDebtPositionWFConfig.class)).thenReturn(wfConfigMock);

    wf = new MassiveIbanUpdateWFToSyncImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(massiveIbanUpdateActivityMock);
  }

  @Test
  void givenNoRetryWhenMassiveIbanUpdateThenCompletesImmediately() {
    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      Mockito.when(massiveIbanUpdateActivityMock.massiveIbanUpdateRetrieveAndUpdateDp(ORG_ID, DPTO_ID, OLD_IBAN, NEW_IBAN, OLD_POSTAL_IBAN, NEW_POSTAL_IBAN))
        .thenReturn(false);

      wf.massiveIbanUpdate(ORG_ID, DPTO_ID, OLD_IBAN, NEW_IBAN, OLD_POSTAL_IBAN, NEW_POSTAL_IBAN);

      Mockito.verify(massiveIbanUpdateActivityMock)
        .massiveIbanUpdateRetrieveAndUpdateDp(ORG_ID, DPTO_ID, OLD_IBAN, NEW_IBAN, OLD_POSTAL_IBAN, NEW_POSTAL_IBAN);
      workflowMock.verify(() -> Workflow.sleep(Mockito.any(Duration.class)), Mockito.never());
      workflowMock.verify(() -> Workflow.continueAsNew(
        ORG_ID, DPTO_ID, OLD_IBAN, NEW_IBAN, OLD_POSTAL_IBAN, NEW_POSTAL_IBAN
      ), Mockito.never());
    }
  }

  @Test
  void givenSomeRetriesWhenMassiveIbanUpdateThenSleepsAndCompletes() {
    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      Mockito.when(massiveIbanUpdateActivityMock.massiveIbanUpdateRetrieveAndUpdateDp(ORG_ID, DPTO_ID, OLD_IBAN, NEW_IBAN, OLD_POSTAL_IBAN, NEW_POSTAL_IBAN))
        .thenReturn(true, false);

      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class))).then(invocation -> null);

      wf.massiveIbanUpdate(ORG_ID, DPTO_ID, OLD_IBAN, NEW_IBAN, OLD_POSTAL_IBAN, NEW_POSTAL_IBAN);

      Mockito.verify(massiveIbanUpdateActivityMock, Mockito.times(2))
        .massiveIbanUpdateRetrieveAndUpdateDp(ORG_ID, DPTO_ID, OLD_IBAN, NEW_IBAN, OLD_POSTAL_IBAN, NEW_POSTAL_IBAN);
      workflowMock.verify(() -> Workflow.sleep(Duration.of(300, ChronoUnit.SECONDS)), Mockito.times(1));
      workflowMock.verify(() -> Workflow.continueAsNew(
        ORG_ID, DPTO_ID, OLD_IBAN, NEW_IBAN, OLD_POSTAL_IBAN, NEW_POSTAL_IBAN
      ), Mockito.never());
    }
  }

  @Test
  void givenReachesLoopLimitWhenMassiveIbanUpdateThenContinueAsNew() {
    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      Boolean[] ninetyNineTrues = new Boolean[99];
      Arrays.fill(ninetyNineTrues, true);

      Mockito.when(massiveIbanUpdateActivityMock.massiveIbanUpdateRetrieveAndUpdateDp(ORG_ID, DPTO_ID, OLD_IBAN, NEW_IBAN, OLD_POSTAL_IBAN, NEW_POSTAL_IBAN))
        .thenReturn(true, ninetyNineTrues)
        .thenReturn(false);

      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class))).then(invocation -> null);

      workflowMock.when(() -> Workflow.continueAsNew(
        ORG_ID, DPTO_ID, OLD_IBAN, NEW_IBAN, OLD_POSTAL_IBAN, NEW_POSTAL_IBAN
      )).then(invocation -> null);

      wf.massiveIbanUpdate(ORG_ID, DPTO_ID, OLD_IBAN, NEW_IBAN, OLD_POSTAL_IBAN, NEW_POSTAL_IBAN);

      Mockito.verify(massiveIbanUpdateActivityMock, Mockito.times(101))
        .massiveIbanUpdateRetrieveAndUpdateDp(ORG_ID, DPTO_ID, OLD_IBAN, NEW_IBAN, OLD_POSTAL_IBAN, NEW_POSTAL_IBAN);

      workflowMock.verify(() -> Workflow.sleep(Duration.of(300, ChronoUnit.SECONDS)), Mockito.times(100));

      workflowMock.verify(() -> Workflow.continueAsNew(
        ORG_ID, DPTO_ID, OLD_IBAN, NEW_IBAN, OLD_POSTAL_IBAN, NEW_POSTAL_IBAN
      ), Mockito.times(1));
    }
  }
}
