/*
 * Copyright 2020 HM Revenue & Customs
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

import base.SpecBase
import models.Reapplication
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import play.api.http.Status
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers.{redirectLocation, status, _}
import play.api.test.Injecting

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReapplyControllerSpec extends SpecBase with Injecting {

  "The reapplication controller" should {
    "redirect to the BusinessStructure controller endpoint" in {
      when(shortLivedCache.fetchAndGetEntry[Boolean](ArgumentMatchers.any(), ArgumentMatchers.eq(Reapplication.cacheKey))(
        ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(true)))

      val result = SUT.get(fakeRequest)

      status(result) mustBe Status.SEE_OTHER

      redirectLocation(result) mustBe Some(controllers.routes.BusinessStructureController.get().url)
    }
  }
  implicit val mcc: MessagesControllerComponents = inject[MessagesControllerComponents]
  val SUT = new ReapplyController()

}
