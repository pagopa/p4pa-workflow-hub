package it.gov.pagopa.pu.workflow.local;

import io.temporal.spring.boot.ActivityImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.HashMap;

import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
@ActivityImpl
public class SpringValuesLocalActivityImpl implements SpringValuesLocalActivity {

  @Value("${workflow.queue:PaymentsReportingIngestionWF}")
  private String workflowQueue;

  @Value("${workflow.startToCloseTimeoutInSeconds:60}")
  private int startToCloseTimeoutInSeconds;

  @Value("${workflow.retryInitialIntervalInMillis:1000}")
  private int retryInitialIntervalInMillis;

  @Value("${workflow.retryBackoffCoefficient:1.1}")
  private double retryBackoffCoefficient;

  @Value("${workflow.retryMaximumAttempts:10}")
  private int retryMaximumAttempts;


  @Override
  public HashMap<String, String> getProperties() {

    HashMap<String, String> properties = new HashMap<>();
    properties.put("startToCloseTimeoutInSeconds", String.valueOf(startToCloseTimeoutInSeconds));
    properties.put("retryInitialIntervalInMillis", String.valueOf(retryInitialIntervalInMillis));
    properties.put("retryBackoffCoefficient", String.valueOf(retryBackoffCoefficient));
    properties.put("retryMaximumAttempts", String.valueOf(retryMaximumAttempts));
    properties.put("workflowQueue", workflowQueue);

    return properties;

  }

}
