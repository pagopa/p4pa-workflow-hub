package it.gov.pagopa.pu.workflow.service.debtposition.sync;

import it.gov.pagopa.payhub.activities.connector.workflowhub.dto.WfExecutionParameters;
import it.gov.pagopa.payhub.activities.dto.debtposition.GenericWfExecutionConfig;
import it.gov.pagopa.payhub.activities.dto.debtposition.WfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.model.DebtPositionWorkflowType;
import it.gov.pagopa.pu.workflow.model.WorkflowTypeOrg;
import it.gov.pagopa.pu.workflow.repository.DebtPositionWorkflowTypeRepository;
import it.gov.pagopa.pu.workflow.repository.WorkflowTypeOrgRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class WfExecutionConfigHandlerServiceTest {

  @Mock
  private DebtPositionWorkflowTypeRepository debtPositionWorkflowTypeRepositoryMock;
  @Mock
  private WorkflowTypeOrgRepository workflowTypeOrgRepositoryMock;
  @Mock
  private WfExecutionConfigMergeService mergeServiceMock;

  private WfExecutionConfigHandlerService service;

  @BeforeEach
  void init(){
    service = new WfExecutionConfigHandlerService(
      debtPositionWorkflowTypeRepositoryMock,
      workflowTypeOrgRepositoryMock,
      mergeServiceMock
    );
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      debtPositionWorkflowTypeRepositoryMock,
      workflowTypeOrgRepositoryMock,
      mergeServiceMock
    );
  }

  @Test
  void givenAlreadyPersistedWfExecutionConfigWhenPersistAndConfigureThenReturnId(){
    // Given
    DebtPositionDTO debtPositionDTO = new DebtPositionDTO();
    debtPositionDTO.setDebtPositionId(1L);
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters(true, false, new GenericWfExecutionConfig());

    WfExecutionConfig expectedResult = Mockito.mock(WfExecutionConfig.class);
    DebtPositionWorkflowType storedDpWfConfig = new DebtPositionWorkflowType();
    storedDpWfConfig.setExecutionConfig(expectedResult);

    Mockito.when(debtPositionWorkflowTypeRepositoryMock.findById(1L))
      .thenReturn(Optional.of(storedDpWfConfig));

    // When
    service.persistAndConfigure(debtPositionDTO, wfExecutionParameters);

    // Then
    Assertions.assertSame(expectedResult, wfExecutionParameters.getWfExecutionConfig());
  }

  @Test
  void givenGenericWfWhenPersistAndConfigureThenStoreAndReturnAskedConfig(){
    // Given
    DebtPositionDTO debtPositionDTO = new DebtPositionDTO();
    debtPositionDTO.setDebtPositionId(1L);
    debtPositionDTO.setDebtPositionTypeOrgId(2L);

    GenericWfExecutionConfig expectedResult = new GenericWfExecutionConfig();
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters(true, false, expectedResult);

    DebtPositionWorkflowType expectedStored = new DebtPositionWorkflowType();
    expectedStored.setDebtPositionId(1L);
    expectedStored.setExecutionConfig(expectedResult);

    Mockito.when(debtPositionWorkflowTypeRepositoryMock.findById(1L))
      .thenReturn(Optional.empty());
    Mockito.when(workflowTypeOrgRepositoryMock.findById(2L))
      .thenReturn(Optional.empty());
    Mockito.when(mergeServiceMock.merge(null, expectedResult))
      .thenReturn(expectedResult);

    // When
    service.persistAndConfigure(debtPositionDTO, wfExecutionParameters);

    // Then
    Assertions.assertSame(expectedResult, wfExecutionParameters.getWfExecutionConfig());
    Mockito.verify(debtPositionWorkflowTypeRepositoryMock)
      .save(expectedStored);
  }

  @Test
  void givenGenericWfWhenPersistAndConfigureThenReturnAskedConfig(){
    // Given
    DebtPositionDTO debtPositionDTO = new DebtPositionDTO();
    debtPositionDTO.setDebtPositionId(1L);
    debtPositionDTO.setDebtPositionTypeOrgId(2L);

    GenericWfExecutionConfig dpWfConfig = new GenericWfExecutionConfig();
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters(true, false, dpWfConfig);

    WfExecutionConfig defaultConfig = Mockito.mock(WfExecutionConfig.class);
    WorkflowTypeOrg wfOrgDefaultConfig = new WorkflowTypeOrg();
    wfOrgDefaultConfig.setDefaultExecutionConfig(defaultConfig);

    WfExecutionConfig expectedResult = new GenericWfExecutionConfig();
    DebtPositionWorkflowType expectedStored = new DebtPositionWorkflowType();
    expectedStored.setDebtPositionId(1L);
    expectedStored.setWorkflowTypeOrgId(2L);
    expectedStored.setExecutionConfig(expectedResult);


    Mockito.when(debtPositionWorkflowTypeRepositoryMock.findById(1L))
      .thenReturn(Optional.empty());
    Mockito.when(workflowTypeOrgRepositoryMock.findById(2L))
      .thenReturn(Optional.of(wfOrgDefaultConfig));
    Mockito.when(mergeServiceMock.merge(Mockito.same(defaultConfig), Mockito.same(dpWfConfig)))
      .thenReturn(expectedResult);

    // When
    service.persistAndConfigure(debtPositionDTO, wfExecutionParameters);

    // Then
    Assertions.assertSame(expectedResult, wfExecutionParameters.getWfExecutionConfig());
    Mockito.verify(debtPositionWorkflowTypeRepositoryMock)
      .save(expectedStored);
  }
}
