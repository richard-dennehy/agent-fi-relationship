package uk.gov.hmrc

import java.time.LocalDateTime

package object agentfirelationship {
  val fakeCredId = "fakeCredId"
  val agentId = "TARN0000001"
  val clientId = "AE123456C"
  val service = "afi"
  val auditDetails = Map("authProviderId" -> fakeCredId, "arn" -> agentId, "regime" -> "afi", "regimeId" -> clientId)
  val testResponseDate = LocalDateTime.now.toString
}
