/*
 * Copyright 2023 HM Revenue & Customs
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

package helpers

import models.{BusinessStructure, OrganisationDetails, TradingDetails, YourDetails}
import play.api.libs.json.{JsString, JsValue, Json}

object FullCacheTestData {

  val organisationForm = new OrganisationDetails("Test Company Name", "1234567890")
  val organisationFormKeyAndJson: (String, JsValue) = OrganisationDetails.cacheKey -> Json.toJson(organisationForm)
  val tradingForm = new TradingDetails( fsrRefNumber = "123", isaProviderRefNumber = "123")
  val tradingFormKeyAndJson: (String, JsValue) = TradingDetails.cacheKey -> Json.toJson(tradingForm)
  val businessStructureForm = new BusinessStructure("LLP")
  val businessStructureFormKeyAndJson: (String, JsValue) = BusinessStructure.cacheKey -> Json.toJson(businessStructureForm)
  val safeKeyAndJson: (String, JsString) = "safeId" -> JsString("")
  val yourForm = new YourDetails(firstName = "Test", lastName = "User", role = "Role", phone = "0191 123 4567", email = "test@test.com")
  val yourFormKeyAndJson: (String, JsValue) = YourDetails.cacheKey -> Json.toJson(yourForm)

  val allDataComponents: Seq[(String, JsValue)] = Seq(
    organisationFormKeyAndJson,
    tradingFormKeyAndJson,
    businessStructureFormKeyAndJson,
    safeKeyAndJson,
    yourFormKeyAndJson
  )

  val noBusinessStructureComponents: Seq[(String, JsValue)] = Seq(
    organisationFormKeyAndJson,
    tradingFormKeyAndJson,
    safeKeyAndJson,
    yourFormKeyAndJson
  )

  val noOrgDetailsComponents: Seq[(String, JsValue)] = Seq(
    tradingFormKeyAndJson,
    businessStructureFormKeyAndJson,
    safeKeyAndJson,
    yourFormKeyAndJson
  )

  val noSafeIdComponents: Seq[(String, JsValue)] = Seq(
    organisationFormKeyAndJson,
    tradingFormKeyAndJson,
    businessStructureFormKeyAndJson,
    yourFormKeyAndJson
  )

  val noTradingDetailsComponents: Seq[(String, JsValue)] = Seq(
    organisationFormKeyAndJson,
    businessStructureFormKeyAndJson,
    safeKeyAndJson,
    yourFormKeyAndJson
  )

  val noFormDetailComponents: Seq[(String, JsValue)] = Seq(
    organisationFormKeyAndJson,
    tradingFormKeyAndJson,
    businessStructureFormKeyAndJson,
    safeKeyAndJson
  )

}
