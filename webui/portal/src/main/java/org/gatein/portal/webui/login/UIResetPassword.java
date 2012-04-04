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

package org.gatein.portal.webui.login;

import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.gatein.portal.webui.workspace.UIMaskWorkspace;
import org.gatein.portal.webui.workspace.UIPortalApplication;
import org.gatein.web.application.ApplicationMessage;
import org.gatein.web.security.Token;
import org.gatein.web.security.security.RemindPasswordTokenService;
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
import org.gatein.webui.form.validator.MandatoryValidator;
import org.gatein.webui.form.validator.StringLengthValidator;

/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Jul 09, 2008
 */
@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIFormWithTitle.gtmpl", events = {
   @EventConfig(listeners = UIResetPassword.SaveActionListener.class),
   @EventConfig(phase = Phase.DECODE, listeners = UIMaskWorkspace.CloseActionListener.class)})
public class UIResetPassword extends UIForm
{
   final static String USER_NAME = "username";

   final static String NEW_PASSWORD = "newpassword";

   final static String CONFIRM_NEW_PASSWORD = "confirmnewpassword";

   static User user_;

   private String tokenId;

   public UIResetPassword() throws Exception
   {
      addUIFormInput(new UIFormStringInput(USER_NAME, USER_NAME, null).setReadOnly(true));
      addUIFormInput(((UIFormStringInput)new UIFormStringInput(NEW_PASSWORD, NEW_PASSWORD, null)).setType(
         UIFormStringInput.PASSWORD_TYPE).addValidator(MandatoryValidator.class).addValidator(
         StringLengthValidator.class, 6, 30));
      addUIFormInput(((UIFormStringInput)new UIFormStringInput(CONFIRM_NEW_PASSWORD, CONFIRM_NEW_PASSWORD, null))
         .setType(UIFormStringInput.PASSWORD_TYPE).addValidator(MandatoryValidator.class).addValidator(
            StringLengthValidator.class, 6, 30));
   }

   public void setUser(User user)
   {
      user_ = user;
      getUIStringInput(USER_NAME).setValue(user.getUserName());
   }

   public void setTokenId(String tokenId)
   {
      this.tokenId = tokenId;
   }

   public String getTokenId()
   {
      return this.tokenId;
   }

   @Override
   public void reset()
   {
      UIFormStringInput newPasswordForm = getUIStringInput(NEW_PASSWORD);
      newPasswordForm.reset();
      UIFormStringInput confirmPasswordForm = getUIStringInput(CONFIRM_NEW_PASSWORD);
      confirmPasswordForm.reset();
   }

   static public class SaveActionListener extends EventListener<UIResetPassword>
   {
      public void execute(Event<UIResetPassword> event) throws Exception
      {
         UIResetPassword uiForm = event.getSource();
         String newpassword = uiForm.getUIStringInput(NEW_PASSWORD).getValue();
         String confirmnewpassword = uiForm.getUIStringInput(CONFIRM_NEW_PASSWORD).getValue();
         WebuiRequestContext request = event.getRequestContext();
         UIApplication uiApp = request.getUIApplication();
         UIMaskWorkspace uiMaskWorkspace = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);
         OrganizationService orgService = uiForm.getApplicationComponent(OrganizationService.class);
         RemindPasswordTokenService tokenService = uiForm.getApplicationComponent(RemindPasswordTokenService.class);
         
         uiForm.reset();
         boolean setPassword = true;
         
         if (!newpassword.equals(confirmnewpassword))
         {
            uiApp.addMessage(new ApplicationMessage("UIResetPassword.msg.password-is-not-match", null));
            setPassword = false;
         }
         
         Token token = tokenService.deleteToken(uiForm.getTokenId());
         if (token == null || token.isExpired())
         {
            uiApp.addMessage(new ApplicationMessage("UIForgetPassword.msg.expration", null));
            setPassword = false;
         }
         
         if (setPassword)
         {
            user_.setPassword(newpassword);
            orgService.getUserHandler().saveUser(user_, true);
            uiMaskWorkspace.createEvent("Close", Phase.DECODE, request).broadcast();
            uiApp.addMessage(new ApplicationMessage("UIResetPassword.msg.change-password-successfully", null));
         }
         request.addUIComponentToUpdateByAjax(uiMaskWorkspace);
      }
   }

}
