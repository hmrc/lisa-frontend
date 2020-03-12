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

package models

import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.{Json, OFormat}

case class TradingDetails(fsrRefNumber: String,
                          isaProviderRefNumber: String)

object TradingDetails {

  val cacheKey = "tradingDetails"

  implicit val formats: OFormat[TradingDetails] = Json.format[TradingDetails]

  val form = Form(
    mapping(
      "fsrRefNumber" -> optional(text)
        .verifying("error.fsrRefNumberRequired", fsr => fsrExists(fsr))
        .verifying("error.fsrRefNumberPattern", fsr => !fsrExists(fsr) || fsrIsNumeric(fsr))
        .verifying("error.fsrRefNumberLength", fsr => !fsrExists(fsr) || !fsrIsNumeric(fsr) || fsrIsSixCharacters(fsr)),
      "isaProviderRefNumber" -> optional(text)
        .verifying("error.isaProviderRefNumberRequired", zref => zrefExists(zref))
        .verifying("error.isaProviderRefNumberPattern", zref => !zrefExists(zref) || zrefIsCorrectPattern(zref))
    )(
      (fsr, zref) => TradingDetails(
        fsr.getOrElse(""),
        zref.getOrElse("")
      )
    )(
      td => Some(Some(td.fsrRefNumber), Some(td.isaProviderRefNumber))
    )
  )

  private def fsrExists(fsr: Option[String]) = {
    fsr.isDefined
  }

  private def fsrIsNumeric(fsr: Option[String]) = {
    fsr.getOrElse("").matches("^[0-9]+$")
  }

  private def fsrIsSixCharacters(fsr: Option[String]) = {
    fsr.getOrElse("").matches("^[0-9]{6}$")
  }

  private def zrefExists(fsr: Option[String]) = {
    fsr.isDefined
  }

  private def zrefIsCorrectPattern(fsr: Option[String]) = {
    fsr.getOrElse ("").matches ("^(z|Z)([0-9]{4}|[0-9]{6})$")
  }

  def uppercaseZ(tradingDetails: TradingDetails): TradingDetails = {
    TradingDetails(tradingDetails.fsrRefNumber, tradingDetails.isaProviderRefNumber.replace("z", "Z"))
  }
}