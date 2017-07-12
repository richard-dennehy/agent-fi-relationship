/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.agentfirelationship.config

import java.net.URL
import javax.inject.Provider

import com.google.inject.AbstractModule
import com.google.inject.name.Names
import play.api.Logger
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.config._
import uk.gov.hmrc.play.http.{HttpGet, HttpPost}

class GuiceModule extends AbstractModule with ServicesConfig {
  override def configure(): Unit = {
    bind(classOf[reactivemongo.api.DB]).toProvider(classOf[MongoDbProvider])
    bind(classOf[AuditConnector]).toInstance(MicroserviceGlobal.auditConnector)
    bind(classOf[HttpPost]).toInstance(WSHttp)
    bindBaseUrl("government-gateway-proxy")
    ()
  }
  private def bindBaseUrl(serviceName: String) =
    bind(classOf[URL]).annotatedWith(Names.named(s"$serviceName-baseUrl")).toProvider(new BaseUrlProvider(serviceName))

  private class BaseUrlProvider(serviceName: String) extends Provider[URL] {
    override lazy val get = new URL(baseUrl(serviceName))
  }
}