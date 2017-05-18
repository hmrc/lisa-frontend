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

package controllers

import java.io.File

import helpers.CSRFTest
import models.{BusinessStructure, OrganisationDetails}
import play.api.{Configuration, Environment, Mode}
import play.api.http.Status
import play.api.test.{FakeHeaders, FakeRequest}
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.PlayAuthConnector
import uk.gov.hmrc.http.cache.client.ShortLivedCache
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import play.api.data.validation.Constraints.pattern
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsJson}
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.Json

import scala.concurrent.Future


class BusinessStructureControllerSpec extends PlaySpec
  with GuiceOneAppPerSuite
  with MockitoSugar
  with CSRFTest
  with BeforeAndAfter {

  "GET /" should {
    "return 200" in {
      when(mockCache.fetchAndGetEntry[BusinessStructure](any(), any())(any(), any())).
        thenReturn(Future.successful(None))

      val result = SUT.get(fakeRequest)
      status(result) mustBe Status.OK
    }

    "return HTML" in {
      when(mockCache.fetchAndGetEntry[BusinessStructure](any(), any())(any(), any())).
        thenReturn(Future.successful(None))

      val result = SUT.get(fakeRequest)
      contentAsString(result) must include("Select your business structure")
      contentType(result) mustBe Some("text/html")
      charset(result) mustBe Some("utf-8")
    }
  }

  "POST /" should {
    "return 200" in {
      val uri = controllers.routes.BusinessStructureController.post().url
      val req = createFakePostRequest[AnyContentAsJson](uri, AnyContentAsJson(json = Json.obj()))
      val result = SUT.post()(req)

      status(result) mustBe Status.BAD_REQUEST
    }

    "return HTML" in {
      val uri = controllers.routes.BusinessStructureController.post().url
      val req = createFakePostRequest[AnyContentAsJson](uri, AnyContentAsJson(json = Json.obj()))
      val result = SUT.post()(req)

      contentAsString(result) must include("Select your business structure")
      contentType(result) mustBe Some("text/html")
      charset(result) mustBe Some("utf-8")
    }
  }

  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = addToken(FakeRequest("GET", "/"))

  def createFakePostRequest[T](uri: String, body:T):FakeRequest[T] = {
    addToken(FakeRequest("POST", uri, FakeHeaders(), body))
  }

  val mockAuthConnector: PlayAuthConnector = mock[PlayAuthConnector]
  val mockConfig: Configuration = mock[Configuration]
  val mockEnvironment: Environment = Environment(mock[File], mock[ClassLoader], Mode.Test)
  val mockCache: ShortLivedCache = mock[ShortLivedCache]

  object SUT extends BusinessStructureController {
    override val authConnector: PlayAuthConnector = mockAuthConnector
    override val config: Configuration = mockConfig
    override val env: Environment = mockEnvironment
    override val cache: ShortLivedCache = mockCache
  }

  when(mockAuthConnector.authorise[Option[String]](any(), any())(any())).
    thenReturn(Future.successful(Some("1234")))

  when(mockConfig.getString(matches("^appName$"), any())).
    thenReturn(Some("lisa-frontend"))

  when(mockConfig.getString(matches("^.*company-auth-frontend.host$"), any())).
    thenReturn(Some(""))

  when(mockConfig.getString(matches("^sosOrigin$"), any())).
    thenReturn(None)

}