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

package metrics

import com.codahale.metrics.MetricRegistry
import org.scalatestplus.play.PlaySpec

class EmailMetricsSpec extends PlaySpec {

  "EmailMetrics" must {

    "increment the emailSentCounter" in {
      val registry = new MetricRegistry
      val metrics  = new EmailMetrics(registry)

      metrics.emailSentCounter()
      metrics.emailSentCounter()

      registry.counter("emailSentCounter").getCount mustBe 2
    }

    "increment the emailNotSentCounter" in {
      val registry = new MetricRegistry
      val metrics  = new EmailMetrics(registry)

      metrics.emailNotSentCounter()

      registry.counter("emailNotSentCounter").getCount mustBe 1
    }

  }

}
