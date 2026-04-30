package it.gov.pagopa.pu.workflow.wf.pagopa.paidinstallments.wf;

import it.gov.pagopa.payhub.activities.activity.ingestionflow.receipt.DeletePaidInstallmentsOnPagoPaActivity;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.wf.pagopa.paidinstallments.config.DeletePaidInstallmentsOnPagoPaWfConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeletePaidInstallmentsOnPagoPaWFTest {

  @Mock
  DeletePaidInstallmentsOnPagoPaActivity  activityMock;

  DeletePaidInstallmentsOnPagoPaWFImpl workflow;

  @BeforeEach
  void setUp() {
    DeletePaidInstallmentsOnPagoPaWfConfig configMock = mock(DeletePaidInstallmentsOnPagoPaWfConfig.class);
    ApplicationContext applicationContextMock = mock(ApplicationContext.class);
    when(configMock.buildDeletePaidInstallmentsOnPagoActivityStub()).thenReturn(activityMock);
    when(applicationContextMock.getBean(DeletePaidInstallmentsOnPagoPaWfConfig.class)).thenReturn(configMock);

    workflow = new DeletePaidInstallmentsOnPagoPaWFImpl();
    workflow.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(activityMock);
  }

  @Test
  void givenDataThenExecutesActivity() {
    // Given
    DebtPositionDTO debtPositionDTO = new DebtPositionDTO();
    Long receiptId = 1L;

    // When
    workflow.deletePaidInstallments(debtPositionDTO, 1L);

    // Then
    verify(activityMock, times(1)).deletePaidInstallmentsOnPagoPa(debtPositionDTO, receiptId);
  }
}
