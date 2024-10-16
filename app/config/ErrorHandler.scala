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
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler

import scala.concurrent.{ExecutionContext, Future}


class ErrorHandler @Inject()(val messagesApi: MessagesApi,
                             val configuration: Configuration,
                             implicit val appConfig: AppConfig,
                             errorView: views.html.error_template,
                             notFoundView: views.html.page_not_found_template
                            ) (implicit val ec: ExecutionContext) extends FrontendErrorHandler {
  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)
                                    (implicit request: RequestHeader): Future[Html] = Future.successful {
    errorView()
  }

  override def notFoundTemplate(implicit request: RequestHeader): Future[Html] = Future.successful {
    notFoundView()
  }

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] =  {
    statusCode match {
      //      case play.mvc.Http.Status.FORBIDDEN => Future.successful {
      //        Forbidden(internalServerErrorTemplate(Request(request, "")))
      //      }

      case play.mvc.Http.Status.FORBIDDEN => internalServerErrorTemplate(Request(request, "")).map(html=> Forbidden(html))

      case _                              => super.onClientError(request, statusCode, message)
    }
  }

//  override def onClientError(implicit request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
//    statusCode match {
//      case play.mvc.Http.Status.FORBIDDEN => Future.successful {
//        Forbidden(internalServerErrorTemplate(Request(request, "")))
//      }
//      case _                              => super.onClientError(request, statusCode, message)
//    }
//  }
}
