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

package models

import play.api.data.Form
import play.api.data.Forms.{mapping, number, optional, text}

case class Questionnaire(
                          easyToUse: Option[Int],
                          satisfactionLevel: Option[Int],
                          whyGiveThisRating: Option[String],
                          referer: Option[String]
                        )

object Questionnaire {
  val maxOptionSize = 4
  val maxBooleanOptionSize = 2
  val form: Form[Questionnaire] = Form(mapping(
    "easyToUse" -> optional(number(0, maxOptionSize)),
    "satisfactionLevel" -> optional(number(0, maxOptionSize)),
    "whyGiveThisRating" -> optional(text),
    "referer" -> optional(text))(Questionnaire.apply)(Questionnaire.unapply)
  )
}
