package it.gov.pagopa.pu.workflow.service.wf.debtposition.sync.config;

import it.gov.pagopa.payhub.activities.dto.IONotificationMessage;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.FineWfExecutionConfig;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.WfExecutionConfig;
import it.gov.pagopa.pu.workflow.config.json.JsonConfig;
import it.gov.pagopa.pu.workflow.utils.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class WfExecutionConfigMergeServiceTest {

  private final WfExecutionConfigMergeService service = new WfExecutionConfigMergeService(new JsonConfig().objectMapper());

  @Test
  void givenNullInputsWhenMergeThenNull(){
    Assertions.assertNull(service.merge(null, null));
  }

  @Test
  void givenJustEfExecutionConfigWhenMergeThenReturnId(){
    // Given
    WfExecutionConfig executionConfig = new GenericWfExecutionConfig();

    // When
    WfExecutionConfig result = service.merge(null, executionConfig);

    // Then
    Assertions.assertSame(executionConfig, result);
  }

  @Test
  void givenJustDefaultWhenMergeThenReturnItsClone(){
    // Given
    GenericWfExecutionConfig defaultConfig = TestUtils.getPodamFactory().manufacturePojo(GenericWfExecutionConfig.class);

    // When
    WfExecutionConfig result = service.merge(defaultConfig, null);

    // Then
    Assertions.assertNotSame(defaultConfig, result);
    Assertions.assertEquals(defaultConfig, result);
    Assertions.assertInstanceOf(GenericWfExecutionConfig.class, result);

    Assertions.assertNotSame(defaultConfig.getIoMessages(), ((GenericWfExecutionConfig)result).getIoMessages());
  }

  @Test
  void givenNotExpectedOverrideTypeWhenMergeThenReturnDefaultClone(){
    // Given
    GenericWfExecutionConfig defaultConfig = TestUtils.getPodamFactory().manufacturePojo(GenericWfExecutionConfig.class);
    WfExecutionConfig unexpectedConfigType = TestUtils.getPodamFactory().manufacturePojo(FineWfExecutionConfig.class);

    // When
    WfExecutionConfig result = service.merge(defaultConfig, unexpectedConfigType);

    // Then
    Assertions.assertNotSame(defaultConfig, result);
    Assertions.assertEquals(defaultConfig, result);
    Assertions.assertInstanceOf(GenericWfExecutionConfig.class, result);

    Assertions.assertNotSame(defaultConfig.getIoMessages(), ((GenericWfExecutionConfig)result).getIoMessages());
  }

  @Test
  void whenMergeThenReturnOverriddenValues(){
    // Given
    GenericWfExecutionConfig defaultConfig = TestUtils.getPodamFactory().manufacturePojo(GenericWfExecutionConfig.class);

    GenericWfExecutionConfig wfExecutionConfig = new GenericWfExecutionConfig();
    IONotificationMessage createdMessageOverride = new IONotificationMessage(null, "OVERRIDE_MESSAGE");
    wfExecutionConfig.setIoMessages(GenericWfExecutionConfig.IONotificationBaseOpsMessages.builder()
        .created(createdMessageOverride)
      .build());

    // When
    WfExecutionConfig result = service.merge(defaultConfig, wfExecutionConfig);

    // Then
    Assertions.assertNotSame(defaultConfig, result);
    Assertions.assertInstanceOf(GenericWfExecutionConfig.class, result);
    GenericWfExecutionConfig castedResult = (GenericWfExecutionConfig) result;
    Assertions.assertNotSame(defaultConfig.getIoMessages(), castedResult.getIoMessages());

    Assertions.assertEquals(new IONotificationMessage(defaultConfig.getIoMessages().getCreated().getSubject(), createdMessageOverride.getMessage()), castedResult.getIoMessages().getCreated());
    Assertions.assertEquals(defaultConfig.getIoMessages().getUpdated(), castedResult.getIoMessages().getUpdated());
    Assertions.assertEquals(defaultConfig.getIoMessages().getDeleted(), castedResult.getIoMessages().getDeleted());
  }

  @Test
  void givenNoDefaultAndNotExpectedDefaultWhenMergeThenReturnNull(){
    // Given
    WfExecutionConfig unexpectedConfigType = TestUtils.getPodamFactory().manufacturePojo(FineWfExecutionConfig.class);

    // When
    WfExecutionConfig result = service.merge(null, unexpectedConfigType);

    // Then
    Assertions.assertNull(result);
  }
}
