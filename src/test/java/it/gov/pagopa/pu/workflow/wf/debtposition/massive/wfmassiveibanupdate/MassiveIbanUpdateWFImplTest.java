package it.gov.pagopa.pu.workflow.wf.debtposition.massive.wfmassiveibanupdate;

import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInfo;
import it.gov.pagopa.payhub.activities.activity.debtposition.massive.MassiveIbanUpdateActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.massive.activity.ScheduleToSyncMassiveIbanUpdateWFActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.massive.config.MassiveDebtPositionWFConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;

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
  void givenNotToSyncWfWhenMassiveIbanUpdateThenActivityInvokedWithCorrectArgsAndNotSchedule() {
    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      WorkflowInfo workflowInfoMock = Mockito.mock(WorkflowInfo.class);
      workflowMock.when(Workflow::getInfo).thenReturn(workflowInfoMock);
      Mockito.when(workflowInfoMock.getWorkflowId()).thenReturn("WORKFLOW_ID");

      Mockito.when(massiveIbanUpdateActivityMock.massiveIbanUpdateRetrieveAndUpdateDp(ORG_ID, DPTO_ID, OLD_IBAN, NEW_IBAN, OLD_POSTAL_IBAN, NEW_POSTAL_IBAN))
        .thenReturn(false);

      Assertions.assertDoesNotThrow(() -> wf.massiveIbanUpdate(ORG_ID, DPTO_ID, OLD_IBAN, NEW_IBAN, OLD_POSTAL_IBAN, NEW_POSTAL_IBAN));
    }
  }

  @Test
  void givenNotToSyncWfWhenMassiveIbanUpdateThenActivityInvokedWithCorrectArgsAndSchedule() {
    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      WorkflowInfo workflowInfoMock = Mockito.mock(WorkflowInfo.class);
      workflowMock.when(Workflow::getInfo).thenReturn(workflowInfoMock);
      Mockito.when(workflowInfoMock.getWorkflowId()).thenReturn("WORKFLOW_ID");

      Mockito.when(massiveIbanUpdateActivityMock.massiveIbanUpdateRetrieveAndUpdateDp(ORG_ID, DPTO_ID, OLD_IBAN, NEW_IBAN, OLD_POSTAL_IBAN, NEW_POSTAL_IBAN))
        .thenReturn(true);
      Mockito.doNothing().when(scheduleToSyncMassiveIbanUpdateWFActivityMock).scheduleToSyncMassiveIbanUpdateWF(ORG_ID, DPTO_ID, OLD_IBAN, NEW_IBAN, OLD_POSTAL_IBAN, NEW_POSTAL_IBAN);

      Assertions.assertDoesNotThrow(() -> wf.massiveIbanUpdate(ORG_ID, DPTO_ID, OLD_IBAN, NEW_IBAN, OLD_POSTAL_IBAN, NEW_POSTAL_IBAN));
    }
  }

  @Test
  void givenToSyncWfWhenMassiveIbanUpdateThenDoesNotRetry() {
    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      WorkflowInfo workflowInfoMock = Mockito.mock(WorkflowInfo.class);
      workflowMock.when(Workflow::getInfo).thenReturn(workflowInfoMock);
      Mockito.when(workflowInfoMock.getWorkflowId()).thenReturn("WORKFLOW_ID_TO_SYNC");

      Mockito.when(massiveIbanUpdateActivityMock.massiveIbanUpdateRetrieveAndUpdateDp(ORG_ID, DPTO_ID, OLD_IBAN, NEW_IBAN, OLD_POSTAL_IBAN, NEW_POSTAL_IBAN))
        .thenReturn(false);

      wf.massiveIbanUpdate(ORG_ID, DPTO_ID, OLD_IBAN, NEW_IBAN, OLD_POSTAL_IBAN, NEW_POSTAL_IBAN);

      Mockito.verify(massiveIbanUpdateActivityMock)
        .massiveIbanUpdateRetrieveAndUpdateDp(ORG_ID, DPTO_ID, OLD_IBAN, NEW_IBAN, OLD_POSTAL_IBAN, NEW_POSTAL_IBAN);
    }
  }

  @Test
  void givenToSyncWfWhenMassiveIbanUpdateThenRetriesAndSleeps() {
    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      WorkflowInfo workflowInfoMock = Mockito.mock(WorkflowInfo.class);
      workflowMock.when(Workflow::getInfo).thenReturn(workflowInfoMock);
      Mockito.when(workflowInfoMock.getWorkflowId()).thenReturn("WORKFLOW_ID_TO_SYNC");

      Mockito.when(massiveIbanUpdateActivityMock.massiveIbanUpdateRetrieveAndUpdateDp(ORG_ID, DPTO_ID, OLD_IBAN, NEW_IBAN, OLD_POSTAL_IBAN, NEW_POSTAL_IBAN))
        .thenReturn(true, false);

      wf.massiveIbanUpdate(ORG_ID, DPTO_ID, OLD_IBAN, NEW_IBAN, OLD_POSTAL_IBAN, NEW_POSTAL_IBAN);

      Mockito.verify(massiveIbanUpdateActivityMock, Mockito.times(2))
        .massiveIbanUpdateRetrieveAndUpdateDp(ORG_ID, DPTO_ID, OLD_IBAN, NEW_IBAN, OLD_POSTAL_IBAN, NEW_POSTAL_IBAN);
    }
  }

  @Test
  void givenToSyncWfWhenMassiveIbanUpdateReachesLoopLimitThenContinueAsNew() {
    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      WorkflowInfo workflowInfoMock = Mockito.mock(WorkflowInfo.class);
      workflowMock.when(Workflow::getInfo).thenReturn(workflowInfoMock);
      Mockito.when(workflowInfoMock.getWorkflowId()).thenReturn("WORKFLOW_ID_TO_SYNC");

      Boolean[] ninetyNineTrues = new Boolean[99];
      Arrays.fill(ninetyNineTrues, true);

      Mockito.when(massiveIbanUpdateActivityMock.massiveIbanUpdateRetrieveAndUpdateDp(
          ORG_ID, DPTO_ID, OLD_IBAN, NEW_IBAN, OLD_POSTAL_IBAN, NEW_POSTAL_IBAN))
        .thenReturn(true, ninetyNineTrues)
        .thenReturn(false);

      workflowMock.when(() -> Workflow.continueAsNew(
        ORG_ID, DPTO_ID, OLD_IBAN, NEW_IBAN, OLD_POSTAL_IBAN, NEW_POSTAL_IBAN
      )).then(invocation -> null);

      wf.massiveIbanUpdate(ORG_ID, DPTO_ID, OLD_IBAN, NEW_IBAN, OLD_POSTAL_IBAN, NEW_POSTAL_IBAN);

      Mockito.verify(massiveIbanUpdateActivityMock, Mockito.times(101))
        .massiveIbanUpdateRetrieveAndUpdateDp(ORG_ID, DPTO_ID, OLD_IBAN, NEW_IBAN, OLD_POSTAL_IBAN, NEW_POSTAL_IBAN);

      workflowMock.verify(() -> Workflow.continueAsNew(ORG_ID, DPTO_ID, OLD_IBAN, NEW_IBAN, OLD_POSTAL_IBAN, NEW_POSTAL_IBAN), Mockito.times(1));
    }
  }
}
