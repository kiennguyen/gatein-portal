/**
 * Copyright (C) 2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.gatein.organization.webui.component;

import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileHandler;
import org.gatein.commons.serialization.api.annotations.Serialized;
import org.gatein.portal.Constants;
import org.gatein.portal.application.PortalRequestContext;
import org.gatein.portal.webui.util.Util;
import org.gatein.portal.webui.workspace.UIPortalApplication;
import org.gatein.portal.webui.workspace.UIWorkingWorkspace;
import org.gatein.services.resources.LocaleConfig;
import org.gatein.services.resources.LocaleConfigService;
import org.gatein.webui.application.WebuiRequestContext;
import org.gatein.webui.config.annotation.ComponentConfig;
import org.gatein.webui.config.annotation.EventConfig;
import org.gatein.webui.core.UIPopupWindow;
import org.gatein.webui.core.lifecycle.UIFormLifecycle;
import org.gatein.webui.event.Event;
import org.gatein.webui.event.EventListener;
import org.gatein.webui.event.Event.Phase;
import org.gatein.webui.form.UIFormInputContainer;
import org.gatein.webui.form.UIFormInputSet;
import org.gatein.webui.form.UIFormTabPane;
import org.gatein.webui.organization.UIUserMembershipSelector;
import org.gatein.webui.organization.UIUserProfileInputSet;

/** Created by The eXo Platform SARL Author : chungnv nguyenchung136@yahoo.com Jun 23, 2006 10:07:15 AM */
@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIFormTabPane.gtmpl", events = {
   @EventConfig(listeners = UIUserInfo.SaveActionListener.class),
   @EventConfig(listeners = UIUserInfo.BackActionListener.class, phase = Phase.DECODE),
   @EventConfig(listeners = UIUserInfo.ToggleChangePasswordActionListener.class, phase = Phase.DECODE)})
@Serialized
public class UIUserInfo extends UIFormTabPane
{

   private String username_ = null;

   public UIUserInfo() throws Exception
   {
      super("UIUserInfo");

      UIFormInputSet accountInputSet = new UIAccountEditInputSet("UIAccountEditInputSet");
      addChild(accountInputSet);
      setSelectedTab(accountInputSet.getId());

      UIFormInputSet userProfileSet = new UIUserProfileInputSet("UIUserProfileInputSet");
      addChild(userProfileSet);

      UIFormInputContainer<?> uiUserMembershipSelectorSet = new UIUserMembershipSelector();
      addChild(uiUserMembershipSelectorSet);

      setActions(new String[]{"Save", "Back"});
   }

   public void setUser(String userName) throws Exception
   {
      username_ = userName;
      OrganizationService service = getApplicationComponent(OrganizationService.class);
      User user = service.getUserHandler().findUserByName(userName);

      getChild(UIAccountEditInputSet.class).setValue(user);
      getChild(UIUserProfileInputSet.class).setUserProfile(userName);

      UIUserMembershipSelector uiMembershipSelector = getChild(UIUserMembershipSelector.class);
      uiMembershipSelector.setUser(user);
   }

   public String getUserName()
   {
      return username_;
   }

   public void processRender(WebuiRequestContext context) throws Exception
   {
      super.processRender(context);
      UIUserMembershipSelector uiUserMembershipSelector = getChild(UIUserMembershipSelector.class);
      if (uiUserMembershipSelector == null)
      {
         return;
      }
      UIPopupWindow uiPopupWindow = uiUserMembershipSelector.getChild(UIPopupWindow.class);
      if (uiPopupWindow == null)
      {
         return;
      }
      uiPopupWindow.processRender(context);
   }

   static public class SaveActionListener extends EventListener<UIUserInfo>
   {
      public void execute(Event<UIUserInfo> event) throws Exception
      {
         UIUserInfo uiUserInfo = event.getSource();
         OrganizationService service = uiUserInfo.getApplicationComponent(OrganizationService.class);
         boolean save = uiUserInfo.getChild(UIAccountEditInputSet.class).save(service);
         if (!save)
         {
            return;
         }
         uiUserInfo.getChild(UIUserProfileInputSet.class).save(service, uiUserInfo.getUserName(), false);

         if (uiUserInfo.getUserName().equals(event.getRequestContext().getRemoteUser()))
         {
            UserProfileHandler hanlder = service.getUserProfileHandler();
            UserProfile userProfile = hanlder.findUserProfileByName(event.getRequestContext().getRemoteUser());
            String language = userProfile.getAttribute(Constants.USER_LANGUAGE);

            UIPortalApplication uiApp = Util.getUIPortalApplication();            
            if (language == null || language.trim().length() < 1)
               return;
            LocaleConfigService localeConfigService =
               event.getSource().getApplicationComponent(LocaleConfigService.class);
            LocaleConfig localeConfig = localeConfigService.getLocaleConfig(language);
            if (localeConfig == null)
               localeConfig = localeConfigService.getDefaultLocaleConfig();
            PortalRequestContext prqCtx = Util.getPortalRequestContext();
            prqCtx.setLocale(localeConfig.getLocale());

            Util.getPortalRequestContext().addUIComponentToUpdateByAjax(
               uiApp.findFirstComponentOfType(UIWorkingWorkspace.class));
            Util.getPortalRequestContext().ignoreAJAXUpdateOnPortlets(true);
         }
         
         UIAccountEditInputSet accountInput = uiUserInfo.getChild(UIAccountEditInputSet.class);
         UIUserProfileInputSet userProfile = uiUserInfo.getChild(UIUserProfileInputSet.class);
         uiUserInfo.setRenderSibling(UIListUsers.class);
         accountInput.reset();
         userProfile.reset();
         event.getRequestContext().setProcessRender(true);
      }
   }

   static public class BackActionListener extends EventListener<UIUserInfo>
   {
      public void execute(Event<UIUserInfo> event) throws Exception
      {
         UIUserInfo userInfo = event.getSource();
         UIAccountEditInputSet accountInput = userInfo.getChild(UIAccountEditInputSet.class);
         UIUserProfileInputSet userProfile = userInfo.getChild(UIUserProfileInputSet.class);
         userInfo.setRenderSibling(UIListUsers.class);
         accountInput.reset();
         userProfile.reset();
         event.getRequestContext().setProcessRender(true);
      }
   }

   static public class ToggleChangePasswordActionListener extends EventListener<UIUserInfo>
   {
      public void execute(Event<UIUserInfo> event) throws Exception
      {
         UIUserInfo userInfo = event.getSource();
         UIAccountEditInputSet uiAccountInput = userInfo.getChild(UIAccountEditInputSet.class);
         uiAccountInput.checkChangePassword();
      }
   }
}
