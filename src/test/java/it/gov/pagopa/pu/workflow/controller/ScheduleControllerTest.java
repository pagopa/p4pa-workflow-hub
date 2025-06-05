package it.gov.pagopa.pu.workflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.pu.workflow.dto.generated.ScheduleInfoDTO;
import it.gov.pagopa.pu.workflow.enums.ScheduleEnum;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowScheduleService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ScheduleControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
class ScheduleControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private WorkflowScheduleService serviceMock;

  @Test
  void whenGetScheduleInfoThenOk() throws Exception {
    ScheduleEnum scheduleId = ScheduleEnum.PAYMENTS_REPORTING_PAGOPA_BROKERS_FETCH;
    ScheduleInfoDTO expectedResult = ScheduleInfoDTO.builder()
      .scheduleId(scheduleId)
      .build();

    Mockito.when(serviceMock.getScheduleInfo(scheduleId))
      .thenReturn(expectedResult);

    MvcResult result = mockMvc.perform(
        get("/workflowhub/schedules/{scheduleId}/info", scheduleId)
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().is2xxSuccessful())
      .andReturn();

    ScheduleInfoDTO resultResponse = objectMapper.readValue(result.getResponse().getContentAsString(), ScheduleInfoDTO.class);
    assertEquals(expectedResult, resultResponse);
  }
}
