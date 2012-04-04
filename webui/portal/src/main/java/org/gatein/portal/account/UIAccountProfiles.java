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

package org.gatein.portal.account;

import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.security.ConversationState;
import org.gatein.portal.pom.config.Utils;
import org.gatein.portal.webui.util.Util;
import org.gatein.portal.webui.workspace.UIWorkingWorkspace;
import org.gatein.web.CacheUserProfileFilter;
import org.gatein.web.application.ApplicationMessage;
import org.gatein.webui.application.WebuiRequestContext;
import org.gatein.webui.config.annotation.ComponentConfig;
import org.gatein.webui.config.annotation.EventConfig;
import org.gatein.webui.core.UIApplication;
import org.gatein.webui.core.lifecycle.UIFormLifecycle;
import org.gatein.webui.event.Event;
import org.gatein.webui.event.EventListener;
import org.gatein.webui.event.Event.Phase;
import org.gatein.webui.form.UIForm;
import org.gatein.webui.form.UIFormStringInput;
import org.gatein.webui.form.validator.EmailAddressValidator;
import org.gatein.webui.form.validator.ExpressionValidator;
import org.gatein.webui.form.validator.MandatoryValidator;
import org.gatein.webui.form.validator.ResourceValidator;
import org.gatein.webui.form.validator.StringLengthValidator;

/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 */

@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIForm.gtmpl",

events = {@EventConfig(listeners = UIAccountProfiles.SaveActionListener.class),
   @EventConfig(listeners = UIAccountProfiles.ResetActionListener.class, phase = Phase.DECODE)})
public class UIAccountProfiles extends UIForm
{

   public UIAccountProfiles() throws Exception
   {
      super();
      String username = Util.getPortalRequestContext().getRemoteUser();
      OrganizationService service = this.getApplicationComponent(OrganizationService.class);
      User useraccount = service.getUserHandler().findUserByName(username);

      UIFormStringInput userName = new UIFormStringInput("userName", "userName", username);
      userName.setReadOnly(true);
      addUIFormInput(userName.addValidator(MandatoryValidator.class).addValidator(StringLengthValidator.class, 3, 30)
         .addValidator(ResourceValidator.class).addValidator(ExpressionValidator.class,
            Utils.USER_NAME_VALIDATOR_REGEX, "ResourceValidator.msg.Invalid-char"));
      addUIFormInput(new UIFormStringInput("firstName", "firstName", useraccount.getFirstName()).addValidator(
         StringLengthValidator.class, 1, 45).addValidator(MandatoryValidator.class));
      addUIFormInput(new UIFormStringInput("lastName", "lastName", useraccount.getLastName()).addValidator(
         StringLengthValidator.class, 1, 45).addValidator(MandatoryValidator.class));

      // TODO: GTNPORTAL-2358 switch to getDisplayName once it will be available in Organization API
      addUIFormInput(new UIFormStringInput("displayName", "displayName", useraccount.getFullName()).addValidator(
            StringLengthValidator.class, 0, 90));
      addUIFormInput(new UIFormStringInput("email", "email", useraccount.getEmail()).addValidator(
         MandatoryValidator.class).addValidator(EmailAddressValidator.class));
   }

   static public class ResetActionListener extends EventListener<UIAccountProfiles>
   {
      public void execute(Event<UIAccountProfiles> event) throws Exception
      {
         UIAccountProfiles uiForm = event.getSource();
         String userName = uiForm.getUIStringInput("userName").getValue();
         OrganizationService service = uiForm.getApplicationComponent(OrganizationService.class);
         User user = service.getUserHandler().findUserByName(userName);
         uiForm.getUIStringInput("firstName").setValue(user.getFirstName());
         uiForm.getUIStringInput("lastName").setValue(user.getLastName());
         // TODO: GTNPORTAL-2358 switch to getDisplayName once it will be available in Organization API
         uiForm.getUIStringInput("displayName").setValue(user.getFullName());
         uiForm.getUIStringInput("email").setValue(user.getEmail());
         event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
      }
   }

   static public class SaveActionListener extends EventListener<UIAccountProfiles>
   {
      public void execute(Event<UIAccountProfiles> event) throws Exception
      {
         UIAccountProfiles uiForm = event.getSource();
         OrganizationService service = uiForm.getApplicationComponent(OrganizationService.class);
         WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
         UIApplication uiApp = context.getUIApplication();

         String userName = uiForm.getUIStringInput("userName").getValue();
         User user = service.getUserHandler().findUserByName(userName);
         String oldEmail = user.getEmail();
         String newEmail = uiForm.getUIStringInput("email").getValue();

         // Check if mail address is already used
         Query query = new Query();
         query.setEmail(newEmail);
         if (service.getUserHandler().findUsers(query).getAll().size() > 0 && !oldEmail.equals(newEmail))
         {
            //Be sure it keep old value
            user.setEmail(oldEmail);
            Object[] args = {userName};
            uiApp.addMessage(new ApplicationMessage("UIAccountInputSet.msg.email-exist", args));
            return;
         }
         user.setFirstName(uiForm.getUIStringInput("firstName").getValue());
         user.setLastName(uiForm.getUIStringInput("lastName").getValue());

         // TODO: GTNPORTAL-2358 switch to setDisplayName once it will be available in Organization API
         user.setFullName(uiForm.getUIStringInput("displayName").getValue());
         user.setEmail(newEmail);
         uiApp.addMessage(new ApplicationMessage("UIAccountProfiles.msg.update.success", null));
         service.getUserHandler().saveUser(user, true);

         UIWorkingWorkspace uiWorkingWS = Util.getUIPortalApplication().getChild(UIWorkingWorkspace.class);
         ConversationState state = ConversationState.getCurrent();
         if (userName.equals(((User)state.getAttribute(CacheUserProfileFilter.USER_PROFILE)).getUserName()))
         {
            state.setAttribute(CacheUserProfileFilter.USER_PROFILE, user);
            uiWorkingWS.updatePortletsByName("UserInfoPortlet");
         }
         uiWorkingWS.updatePortletsByName("OrganizationPortlet");
      }
   }
}
