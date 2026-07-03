package it.gov.pagopa.pu.workflow.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ScheduleEnum {
  PAYMENTS_REPORTING_PAGOPA_BROKERS_FETCH("PaymentsReportingPagoPaBrokersFetchSchedule"),
  SYNCHRONIZE_TAXONOMY_PAGOPA_FETCH("SynchronizeTaxonomyPagoPaFetchSchedule"),
  ALIGN_SEND_CAMPAIGN_COUNTERS("AlignSendCampaignCountersSchedule");

  private final String value;
}

