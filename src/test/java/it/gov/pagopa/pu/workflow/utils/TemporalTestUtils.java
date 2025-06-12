package it.gov.pagopa.pu.workflow.utils;

import io.temporal.activity.ActivityOptions;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.spring.boot.ActivityImpl;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Functions;
import io.temporal.workflow.Workflow;
import it.gov.pagopa.pu.workflow.config.temporal.BaseWfConfig;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import org.junit.jupiter.api.Assertions;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class TemporalTestUtils {
  private TemporalTestUtils(){}

  // Helper method to check that the method exists on both classes
  public static void assertSignalMethodExists(Class<?> wfInterface, String methodName, Class<?>... parameterTypes) {
    try {
      Assertions.assertNotNull(wfInterface.getMethod(methodName, parameterTypes));
    } catch (NoSuchMethodException e) {
      throw new IllegalArgumentException("signalMethod doesn't exists", e);
    }
  }

  public static WorkflowExecution buildWorkflowExecutionMock(WorkflowCreatedDTO expectedResult){
    WorkflowExecution wfExecMock = Mockito.mock(WorkflowExecution.class);

    Mockito.when(wfExecMock.getWorkflowId())
      .thenReturn(expectedResult.getWorkflowId());
    Mockito.when(wfExecMock.getRunId())
      .thenReturn(expectedResult.getRunId());

    return wfExecMock;
  }

  public static <A1> void configureWorkflowClientServiceMock(WorkflowClientService workflowClientServiceMock, WorkflowCreatedDTO expectedResult, A1 arg1){
    Mockito.when(workflowClientServiceMock.start(TemporalTestUtils.buildArgumentMatcherProc(arg1),
        Mockito.same(arg1)))
      .thenReturn(expectedResult);
  }
  public static <A1> Functions.Proc1<A1> buildArgumentMatcherProc(A1 arg1){
    return Mockito.argThat((Functions.Proc1<A1> f) -> {
      f.apply(arg1);
      return true;
    });
  }

  public static <A1, A2> void configureWorkflowClientServiceMock(WorkflowClientService workflowClientServiceMock, WorkflowCreatedDTO expectedResult, A1 arg1, A2 arg2){
    Mockito.when(workflowClientServiceMock.start(TemporalTestUtils.buildArgumentMatcherProc(arg1, arg2),
        Mockito.same(arg1), Mockito.same(arg2)))
      .thenReturn(expectedResult);
  }
  public static <A1, A2> Functions.Proc2<A1, A2> buildArgumentMatcherProc(A1 arg1, A2 arg2){
    return Mockito.argThat((Functions.Proc2<A1, A2> f) -> {
      f.apply(arg1, arg2);
      return true;
    });
  }

  public static <A1, A2, A3> void configureWorkflowClientServiceMock(WorkflowClientService workflowClientServiceMock, WorkflowCreatedDTO expectedResult, A1 arg1, A2 arg2, A3 arg3){
    Mockito.when(workflowClientServiceMock.start(TemporalTestUtils.buildArgumentMatcherProc(arg1, arg2, arg3),
        Mockito.same(arg1), Mockito.same(arg2), Mockito.same(arg3)))
      .thenReturn(expectedResult);
  }
  public static <A1, A2, A3> Functions.Proc3<A1, A2, A3> buildArgumentMatcherProc(A1 arg1, A2 arg2, A3 arg3){
    return Mockito.argThat((Functions.Proc3<A1, A2, A3> f) -> {
      f.apply(arg1, arg2, arg3);
      return true;
    });
  }

  public static <A1, A2, A3, A4> void configureWorkflowClientServiceMock(WorkflowClientService workflowClientServiceMock, WorkflowCreatedDTO expectedResult, A1 arg1, A2 arg2, A3 arg3, A4 arg4){
    Mockito.when(workflowClientServiceMock.start(TemporalTestUtils.buildArgumentMatcherProc(arg1, arg2, arg3, arg4),
        Mockito.same(arg1), Mockito.same(arg2), Mockito.same(arg3), Mockito.same(arg4)))
      .thenReturn(expectedResult);
  }
  public static <A1, A2, A3, A4> Functions.Proc4<A1, A2, A3, A4> buildArgumentMatcherProc(A1 arg1, A2 arg2, A3 arg3, A4 arg4){
    return Mockito.argThat((Functions.Proc4<A1, A2, A3, A4> f) -> {
      f.apply(arg1, arg2, arg3, arg4);
      return true;
    });
  }

  public static <A1, A2, A3, A4, A5> void configureWorkflowClientServiceMock(WorkflowClientService workflowClientServiceMock, WorkflowCreatedDTO expectedResult, A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5){
    Mockito.when(workflowClientServiceMock.start(TemporalTestUtils.buildArgumentMatcherProc(arg1, arg2, arg3, arg4, arg5),
        Mockito.same(arg1), Mockito.same(arg2), Mockito.same(arg3), Mockito.same(arg4), Mockito.same(arg5)))
      .thenReturn(expectedResult);
  }
  public static <A1, A2, A3, A4, A5> Functions.Proc5<A1, A2, A3, A4, A5> buildArgumentMatcherProc(A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5){
    return Mockito.argThat((Functions.Proc5<A1, A2, A3, A4, A5> f) -> {
      f.apply(arg1, arg2, arg3, arg4, arg5);
      return true;
    });
  }

  public static void configureWorkflowClientServiceMock(WorkflowClientService workflowClientServiceMock, WorkflowCreatedDTO expectedResult){
    Mockito.when(workflowClientServiceMock.start(TemporalTestUtils.buildArgumentMatcherProc()))
      .thenReturn(expectedResult);
  }
  public static Functions.Proc buildArgumentMatcherProc(){
    return Mockito.argThat((Functions.Proc f) -> {
      f.apply();
      return true;
    });
  }

  public static void verifyWorkflowTaskQueueConfiguration(String taskQueue, Class<?> wfImpl){
    Assertions.assertEquals(taskQueue, wfImpl.getAnnotation(WorkflowImpl.class).taskQueues()[0]);
  }

  public static void verifyActivityStubConfiguration(BaseWfConfig config, Map<Class<?>, Class<?>> localActivityInterface2Impl) throws InvocationTargetException, IllegalAccessException {
    config.setRetryInitialIntervalInMillis(1);
    config.setRetryBackoffCoefficient(1.0);
    try(MockedStatic<Workflow> workflowMockedStatic = Mockito.mockStatic(Workflow.class)) {
      for (Method method : config.getClass().getMethods()) {
        if (method.getName().startsWith("build")) {
          Class<?> activityInterface = method.getReturnType();
          Class<?> localActivityImplClass = localActivityInterface2Impl.get(activityInterface);
          String expectedTaskQueue = localActivityImplClass != null
            ? localActivityImplClass.getAnnotation(ActivityImpl.class).taskQueues()[0]
            : null;

          ArgumentCaptor<ActivityOptions> activityOptionsCaptor = ArgumentCaptor.captor();
          Object expectedStub = Mockito.mock(activityInterface);
          workflowMockedStatic.when(() -> Workflow.newActivityStub(Mockito.eq(activityInterface), activityOptionsCaptor.capture()))
            .thenReturn(expectedStub);

          Object stub = method.invoke(config);

          Assertions.assertSame(expectedStub, stub);
          Assertions.assertEquals(expectedTaskQueue, activityOptionsCaptor.getValue().getTaskQueue());
        }
      }
    }
  }
}
