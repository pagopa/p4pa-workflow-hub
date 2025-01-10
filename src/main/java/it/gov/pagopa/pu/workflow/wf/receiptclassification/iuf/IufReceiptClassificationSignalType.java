package it.gov.pagopa.pu.workflow.wf.receiptclassification.iuf;

import lombok.Getter;

@Getter
public enum IufReceiptClassificationSignalType {

  RENDICONTAZIONE("rendicontazione"),
  TESORERIA("tesoreria");

  private final String value;

  IufReceiptClassificationSignalType(String value) {
    this.value = value;
  }
}
