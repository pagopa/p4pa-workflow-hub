package it.gov.pagopa.pu.workflow.service.temporal;

import io.temporal.client.WorkflowStub;
import io.temporal.workflow.Functions;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;

public interface WorkflowClientService {
  WorkflowCreatedDTO start(Functions.Proc workflow);
  <A1> WorkflowCreatedDTO start(Functions.Proc1<A1> workflow, A1 arg1);
  <A1, A2> WorkflowCreatedDTO start(Functions.Proc2<A1, A2> workflow, A1 arg1, A2 arg2);
  <A1, A2, A3> WorkflowCreatedDTO start(Functions.Proc3<A1, A2, A3> workflow, A1 arg1, A2 arg2, A3 arg3);
  <A1, A2, A3, A4> WorkflowCreatedDTO start(Functions.Proc4<A1, A2, A3, A4> workflow, A1 arg1, A2 arg2, A3 arg3, A4 arg4);
  <A1, A2, A3, A4, A5> WorkflowCreatedDTO start(Functions.Proc5<A1, A2, A3, A4, A5> workflow, A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5);
  <A1, A2, A3, A4, A5, A6> WorkflowCreatedDTO start(Functions.Proc6<A1, A2, A3, A4, A5, A6> workflow, A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5, A6 arg6);

  WorkflowCreatedDTO signalWithStart(WorkflowStub workflowStub, String signalName, Object[] signalArgs, Object[] startArgs);
}
