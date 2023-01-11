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

package uk.gov.hmrc.sca.controllers

import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.sca.config.AppConfig
import uk.gov.hmrc.sca.connectors.ScaWrapperDataConnector
import uk.gov.hmrc.sca.models.{MenuItemConfig, PtaMenuConfig}
import uk.gov.hmrc.sca.views.html.PtaMenuBar

import javax.inject.Inject
import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success, Try}

class TestLibrary @Inject()(ptaMenuBar: PtaMenuBar, scaWrapperDataConnector: ScaWrapperDataConnector, appConfig: AppConfig) {

  val ptaHost = "http://localhost:9232/personal-account"
  val btaHost = "http://localhost:9020/business-tax-account"

  private def sortMenuItemConfig(menuItemConfig: Seq[MenuItemConfig]): PtaMenuConfig = {
    PtaMenuConfig(menuItemConfig.filter(_.leftAligned), menuItemConfig.filterNot(_.leftAligned))
  }

  def menu(implicit ec: ExecutionContext, hc: HeaderCarrier): HtmlFormat.Appendable = {
    Try(Await.ready(scaWrapperDataConnector.getWrapperData, appConfig.wrapperDataTimeout)) match {
        case Success(res) => res.value.get match {
          case Success(menuItems) => ptaMenuBar(sortMenuItemConfig(menuItems))
          case Failure(exception) => ptaMenuBar(sortMenuItemConfig(appConfig.fallbackMenuConfig))
        }
        case Failure(exception) => ptaMenuBar(sortMenuItemConfig(appConfig.fallbackMenuConfig))
    }
  }

}
