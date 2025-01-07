package it.gov.pagopa.pu.workflow.exception;

import it.gov.pagopa.pu.workflow.exception.custom.WorkflowInternalErrorException;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.mockito.Mockito.doThrow;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@WebMvcTest(value = {WorkflowExceptionHandlerTest.TestController.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {
  WorkflowExceptionHandlerTest.TestController.class,
  WorkflowExceptionHandler.class})
class WorkflowExceptionHandlerTest {

    public static final String DATA = "data";
    @Autowired
    private MockMvc mockMvc;

    @MockitoSpyBean
    private TestController testControllerSpy;

    @RestController
    @Slf4j
    static class TestController {

        @GetMapping("/test")
        String testEndpoint(@RequestParam(DATA) String data) {
            return "OK";
        }
    }

    @Test
    void handleNotFoundWorkflowError() throws Exception {
        doThrow(new WorkflowNotFoundException("Error")).when(testControllerSpy).testEndpoint(DATA);

        mockMvc.perform(MockMvcRequestBuilders.get("/test")
                        .param(DATA, DATA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("WORKFLOW_NOT_FOUND"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"));

    }

    @Test
    void handleInternalError() throws Exception {
        doThrow(new WorkflowInternalErrorException("Error")).when(testControllerSpy).testEndpoint(DATA);

        mockMvc.perform(MockMvcRequestBuilders.get("/test")
                        .param(DATA, DATA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("WORKFLOW_GENERIC_ERROR"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"));

    }
}
