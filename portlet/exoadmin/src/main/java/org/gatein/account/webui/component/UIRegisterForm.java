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

package org.gatein.account.webui.component;

import java.util.ArrayList;
import java.util.List;

import nl.captcha.Captcha;

import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.web.application.ApplicationMessage;
import org.gatein.webui.application.WebuiRequestContext;
import org.gatein.webui.application.portlet.PortletRequestContext;
import org.gatein.webui.config.annotation.ComponentConfig;
import org.gatein.webui.config.annotation.EventConfig;
import org.gatein.webui.core.UIApplication;
import org.gatein.webui.core.lifecycle.UIFormLifecycle;
import org.gatein.webui.event.Event;
import org.gatein.webui.event.EventListener;
import org.gatein.webui.event.Event.Phase;
import org.gatein.webui.exception.MessageException;
import org.gatein.webui.form.UIForm;
import org.gatein.webui.form.UIFormInputWithActions;
import org.gatein.webui.form.UIFormStringInput;
import org.gatein.webui.form.UIFormInputWithActions.ActionData;
import org.gatein.webui.form.validator.Validator;

/**
 * 
 * @author <a href="mailto:hoang281283@gmail.com">Minh Hoang TO</a>
 * @version $Id$
 *
 */

@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIFormWithTitle.gtmpl", events = {
   @EventConfig(listeners = UIRegisterForm.SubscribeActionListener.class),
   @EventConfig(listeners = UIRegisterForm.ResetActionListener.class, phase = Phase.DECODE),
   @EventConfig(name = UIRegisterForm.CheckUsernameAvailability.LISTENER_NAME, listeners = UIRegisterForm.CheckUsernameAvailability.class, phase = Phase.DECODE)})
public class UIRegisterForm extends UIForm
{

   private final static String[] ACTIONS = {"Subscribe", "Reset"};

   public UIRegisterForm() throws Exception
   {
      UIFormInputWithActions registerInput = new UIRegisterInputSet("RegisterInputSet");
      //Set actions on registerInput 's User Name field
      List<ActionData> fieldActions = new ArrayList<ActionData>();
      ActionData checkAvailable = new ActionData();
      checkAvailable.setActionListener(CheckUsernameAvailability.LISTENER_NAME);
      checkAvailable.setActionName(CheckUsernameAvailability.LISTENER_NAME);
      checkAvailable.setActionType(ActionData.TYPE_ICON);
      checkAvailable.setCssIconClass("SearchIcon");
      fieldActions.add(checkAvailable);
      registerInput.setActionField(UIRegisterInputSet.USER_NAME, fieldActions);

      addUIFormInput(registerInput);
      setActions(ACTIONS);
   }

   private void resetInput()
   {
      getChild(UIRegisterInputSet.class).reset();
   }

   @Override
   public void processAction(WebuiRequestContext context) throws Exception
   {
      super.processAction(context);
      
      if(context.getProcessRender())
      {
         //Invalidate the capcha
         if (context instanceof PortletRequestContext)
         {
            PortletRequestContext prc = (PortletRequestContext)context;
            prc.getRequest().getPortletSession().removeAttribute(Captcha.NAME);
         }
         context.addUIComponentToUpdateByAjax(getChild(UIRegisterInputSet.class));
      }
   }
   
   static public class SubscribeActionListener extends EventListener<UIRegisterForm>
   {
      @Override
      public void execute(Event<UIRegisterForm> event) throws Exception
      {
         UIRegisterForm registerForm = event.getSource();
         OrganizationService orgService = registerForm.getApplicationComponent(OrganizationService.class);
         UserHandler userHandler = orgService.getUserHandler();
         WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
         UIRegisterInputSet registerInput = registerForm.getChild(UIRegisterInputSet.class);

         if (registerInput.save(userHandler, context))
         {
            //TODO: Send email and add Account Activating feature
            UIApplication uiApp = context.getUIApplication();
            uiApp.addMessage(new ApplicationMessage("UIRegisterForm.registerWithSuccess.message", null));           
         }

         //Invalidate the capcha
         if (context instanceof PortletRequestContext)
         {
            PortletRequestContext prc = (PortletRequestContext)context;
            prc.getRequest().getPortletSession().removeAttribute(Captcha.NAME);
         }
      }
   }

   static public class CheckUsernameAvailability extends EventListener<UIRegisterForm>
   {

      final static String LISTENER_NAME = "CheckUsernameAvailability";

      @Override
      public void execute(Event<UIRegisterForm> event) throws Exception
      {
         UIRegisterForm registerForm = event.getSource();
         OrganizationService orgService = registerForm.getApplicationComponent(OrganizationService.class);
         UIRegisterInputSet registerInput = registerForm.getChild(UIRegisterInputSet.class);
         UIFormStringInput userNameInput = registerInput.getUIStringInput(UIRegisterInputSet.USER_NAME);
         List<Validator> validators = userNameInput.getValidators();
         for (Validator validator  : validators)
         {
            try
            {
               validator.validate(userNameInput);
            }
            catch (MessageException e)
            {
               event.getRequestContext().getUIApplication().addMessage(e.getDetailMessage());
               return;
            }
         }

         String typedUsername = userNameInput.getValue();
         WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
         UIApplication uiApp = context.getUIApplication();
         if (usernameIsUsed(typedUsername, orgService))
         {
            uiApp.addMessage(new ApplicationMessage("UIAccountInputSet.msg.user-exist", new String[]{typedUsername},
               ApplicationMessage.WARNING));
         }
         else
         {
            uiApp.addMessage(new ApplicationMessage("UIAccountInputSet.msg.user-not-exist", new String[]{typedUsername}, ApplicationMessage.INFO));
         }
      }

      private boolean usernameIsUsed(String username, OrganizationService orgService)
      {
         UserHandler userHandler = orgService.getUserHandler();
         try
         {
            if (userHandler.findUserByName(username) != null)
            {
               return true;
            }
         }
         catch (Exception ex)
         {
            ex.printStackTrace();
         }
         return false;
      }
   }

   static public class ResetActionListener extends EventListener<UIRegisterForm>
   {
      @Override
      public void execute(Event<UIRegisterForm> event) throws Exception
      {
         UIRegisterForm registerForm = event.getSource();
         registerForm.resetInput();
      }
   }
}
