package it.gov.pagopa.pu.workflow.config.temporal;

import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

@ExtendWith(MockitoExtension.class)
class TemporalWFServiceCustomizerTest {

  @Mock
  private TemporalConfig.TemporalTimeoutsConfig temporalTimeoutsConfigMock;
  @Mock
  private TemporalConfig temporalConfigMock;

  private TemporalWFServiceCustomizer customizer;

  @BeforeEach
  void init(){
    customizer = new TemporalWFServiceCustomizer(temporalConfigMock);

    Mockito.lenient()
      .when(temporalConfigMock.getTimeouts())
      .thenReturn(temporalTimeoutsConfigMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      temporalConfigMock,
      temporalTimeoutsConfigMock
    );
  }

  @Test
  void whenCustomizeThenReadConfig(){
    // Given
    WorkflowServiceStubsOptions.Builder optionBuilderMock = Mockito.mock(WorkflowServiceStubsOptions.Builder.class);

    long systemInfoTimeoutSeconds = 1L;
    long rpcLongPollTimeoutSeconds = 2L;
    long rpcQueryTimeoutSeconds = 3L;
    long rpcGenericTimeoutSeconds = 4L;

    Mockito.when(temporalTimeoutsConfigMock.getSystemInfo()).thenReturn(systemInfoTimeoutSeconds);
    Mockito.when(temporalTimeoutsConfigMock.getRpcLongPoll()).thenReturn(rpcLongPollTimeoutSeconds);
    Mockito.when(temporalTimeoutsConfigMock.getRpcQuery()).thenReturn(rpcQueryTimeoutSeconds);
    Mockito.when(temporalTimeoutsConfigMock.getRpcGeneric()).thenReturn(rpcGenericTimeoutSeconds);

    Mockito.when(optionBuilderMock.setSystemInfoTimeout(Duration.ofSeconds(systemInfoTimeoutSeconds)))
      .thenReturn(optionBuilderMock);
    Mockito.when(optionBuilderMock.setRpcLongPollTimeout(Duration.ofSeconds(rpcLongPollTimeoutSeconds)))
      .thenReturn(optionBuilderMock);
    Mockito.when(optionBuilderMock.setRpcQueryTimeout(Duration.ofSeconds(rpcQueryTimeoutSeconds)))
      .thenReturn(optionBuilderMock);
    Mockito.when(optionBuilderMock.setRpcTimeout(Duration.ofSeconds(rpcGenericTimeoutSeconds)))
      .thenReturn(optionBuilderMock);

    // When
    WorkflowServiceStubsOptions.Builder result = customizer.customize(optionBuilderMock);

    // Then
    Assertions.assertSame(optionBuilderMock, result);

    Mockito.verify(temporalConfigMock, Mockito.times(4)).getTimeouts();
    Mockito.verifyNoMoreInteractions(optionBuilderMock);
  }
}
