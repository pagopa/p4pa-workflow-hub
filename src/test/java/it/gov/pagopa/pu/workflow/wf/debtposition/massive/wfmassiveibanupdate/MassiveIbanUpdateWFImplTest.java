package it.gov.pagopa.pu.workflow.wf.debtposition.massive.wfmassiveibanupdate;

import it.gov.pagopa.payhub.activities.activity.debtposition.massive.MassiveIbanUpdateActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.massive.config.MassiveDebtPositionWFConfig;
import org.junit.jupiter.api.AfterEach;
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

  private MassiveIbanUpdateWFImpl wf;

  @BeforeEach
  void setUp() {
    MassiveDebtPositionWFConfig wfConfigMock = Mockito.mock(MassiveDebtPositionWFConfig.class);
    ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    Mockito.when(wfConfigMock.buildMassiveIbanUpdateActivityStub()).thenReturn(massiveIbanUpdateActivityMock);
    Mockito.when(applicationContextMock.getBean(MassiveDebtPositionWFConfig.class)).thenReturn(wfConfigMock);

    wf = new MassiveIbanUpdateWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(massiveIbanUpdateActivityMock);
  }

  @Test
  void givenValidParamsWhenMassiveIbanUpdateThenActivityInvokedWithCorrectArgs() {
    // When
    wf.massiveIbanUpdate(ORG_ID, DPTO_ID, OLD_IBAN, NEW_IBAN, OLD_POSTAL_IBAN, NEW_POSTAL_IBAN);

    // Then
    Mockito.verify(massiveIbanUpdateActivityMock)
      .massiveIbanUpdateRetrieveAndUpdateDp(ORG_ID, DPTO_ID, NEW_IBAN, OLD_IBAN, OLD_POSTAL_IBAN, NEW_POSTAL_IBAN);
  }
}
