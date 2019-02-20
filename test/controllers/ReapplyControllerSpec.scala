/*
 * Copyright 2019 HM Revenue & Customs
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
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import play.api.http.Status
import play.api.test.Helpers.{redirectLocation, status, _}

import scala.concurrent.Future

class ReapplyControllerSpec extends SpecBase {

  "The reapplication controller" should {
    "redirect to the BusinessStructure controller endpoint" in {
      when(shortLivedCache.fetchAndGetEntry[Boolean](any(), org.mockito.Matchers.eq(Reapplication.cacheKey))(any(), any(), any())).
        thenReturn(Future.successful(Some(true)))

      val result = SUT.get(fakeRequest)

      status(result) mustBe Status.SEE_OTHER

      redirectLocation(result) mustBe Some(controllers.routes.BusinessStructureController.get().url)
    }
  }

  val SUT = new ReapplyController()

}
