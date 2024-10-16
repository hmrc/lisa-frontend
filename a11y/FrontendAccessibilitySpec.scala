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

import org.scalacheck.Arbitrary
import play.api.data.Form
import play.twirl.api.Html
import uk.gov.hmrc.scalatestaccessibilitylinter.views.AutomaticAccessibilitySpec
import views.html._
import config.AppConfig
import play.api.mvc.{Request, RequestHeader}
import play.api.test.FakeRequest
import play.api.test.CSRFTokenHelper._

class FrontendAccessibilitySpec extends AutomaticAccessibilitySpec {

  implicit val arbitraryRequest: Arbitrary[RequestHeader] = fixed(FakeRequest().withCSRFToken)
  implicit val arbitraryConfig: Arbitrary[AppConfig] = fixed(app.injector.instanceOf[AppConfig])
  implicit val arbitraryBusinessStructure: Arbitrary[Form[models.BusinessStructure]] = fixed(models.BusinessStructure.form)
  implicit val arbitraryOrganisationDetails: Arbitrary[Form[models.OrganisationDetails]] = fixed(models.OrganisationDetails.form)
  implicit val arbitraryTradingDetails: Arbitrary[Form[models.TradingDetails]] = fixed(models.TradingDetails.form)
  implicit val arbitraryYourDetails: Arbitrary[Form[models.YourDetails]] = fixed(models.YourDetails.form)
  override def viewPackageName: String = "views.html"

  override def layoutClasses: Seq[Class[govuk_wrapper]] = Seq(classOf[govuk_wrapper])

  override def renderViewByClass: PartialFunction[Any, Html] = {
    case access_denied_assistant: error.access_denied_assistant                     => render(access_denied_assistant)
    case access_denied_individual_or_agent: error.access_denied_individual_or_agent => render(access_denied_individual_or_agent)
    case error_template: error_template                                             => render(error_template)
    case page_not_found_template: page_not_found_template                           => render(page_not_found_template)
    case application_pending: registration.application_pending                      => render(application_pending)
    case application_rejected: registration.application_rejected                    => render(application_rejected)
    case application_submitted: registration.application_submitted                  => render(application_submitted)
    case application_successful: registration.application_successful                => render(application_successful)
    case business_structure: registration.business_structure                        => render(business_structure)
    case matching_failed: registration.matching_failed                              => render(matching_failed)
    case organisation_details: registration.organisation_details                    => render(organisation_details)
    case summary: registration.summary                                              => render(summary)
    case trading_details: registration.trading_details                              => render(trading_details)
    case your_details: registration.your_details                                    => render(your_details)
    case timeout_sign_out: timeout_sign_out                                         => render(timeout_sign_out)
  }

  runAccessibilityTests()
}