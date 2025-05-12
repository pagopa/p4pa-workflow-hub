package it.gov.pagopa.pu.workflow.service;

import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.FineWfExecutionConfig;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.WfExecutionConfig;
import it.gov.pagopa.pu.workflow.exception.custom.InvalidWfExecutionConfigException;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowTypeNotFoundException;
import it.gov.pagopa.pu.workflow.model.WorkflowType;
import it.gov.pagopa.pu.workflow.model.WorkflowTypeOrg;
import it.gov.pagopa.pu.workflow.repository.WorkflowTypeOrgRepository;
import it.gov.pagopa.pu.workflow.repository.WorkflowTypeRepository;
import it.gov.pagopa.pu.workflow.service.wf.debtposition.sync.config.WfExecutionConfigMergeService;
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
class WorkflowTypeOrgSaveServiceTest {

  @Mock
  private WorkflowTypeRepository workflowTypeRepositoryMock;
  @Mock
  private WorkflowTypeOrgRepository repositoryMock;
  @Mock
  private WfExecutionConfigMergeService mergeServiceMock;

  private WorkflowTypeOrgSaveService service;

  @BeforeEach
  void init(){
    service = new WorkflowTypeOrgSaveServiceImpl(workflowTypeRepositoryMock, mergeServiceMock, repositoryMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(workflowTypeRepositoryMock, mergeServiceMock, repositoryMock);
  }

  @Test
  void givenNotExistentWorkflowTypeWhenSaveThenThrowWorkflowTypeNotFoundException(){
    // Given
    WorkflowTypeOrg entity = WorkflowTypeOrg.builder()
      .workflowTypeId(0L)
      .build();

    Mockito.when(workflowTypeRepositoryMock.findById(Mockito.same(entity.getWorkflowTypeId())))
        .thenReturn(Optional.empty());

    // When
    WorkflowTypeNotFoundException result = Assertions.assertThrows(WorkflowTypeNotFoundException.class, () -> service.save(entity));

    // Then
    Assertions.assertEquals("Cannot find WorkflowType having id 0",
      result.getMessage());
  }

  @Test
  void givenUnexpectedWfExecutionConfigWhenSaveThenThrowInvalidWfExecutionConfigException(){
    // Given
    WorkflowTypeOrg entity = WorkflowTypeOrg.builder()
      .workflowTypeId(0L)
      .defaultExecutionConfig(new GenericWfExecutionConfig())
      .build();

    WorkflowType workflowType = WorkflowType.builder()
      .defaultExecutionConfig(new FineWfExecutionConfig())
      .build();

    Mockito.when(workflowTypeRepositoryMock.findById(Mockito.same(entity.getWorkflowTypeId())))
      .thenReturn(Optional.of(workflowType));

    // When
    InvalidWfExecutionConfigException result = Assertions.assertThrows(InvalidWfExecutionConfigException.class, () -> service.save(entity));

    // Then
    Assertions.assertEquals("Invalid execution config type for workflowTypeId: 0. Expected: FineWfExecutionConfig, Found: GenericWfExecutionConfig",
      result.getMessage());
  }

  @Test
  void whenSaveThenSaveMergedConfig(){
    // Given
    WorkflowTypeOrg entity = WorkflowTypeOrg.builder()
      .workflowTypeId(0L)
      .defaultExecutionConfig(new GenericWfExecutionConfig())
      .build();

    WorkflowType workflowType = WorkflowType.builder()
      .defaultExecutionConfig(new GenericWfExecutionConfig())
      .build();

    WfExecutionConfig mergedConfig = new GenericWfExecutionConfig();
    WorkflowTypeOrg expectedResult = new WorkflowTypeOrg();

    Mockito.when(workflowTypeRepositoryMock.findById(Mockito.same(entity.getWorkflowTypeId())))
      .thenReturn(Optional.of(workflowType));

    Mockito.when(mergeServiceMock.merge(Mockito.same(workflowType.getDefaultExecutionConfig()), Mockito.same(entity.getDefaultExecutionConfig())))
      .thenReturn(mergedConfig);

    Mockito.when(repositoryMock.save(Mockito.same(entity)))
      .thenReturn(expectedResult);

    // When
    WorkflowTypeOrg result = service.save(entity);

    // Then
    Assertions.assertSame(expectedResult, result);
    Assertions.assertSame(mergedConfig, entity.getDefaultExecutionConfig());
  }
}
