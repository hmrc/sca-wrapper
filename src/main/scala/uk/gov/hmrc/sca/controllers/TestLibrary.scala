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
import uk.gov.hmrc.sca.models.{MenuItemConfig, PtaMenuConfig, WrapperDataRequest}
import uk.gov.hmrc.sca.views.html.PtaMenuBar

import javax.inject.Inject
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

class TestLibrary @Inject()(ptaMenuBar: PtaMenuBar, scaWrapperDataConnector: ScaWrapperDataConnector, appConfig: AppConfig) {

  private def sortMenuItemConfig(menuItemConfig: Seq[MenuItemConfig]): PtaMenuConfig = {
    PtaMenuConfig(menuItemConfig.filter(_.leftAligned).sortBy(_.position), menuItemConfig.filterNot(_.leftAligned).sortBy(_.position))
  }

  def layout = {

  }

  def menu(implicit ec: ExecutionContext, hc: HeaderCarrier): HtmlFormat.Appendable = {
    val menuItems = Await.ready(scaWrapperDataConnector.wrapperData(WrapperDataRequest(appConfig.signoutUrl)), Duration("10 seconds")).value.get.get //pls forgive me, will change
    ptaMenuBar(sortMenuItemConfig(menuItems.menuItemConfig))
  }

}
