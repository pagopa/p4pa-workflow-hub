package it.gov.pagopa.pu.workflow;

import it.gov.pagopa.payhub.activities.aspect.NotRetryableActivityExceptionHandlerAspect;
import it.gov.pagopa.payhub.activities.performancelogger.ApiRequestPerformanceLogger;
import it.gov.pagopa.payhub.activities.util.Utilities;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.webmvc.autoconfigure.error.ErrorMvcAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.util.TimeZone;

@SpringBootApplication(
  exclude = {ErrorMvcAutoConfiguration.class},
  scanBasePackages = {
    "it.gov.pagopa.pu.workflow",
    "it.gov.pagopa.payhub.activities.connector.auth"
  })
@Import({
  NotRetryableActivityExceptionHandlerAspect.class,
  ApiRequestPerformanceLogger.class
})
public class WorkflowApplication {

  public static void main(String[] args) {
    TimeZone.setDefault(Utilities.DEFAULT_TIMEZONE);
    SpringApplication.run(WorkflowApplication.class, args);
  }

}
