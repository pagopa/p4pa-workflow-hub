package it.gov.pagopa.pu.workflow.local;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

import java.util.HashMap;

@ActivityInterface
public interface SpringValuesLocalActivity {
  @ActivityMethod
  HashMap<String, String> getProperties();
}
