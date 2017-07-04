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

package models

import play.api.data.Forms._
import play.api.data._
import play.api.data.validation.Constraints.pattern
import play.api.libs.json.{Json, OFormat}

case class TradingDetails(fsrRefNumber: String,
                          isaProviderRefNumber: String)

object TradingDetails {

  implicit val formats: OFormat[TradingDetails] = Json.format[TradingDetails]

  val cacheKey = "tradingDetails"

  val form = Form(
    mapping(
      "fsrRefNumber" -> text.verifying(pattern("""^[0-9]{6}$""".r, error="error.fsrRefNumber")),
      "isaProviderRefNumber" -> text.verifying(pattern("""^Z([0-9]{4}|[0-9]{6})$""".r, error="error.isaProviderRefNumber"))
    )(TradingDetails.apply)(TradingDetails.unapply)
  )
}