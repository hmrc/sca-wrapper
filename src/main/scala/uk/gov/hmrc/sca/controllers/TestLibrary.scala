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

import uk.gov.hmrc.sca.models.{PtaMenuConfig, PtaMenuItemConfig}
import uk.gov.hmrc.sca.views.html.{PtaMenuBar, SCALayout}

import javax.inject.Inject

class TestLibrary @Inject()(ptaMenuBar: PtaMenuBar) {

  val ptaHost = "http://localhost:9232/personal-account"
  val btaHost = "http://localhost:9020/business-tax-account"

  def signoutParams(continueUrl: Option[String], origin: Option[String]) = {
    val contUrl = s"${continueUrl.fold(""){url => s"continueUrl=$url"}}"
    val originUrl = s"${origin.fold(""){url => s"origin=$url"}}"
    (contUrl, originUrl) match {
      case _ if contUrl.nonEmpty && origin.nonEmpty => s"?$contUrl&$originUrl"
      case _ if contUrl.isEmpty && origin.isEmpty => ""
      case x@_ => s"?${x._1}${x._2}"
    }
  }

  def menu = {
    ptaMenuBar(PtaMenuConfig(
      leftAlignedItems = Seq(
        PtaMenuItemConfig(true, 0, Some("hmrc-account-icon hmrc-account-icon--home"), "Account Home", s"$ptaHost/test", true, None)
      ),
      rightAlignedItems = Seq(
        PtaMenuItemConfig(false, 0, None, "Messages", s"$ptaHost/messages", false, None),
        PtaMenuItemConfig(false, 1, None, "Check progress", s"$ptaHost/track", false, None),
        PtaMenuItemConfig(false, 2, None, "Profile and settings", s"$ptaHost/profile-and-settings", false, None),
        PtaMenuItemConfig(false, 3, None, "Business tax account", s"$btaHost/business-account", false, None),
        PtaMenuItemConfig(false, 4, None, "Sign out", s"$ptaHost/signout${signoutParams(None, None)}", false, None)
      )
      ))
  }

}
