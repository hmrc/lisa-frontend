/*
 * Copyright 2026 HM Revenue & Customs
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

package connectors

import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.http.HttpResponse

class RawResponseReadsSpec extends PlaySpec with RawResponseReads {

  "RawResponseReads.httpReads" must {

    "return the response unchanged" in {
      val response = HttpResponse(status = 200, body = "payload")

      httpReads.read("GET", "/test", response) mustBe theSameInstanceAs(response)
    }

  }

}
