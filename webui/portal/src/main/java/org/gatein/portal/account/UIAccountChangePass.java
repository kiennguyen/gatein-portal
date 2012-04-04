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
import org.exoplatform.services.organization.User;
import org.gatein.portal.webui.util.Util;
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
import org.gatein.webui.form.validator.MandatoryValidator;
import org.gatein.webui.form.validator.PasswordStringLengthValidator;

/**
 * Created by The eXo Platform SARL
 * Author : tung.dang
 *          tungcnw@gmail.com
 */

@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIForm.gtmpl", events = {
   @EventConfig(listeners = UIAccountChangePass.SaveActionListener.class),
   @EventConfig(listeners = UIAccountChangePass.ResetActionListener.class, phase = Phase.DECODE)})
public class UIAccountChangePass extends UIForm
{

   // constructor
   public UIAccountChangePass() throws Exception
   {
      super();
      addUIFormInput(new UIFormStringInput("currentpass", "password", null).setType(UIFormStringInput.PASSWORD_TYPE)
         .addValidator(MandatoryValidator.class));
      addUIFormInput(new UIFormStringInput("newpass", "password", null).setType(UIFormStringInput.PASSWORD_TYPE)
         .addValidator(PasswordStringLengthValidator.class, 6, 30).addValidator(MandatoryValidator.class));
      addUIFormInput(new UIFormStringInput("confirmnewpass", "password", null).setType(UIFormStringInput.PASSWORD_TYPE)
         .addValidator(PasswordStringLengthValidator.class, 6, 30).addValidator(MandatoryValidator.class));
   }

   static public class ResetActionListener extends EventListener<UIAccountChangePass>
   {
      public void execute(Event<UIAccountChangePass> event) throws Exception
      {
         UIAccountChangePass uiForm = event.getSource();
         uiForm.reset();
         event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
      }
   }

   static public class SaveActionListener extends EventListener<UIAccountChangePass>
   {
      public void execute(Event<UIAccountChangePass> event) throws Exception
      {
         UIAccountChangePass uiForm = event.getSource();
         OrganizationService service = uiForm.getApplicationComponent(OrganizationService.class);
         WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
         UIApplication uiApp = context.getUIApplication();
         String username = Util.getPortalRequestContext().getRemoteUser();
         User user = service.getUserHandler().findUserByName(username);
         String currentPass = uiForm.getUIStringInput("currentpass").getValue();
         String newPass = uiForm.getUIStringInput("newpass").getValue();
         String confirmnewPass = uiForm.getUIStringInput("confirmnewpass").getValue();

         if (!service.getUserHandler().authenticate(username, currentPass))
         {
            uiApp.addMessage(new ApplicationMessage("UIAccountChangePass.msg.currentpassword-is-not-match", null, 1));
            uiForm.reset();
            event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
            return;
         }

         if (!newPass.equals(confirmnewPass))
         {
            uiApp.addMessage(new ApplicationMessage("UIAccountChangePass.msg.password-is-not-match", null, 1));
            uiForm.reset();
            event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
            return;
         }
         user.setPassword(newPass);
         uiApp.addMessage(new ApplicationMessage("UIAccountChangePass.msg.change.pass.success", null));
         service.getUserHandler().saveUser(user, true);
         uiForm.reset();
         event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
         UIAccountSetting ui = uiForm.getParent();
         ui.getChild(UIAccountProfiles.class).setRendered(true);
         ui.getChild(UIAccountChangePass.class).setRendered(false);
         event.getRequestContext().addUIComponentToUpdateByAjax(ui);
         return;
      }
   }
}
