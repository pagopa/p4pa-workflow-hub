package it.gov.pagopa.pu.workflow.wf.assessmentsregistry.wfassessmentsregistry;

import static it.gov.pagopa.pu.workflow.utils.faker.DebtPositionFaker.buildDebtPositionDTO;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.gov.pagopa.payhub.activities.activity.assessments.AssessmentsRegistryCreationActivity;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.wf.assessmentsregistry.config.CreateAssessmentsRegistryWFConfig;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
class CreateAssessmentsRegistryWFImplTest {

  @Mock
  private AssessmentsRegistryCreationActivity assessmentsRegistryCreationActivityMock;

  private CreateAssessmentsRegistryWFImpl workflow;

  @BeforeEach
  void setUp() {
    CreateAssessmentsRegistryWFConfig configMock = mock(CreateAssessmentsRegistryWFConfig.class);
    ApplicationContext applicationContextMock = mock(ApplicationContext.class);
    when(configMock.buildAssessmentsRegistryCreationActivityStub()).thenReturn(assessmentsRegistryCreationActivityMock);

    when(applicationContextMock.getBean(CreateAssessmentsRegistryWFConfig.class)).thenReturn(configMock);

    workflow = new CreateAssessmentsRegistryWFImpl();
    workflow.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(assessmentsRegistryCreationActivityMock);
  }

  @Test
  void givenValidDTOAndIudListWhenCreateAssessmentsRegistryThenVerify() {
    // Given
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    List<String> iudList = List.of("IUD");

    // When
    workflow.createAssessmentsRegistry(debtPositionDTO, iudList);

    // Then
    verify(assessmentsRegistryCreationActivityMock).createAssessmentsRegistryByDebtPositionDTOAndIudList(debtPositionDTO, iudList);
  }
}
