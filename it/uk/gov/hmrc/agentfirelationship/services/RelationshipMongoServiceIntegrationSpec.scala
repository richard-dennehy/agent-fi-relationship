package uk.gov.hmrc.agentfirelationship.services

import java.time.LocalDateTime
import javax.inject.Singleton

import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.agentfirelationship.models.Relationship
import uk.gov.hmrc.agentfirelationship.models.RelationshipStatus.{Active, Terminated}
import uk.gov.hmrc.agentfirelationship.{agentId, clientId, service}
import uk.gov.hmrc.agentfirelationship.support._
import uk.gov.hmrc.agentmtdidentifiers.model.Arn
import uk.gov.hmrc.play.test.UnitSpec
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class RelationshipMongoServiceIntegrationSpec extends UnitSpec
  with UpstreamServicesStubs with GuiceOneServerPerSuite with MongoApp {
  me: DualSuite =>

  def repo: RelationshipMongoService = app.injector.instanceOf[RelationshipMongoService]

  override implicit lazy val app: Application = appBuilder.build()
  override def arn = agentId
  override def nino = clientId

  val testResponseDate: String = LocalDateTime.now.toString
  val validTestRelationship: Relationship = Relationship(Arn(arn), service, nino, Some(Active), LocalDateTime.parse(testResponseDate), None)
  val invalidTestRelationship: Relationship = validTestRelationship.copy(relationshipStatus = Some(Terminated))
  val validTestRelationshipCesa: Relationship = Relationship(Arn(arn), service, nino, Some(Active), LocalDateTime.parse(testResponseDate), None, fromCesa = Some(true))

  protected def appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.auth.port" -> wireMockPort,
        "auditing.consumer.baseUri.port" -> wireMockPort,
        "mongodb.uri" -> s"mongodb://127.0.0.1:27017/test-${this.getClass.getSimpleName}",
        "features.copy-cesa-relationships" -> false,
        "features.check-cesa-relationships" -> false
      )

  "Update AFI relationships which do not have relationshipStatus to have relationshipStatus: ACTIVE" should {
    "all relationships without status, now have status ACTIVE" in {
      val create = for {
        _ <- repo.createRelationship(validTestRelationship)
        _ <- repo.createRelationship(validTestRelationship.copy(relationshipStatus = None))
        _ <- repo.createRelationship(validTestRelationship.copy(relationshipStatus = None))
        _ <- repo.createRelationship(validTestRelationship.copy(relationshipStatus = Some(Terminated)))
      } yield ()
      await(create)
      await(repo.addActiveRelationshipStatus())

      await(repo.findRelationships(agentId, "afi", clientId, Active)).length shouldBe 3
      await(repo.findRelationships(agentId, "afi", clientId, Terminated)).length shouldBe 1
    }

    "relationships with status are unaffected" in {
      val create = for {
        _ <- repo.createRelationship(validTestRelationship)
        _ <- repo.createRelationship(validTestRelationship.copy(relationshipStatus = Some(Terminated)))
        _ <- repo.createRelationship(validTestRelationship.copy(relationshipStatus = Some(Terminated)))
        _ <- repo.createRelationship(validTestRelationship.copy(relationshipStatus = Some(Terminated)))
      } yield ()
      await(create)
      await(repo.addActiveRelationshipStatus())

      await(repo.findRelationships(agentId, "afi", clientId, Active)).length shouldBe 1
      await(repo.findRelationships(agentId, "afi", clientId, Terminated)).length shouldBe 3
    }
  }

  "RelationshipMongoService" should {
    "return active relationships for findRelationships" in {
      await(repo.createRelationship(validTestRelationship))
      await(repo.createRelationship(validTestRelationshipCesa))
      await(repo.createRelationship(invalidTestRelationship))

      await(repo.findAll()).size shouldBe 3

      val result = await(repo.findRelationships(arn,service, nino, Active))

      result should not be empty
      result.head shouldBe validTestRelationship
    }

    "return empty results if no active relationships found" in {
      await(repo.createRelationship(invalidTestRelationship))
      val result = await(repo.findRelationships(arn,service, nino, Active))

      result shouldBe empty
    }

    "return TERMINATED relationships" in {
      await(repo.createRelationship(validTestRelationship))
      await(repo.createRelationship(validTestRelationshipCesa))
      await(repo.createRelationship(invalidTestRelationship))

      await(repo.findAll()).size shouldBe 3

      val result = await(repo.findRelationships(arn,service, nino, Terminated))

      result should not be empty
      result.head shouldBe invalidTestRelationship
    }

    "return empty results if no TERMINATED relationships found" in {
      await(repo.createRelationship(validTestRelationship))
      val result = await(repo.findRelationships(arn,service, nino, Terminated))

      result shouldBe empty
    }
  }
}