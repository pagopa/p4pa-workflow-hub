package it.gov.pagopa.pu.workflow.controller;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.OrganizationPaymentsReportingPagoPaFetchWFClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PagoPaFetchControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
class PagoPaFetchControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private OrganizationPaymentsReportingPagoPaFetchWFClient serviceMock;

  @Test
  void whenGetWorkflowStatusThenOk() throws Exception {
    Long organizationId = 10L;
    String workflowId = "workflow-1";
    String runId = "runId";

    Mockito.when(serviceMock.retrieve(organizationId))
      .thenReturn(new WorkflowCreatedDTO(workflowId, runId));

    mockMvc.perform(
        get("/workflowhub/workflow/pagopa-fetch/payments-reporting/{organizationId}", organizationId)
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().is2xxSuccessful())
      .andExpect(content().json("{\"workflowId\":\"workflow-1\",\"runId\":\"runId\"}"));
  }
}
