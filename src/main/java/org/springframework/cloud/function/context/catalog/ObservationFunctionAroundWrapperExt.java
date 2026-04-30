package org.springframework.cloud.function.context.catalog;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.function.observability.FunctionObservationConvention;
import org.springframework.cloud.function.observability.ObservationFunctionAroundWrapper;

/**
 * Extension of {@link ObservationFunctionAroundWrapper} needed in order to set wrapped false in case of exception retried.<BR />
 * It was not more configuring observation see {@link org.springframework.cloud.function.context.catalog.SimpleFunctionRegistry.FunctionInvocationWrapper#apply(java.lang.Object)}  */
public class ObservationFunctionAroundWrapperExt extends ObservationFunctionAroundWrapper {

  public ObservationFunctionAroundWrapperExt(ObservationRegistry registry,
                                             ObjectProvider<FunctionObservationConvention> functionObservationConvention) {
    //noinspection DataFlowIssue: Created as done in org.springframework.cloud.function.observability.ObservationAutoConfiguration.observationFunctionAroundWrapper
    super(registry, functionObservationConvention.getIfAvailable(() -> null));
  }

  @Override
  protected Object doApply(Object message, SimpleFunctionRegistry.FunctionInvocationWrapper targetFunction) {
    try {
      return super.doApply(message, targetFunction);
    } finally {
      targetFunction.wrapped=false;
    }
  }
}
