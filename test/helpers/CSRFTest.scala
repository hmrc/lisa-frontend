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

/***
  * Credit: http://stackoverflow.com/a/40259536
  */

package helpers

import play.api.Application
import play.api.test.FakeRequest
import play.filters.csrf.CSRF.Token
import play.filters.csrf.{CSRFConfigProvider, CSRFFilter}
import play.api.test.CSRFTokenHelper._

import scala.language.postfixOps

trait CSRFTest {
//  def addToken[T](fakeRequest: FakeRequest[T])(implicit app: Application): FakeRequest[T] = {
//    val csrfConfig = app.injector.instanceOf[CSRFConfigProvider].get
//    val csrfFilter = app.injector.instanceOf[CSRFFilter]
//    val token = csrfFilter.tokenProvider.generateToken
//    val blah = Token
//    blah.
//
//    fakeRequest.copyFakeRequest(tags = fakeRequest.tags ++ Map(
//      Token.NameRequestTag  -> csrfConfig.tokenName,
//      Token.RequestTag      -> token
//    )).withHeaders((csrfConfig.headerName, token))
//  }
}