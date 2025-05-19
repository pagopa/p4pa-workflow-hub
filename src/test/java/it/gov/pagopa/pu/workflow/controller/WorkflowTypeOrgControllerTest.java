package it.gov.pagopa.pu.workflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.FineWfExecutionConfig;
import it.gov.pagopa.pu.workflow.model.WorkflowTypeOrg;
import it.gov.pagopa.pu.workflow.service.WorkflowTypeOrgSaveService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkflowTypeOrgControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
class WorkflowTypeOrgControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private WorkflowTypeOrgSaveService serviceMock;

  @Test
  void givenSaveWorkflowTypeOrgThenInvokeService() throws Exception {
    WorkflowTypeOrg entity = new WorkflowTypeOrg();
    entity.setDebtPositionTypeOrgId(1L);
    entity.setWorkflowTypeId(0L);
    entity.setDefaultExecutionConfig(new FineWfExecutionConfig());

    WorkflowTypeOrg saved = new WorkflowTypeOrg();
    saved.setDebtPositionTypeOrgId(1L);
    FineWfExecutionConfig mergedConfig = new FineWfExecutionConfig();
    mergedConfig.setIoMessages(new FineWfExecutionConfig.IONotificationFineWfMessages());
    saved.setDefaultExecutionConfig(mergedConfig);

    Mockito.when(serviceMock.save(entity))
      .thenReturn(saved);

    MvcResult result = mockMvc.perform(
        post("/workflowhub/workflow-type-orgs")
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON_VALUE)
          .content(objectMapper.writeValueAsString(entity)))
      .andExpect(status().is2xxSuccessful())
      .andReturn();

    WorkflowTypeOrg resultResponse = objectMapper.readValue(result.getResponse().getContentAsString(), WorkflowTypeOrg.class);
    assertEquals(saved, resultResponse);
  }
}
