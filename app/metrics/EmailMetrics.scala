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

package metrics

import java.util.concurrent.TimeUnit
import com.codahale.metrics.MetricRegistry

import uk.gov.hmrc.play.graphite.MicroserviceMetrics


trait EmailMetrics {
  def emailSentCounter(): Unit
  def emailNotSentCounter(): Unit
}

object EmailMetrics extends EmailMetrics with MicroserviceMetrics {

  val registry: MetricRegistry = metrics.defaultRegistry

  override def emailNotSentCounter(): Unit = registry.counter("emailNotSentCounter").inc()

  override def emailSentCounter(): Unit = registry.counter("emailSentCounter").inc()
}
