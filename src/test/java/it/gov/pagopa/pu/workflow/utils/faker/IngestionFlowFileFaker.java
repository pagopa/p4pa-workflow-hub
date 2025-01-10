package it.gov.pagopa.pu.workflow.utils.faker;

import it.gov.pagopa.payhub.activities.dto.IngestionFlowFileDTO;
import it.gov.pagopa.payhub.activities.enums.IngestionFlowFileType;
import it.gov.pagopa.pu.workflow.dto.generated.IngestionFlowFileRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.IngestionFlowFileTypeRequest;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static it.gov.pagopa.pu.workflow.utils.TestUtils.DATE;
import static it.gov.pagopa.pu.workflow.utils.TestUtils.OFFSET_DATE_TIME;
import static it.gov.pagopa.pu.workflow.utils.faker.OrganizationFaker.buildOrganization;
import static it.gov.pagopa.pu.workflow.utils.faker.OrganizationFaker.buildOrganizationRequestDTO;

public class IngestionFlowFileFaker {

  public static IngestionFlowFileDTO buildIngestionFlowFileDTO() {
    return IngestionFlowFileDTO.builder()
      .ingestionFlowFileId(1L)
      .version(1)
      .org(buildOrganization())
      .status("status")
      .numTotalRows(3L)
      .numCorrectlyImportedRows(2L)
      .creationDate(DATE.toInstant())
      .lastUpdateDate(DATE.toInstant())
      .flagActive(true)
      .operatorExternalUserId("operatorExternalId")
      .flagSpontaneous(Boolean.TRUE)
      .filePathName("filePathName")
      .fileName("fileName")
      .fileSize(1L)
      .pdfGenerated(2L)
      .codRequestToken("codRequestToken")
      .codError("codError")
      .pspIdentifier("PspId")
      .flowDateTime(LocalDateTime.ofInstant(DATE.toInstant(), ZoneId.systemDefault()))
      .fileSourceCode("FileSourceCode")
      .discardFileName("DiscardFileName")
      .flowFileType(IngestionFlowFileType.PAYMENTS_REPORTING)
      .build();
  }

  public static IngestionFlowFileRequestDTO buildIngestionFlowFileRequestDTO() {
    return IngestionFlowFileRequestDTO.builder()
      .ingestionFlowFileId(1L)
      .version(1)
      .org(buildOrganizationRequestDTO())
      .status("status")
      .numTotalRows(3L)
      .numCorrectlyImportedRows(2L)
      .creationDate(OFFSET_DATE_TIME)
      .lastUpdateDate(OFFSET_DATE_TIME)
      .flagActive(true)
      .operatorExternalUserId("operatorExternalId")
      .flagSpontaneous(Boolean.TRUE)
      .filePathName("filePathName")
      .fileName("fileName")
      .fileSize(1L)
      .pdfGenerated(2L)
      .codRequestToken("codRequestToken")
      .codError("codError")
      .pspIdentifier("PspId")
      .flowDateTime(OFFSET_DATE_TIME)
      .fileSourceCode("FileSourceCode")
      .discardFileName("DiscardFileName")
      .flowFileType(IngestionFlowFileTypeRequest.PAYMENTS_REPORTING)
      .build();
  }
}
