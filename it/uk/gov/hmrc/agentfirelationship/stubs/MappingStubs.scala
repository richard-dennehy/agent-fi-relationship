package uk.gov.hmrc.agentfirelationship.stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import uk.gov.hmrc.agentmtdidentifiers.model.Arn
import uk.gov.hmrc.domain.SaAgentReference

trait MappingStubs {

  def givenArnIsKnownFor(arn: Arn, saAgentReference: SaAgentReference) = {
    stubFor(
      get(urlEqualTo(s"/agent-mapping/mappings/sa/${arn.value}"))
        .willReturn(aResponse()
          .withStatus(200)
          .withBody(s"""{"mappings":[{"arn":"${arn.value}","saAgentReference":"${saAgentReference.value}"}]}""")))
  }

  def givenArnIsKnownFor(arn: Arn, refs: Seq[SaAgentReference]) = {
    stubFor(
      get(urlEqualTo(s"/agent-mapping/mappings/sa/${arn.value}"))
        .willReturn(aResponse()
          .withStatus(200)
          .withBody(s"""{"mappings":[${refs.map(ref => s"""{"arn":"${arn.value}","saAgentReference":"${ref.value}"}""").mkString(",")}]}""")))
  }

  def givenAgentRefsNotFoundFor(arn: Arn) = {
    stubFor(
      get(urlEqualTo(s"/agent-mapping/mappings/sa/${arn.value}"))
        .willReturn(aResponse()
          .withStatus(404)))
  }

  def givenServiceReturnsServerError() = {
    stubFor(
      get(urlMatching(s"/agent-mapping/.*"))
        .willReturn(aResponse().withStatus(500)))
  }

  def givenServiceReturnsServiceUnavailable() = {
    stubFor(
      get(urlMatching(s"/agent-mapping/.*"))
        .willReturn(aResponse().withStatus(503)))
  }

}
