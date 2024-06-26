/*
 * Copyright 2024 HM Revenue & Customs
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

package config

import com.google.inject.Inject
import play.api.i18n.MessagesApi
import play.api.mvc.Results._
import play.api.mvc._
import play.api.Configuration
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler

import scala.concurrent.Future


class ErrorHandler @Inject()(val messagesApi: MessagesApi,
                             val configuration: Configuration,
                             implicit val appConfig: AppConfig,
                             errorView: views.html.error_template,
                             notFoundView: views.html.page_not_found_template
                            ) extends FrontendErrorHandler {
  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)
                                    (implicit request: Request[_]): HtmlFormat.Appendable = {
    errorView()
  }

  override def notFoundTemplate(implicit request: Request[_]): Html = {
    notFoundView()
  }

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    statusCode match {
      case play.mvc.Http.Status.FORBIDDEN => Future.successful(Forbidden(internalServerErrorTemplate(Request(request, ""))))
      case _                              => super.onClientError(request, statusCode, message)
    }
  }
}
