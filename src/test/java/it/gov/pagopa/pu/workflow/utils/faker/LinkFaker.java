package it.gov.pagopa.pu.workflow.utils.faker;

import it.gov.pagopa.pu.organization.dto.generated.Link;
import it.gov.pagopa.pu.workflow.dto.generated.LinkRequestDTO;

public class LinkFaker {

  public static Link buildLink(){
    return Link.builder()
      .href("href")
      .hreflang("hreflang")
      .title("title")
      .type("type")
      .deprecation("deprecation")
      .profile("profile")
      .name("name")
      .templated(true)
      .build();
  }

  public static LinkRequestDTO buildLinkRequestDTO(){
    return LinkRequestDTO.builder()
      .href("href")
      .hreflang("hreflang")
      .title("title")
      .type("type")
      .deprecation("deprecation")
      .profile("profile")
      .name("name")
      .templated(true)
      .build();
  }
}
