package uk.gov.hmrc.agentfirelationship

import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.agentfirelationship.support.{IntegrationSpec, RelationshipActions, UpstreamServicesStubs}

import scala.concurrent.Await
import scala.concurrent.duration._

class FailuresIntegrationSpec extends IntegrationSpec with GuiceOneServerPerSuite with RelationshipActions with UpstreamServicesStubs {

  override implicit lazy val app: Application = appBuilder.build()

  protected def appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(
        "mongodb.uri" -> "mongodb://nowhere:27017/none"
      )

  feature("Do not handle infrastructure failures, propagates errors downstream") {

    scenario("Mongodb not available when creating relationship") {
      Given("a create-relationship request with basic string values for Agent ID, client ID and service")
      givenCreatedAuditEventStub(auditDetails)
      val response = Await.result(createRelationship(agentId, clientId, service, validDateFormatInString), 10 seconds)
      When("I call the create-relationship endpoint")
      response.status shouldBe INTERNAL_SERVER_ERROR
      Then("I will receive a 500 INTERNAL SERVER ERROR response")
    }

    scenario("Mongodb not available when deleting relationship") {
      Given("there exists a relationship between an agent and client for a given service")
      givenEndedAuditEventStub(auditDetails)

      When("I call the delete-relationship endpoint")
      val response = Await.result(deleteRelationship(agentId, clientId, service), 10 seconds)

      Then("I should get a 500 INTERNAL SERVER ERROR response")
      response.status shouldBe INTERNAL_SERVER_ERROR
    }

    scenario("Mongodb not available when viewing relationship") {
      Given("there exists a relationship between an agent and client for a given service")
      givenCreatedAuditEventStub(auditDetails)
      Await.result(createRelationship(agentId, clientId, service, validDateFormatInString), 10 seconds)

      When("I call the View Relationship endpoint")
      val response = Await.result(getRelationship(agentId, clientId, service), 10 seconds)

      Then("I will receive a 500 INTERNAL SERVER ERROR response")
      response.status shouldBe INTERNAL_SERVER_ERROR
    }
  }
}
