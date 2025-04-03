package it.gov.pagopa.pu.workflow.service.debtposition.sync.config;

import it.gov.pagopa.payhub.activities.connector.workflowhub.dto.WfExecutionParameters;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.FineWfExecutionConfig;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.WfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.exception.custom.InvalidWfExecutionConfigException;
import it.gov.pagopa.pu.workflow.model.DebtPositionWorkflowType;
import it.gov.pagopa.pu.workflow.model.WorkflowTypeOrg;
import it.gov.pagopa.pu.workflow.repository.DebtPositionWorkflowTypeRepository;
import it.gov.pagopa.pu.workflow.repository.WorkflowTypeOrgRepository;
import it.gov.pagopa.pu.workflow.service.DataCipherService;
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
  private DataCipherService dataCipherServiceMock;
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
      dataCipherServiceMock,
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
  void givenAlreadyPersistedWfExecutionConfigWhenPersistAndConfigureThenReturnIt(){
    givenAlreadyPersistedWfExecutionConfigWhenPersistAndConfigureThenReturnIt(null);
  }
  @Test
  void givenAlreadyPersistedWfExecutionConfigAndProvidedNewOneWhenPersistAndConfigureThenIgnoreInputAndReturnStored(){
    givenAlreadyPersistedWfExecutionConfigWhenPersistAndConfigureThenReturnIt(new GenericWfExecutionConfig());
  }
  void givenAlreadyPersistedWfExecutionConfigWhenPersistAndConfigureThenReturnIt(WfExecutionConfig providedExecutionConfig){
    // Given
    DebtPositionDTO debtPositionDTO = new DebtPositionDTO();
    debtPositionDTO.setDebtPositionId(1L);
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters(true, false, providedExecutionConfig);

    byte[] cipheredDpWfExecutionConfig = new byte[0];
    WfExecutionConfig expectedResult = Mockito.mock(WfExecutionConfig.class);
    DebtPositionWorkflowType storedDpWfConfig = new DebtPositionWorkflowType();
    storedDpWfConfig.setExecutionConfig(cipheredDpWfExecutionConfig);

    Mockito.when(debtPositionWorkflowTypeRepositoryMock.findById(1L))
      .thenReturn(Optional.of(storedDpWfConfig));
    Mockito.when(dataCipherServiceMock.decryptObj(Mockito.same(cipheredDpWfExecutionConfig), Mockito.eq(WfExecutionConfig.class)))
      .thenReturn(expectedResult);

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

    byte[] cipheredDpWfExecutionConfig = new byte[0];
    DebtPositionWorkflowType expectedStored = new DebtPositionWorkflowType();
    expectedStored.setDebtPositionId(1L);
    expectedStored.setExecutionConfig(cipheredDpWfExecutionConfig);

    Mockito.when(debtPositionWorkflowTypeRepositoryMock.findById(1L))
      .thenReturn(Optional.empty());
    Mockito.when(workflowTypeOrgRepositoryMock.findById(2L))
      .thenReturn(Optional.empty());
    Mockito.when(mergeServiceMock.merge(null, expectedResult))
      .thenReturn(expectedResult);
    Mockito.when(dataCipherServiceMock.encryptObj(Mockito.same(expectedResult)))
      .thenReturn(cipheredDpWfExecutionConfig);

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
    wfOrgDefaultConfig.setDebtPositionTypeOrgId(2L);
    wfOrgDefaultConfig.setDefaultExecutionConfig(defaultConfig);

    byte[] cipheredDpWfExecutionConfig = new byte[0];
    WfExecutionConfig expectedResult = new GenericWfExecutionConfig();
    DebtPositionWorkflowType expectedStored = new DebtPositionWorkflowType();
    expectedStored.setDebtPositionId(1L);
    expectedStored.setWorkflowTypeOrgId(2L);
    expectedStored.setExecutionConfig(cipheredDpWfExecutionConfig);


    Mockito.when(debtPositionWorkflowTypeRepositoryMock.findById(1L))
      .thenReturn(Optional.empty());
    Mockito.when(workflowTypeOrgRepositoryMock.findById(2L))
      .thenReturn(Optional.of(wfOrgDefaultConfig));
    Mockito.when(mergeServiceMock.merge(Mockito.same(defaultConfig), Mockito.same(dpWfConfig)))
      .thenReturn(expectedResult);
    Mockito.when(dataCipherServiceMock.encryptObj(Mockito.same(expectedResult)))
      .thenReturn(cipheredDpWfExecutionConfig);

    // When
    service.persistAndConfigure(debtPositionDTO, wfExecutionParameters);

    // Then
    Assertions.assertSame(expectedResult, wfExecutionParameters.getWfExecutionConfig());
    Mockito.verify(debtPositionWorkflowTypeRepositoryMock)
      .save(expectedStored);
  }

  @Test
  void givenNoWfExecutionConfigWhenPersistAndConfigureThenDoNothing(){
    // Given
    DebtPositionDTO debtPositionDTO = new DebtPositionDTO();
    debtPositionDTO.setDebtPositionId(1L);
    debtPositionDTO.setDebtPositionTypeOrgId(2L);

    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters(true, false, null);

    Mockito.when(debtPositionWorkflowTypeRepositoryMock.findById(1L))
      .thenReturn(Optional.empty());
    Mockito.when(workflowTypeOrgRepositoryMock.findById(2L))
      .thenReturn(Optional.empty());
    Mockito.when(mergeServiceMock.merge(Mockito.isNull(), Mockito.isNull()))
      .thenReturn(null);

    // When
    service.persistAndConfigure(debtPositionDTO, wfExecutionParameters);

    // Then
    Assertions.assertNull(wfExecutionParameters.getWfExecutionConfig());
  }

  @Test
  void givenValidFineWfExecutionConfigWhenFindStoredExecutionConfigThenOk() {
    // Given
    long debtPositionId = 1L;
    FineWfExecutionConfig fineConfig = new FineWfExecutionConfig();
    byte[] ciphered = new byte[0];

    DebtPositionWorkflowType dpWfType = new DebtPositionWorkflowType();
    dpWfType.setExecutionConfig(ciphered);

    Mockito.when(debtPositionWorkflowTypeRepositoryMock.findById(debtPositionId))
      .thenReturn(Optional.of(dpWfType));
    Mockito.when(dataCipherServiceMock.decryptObj(ciphered, WfExecutionConfig.class))
      .thenReturn(fineConfig);

    // When
    FineWfExecutionConfig result = service.findStoredExecutionConfig(debtPositionId, FineWfExecutionConfig.class);

    // Then
    Assertions.assertSame(fineConfig, result);
  }

  @Test
  void givenNoExecutionConfigWhenFindStoredExecutionConfigThenThrowInvalidWfExecutionConfigException() {
    // Given
    long debtPositionId = 1L;
    Mockito.when(debtPositionWorkflowTypeRepositoryMock.findById(debtPositionId))
      .thenReturn(Optional.empty());

    // Then
    Assertions.assertThrows(InvalidWfExecutionConfigException.class,
      () -> service.findStoredExecutionConfig(debtPositionId, FineWfExecutionConfig.class));
  }

  @Test
  void givenWrongExecutionConfigTypeWhenFindStoredExecutionConfigThenThrowInvalidWfExecutionConfigException() {
    // Given
    long debtPositionId = 1L;
    GenericWfExecutionConfig wrongConfig = new GenericWfExecutionConfig();
    byte[] ciphered = new byte[0];

    DebtPositionWorkflowType dpWfType = new DebtPositionWorkflowType();
    dpWfType.setExecutionConfig(ciphered);

    Mockito.when(debtPositionWorkflowTypeRepositoryMock.findById(debtPositionId))
      .thenReturn(Optional.of(dpWfType));
    Mockito.when(dataCipherServiceMock.decryptObj(ciphered, WfExecutionConfig.class))
      .thenReturn(wrongConfig);

    // Then
    Assertions.assertThrows(InvalidWfExecutionConfigException.class,
      () -> service.findStoredExecutionConfig(debtPositionId, FineWfExecutionConfig.class));
  }

}
