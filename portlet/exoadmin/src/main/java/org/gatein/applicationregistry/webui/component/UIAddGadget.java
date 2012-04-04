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

package org.gatein.applicationregistry.webui.component;

import org.gatein.application.gadget.Gadget;
import org.gatein.application.gadget.GadgetRegistryService;
import org.gatein.commons.serialization.api.annotations.Serialized;
import org.gatein.portal.webui.application.GadgetUtil;
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
import org.gatein.webui.form.validator.URLValidator;

import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Thanh Tung
 *          thanhtungty@gmail.com
 * Jul 4, 2008  
 */

@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIForm.gtmpl", events = {
   @EventConfig(listeners = UIAddGadget.AddActionListener.class),
   @EventConfig(listeners = UIAddGadget.CancelActionListener.class, phase = Phase.DECODE)})
@Serialized
public class UIAddGadget extends UIForm
{

   static final String FIELD_URL = "url";

   public UIAddGadget() throws Exception
   {
      addUIFormInput(new UIFormStringInput(FIELD_URL, null, null).addValidator(MandatoryValidator.class).addValidator(
         URLValidator.class));
   }

   public static class AddActionListener extends EventListener<UIAddGadget>
   {

      public void execute(Event<UIAddGadget> event) throws Exception
      {
         UIAddGadget uiForm = event.getSource();
         WebuiRequestContext context = event.getRequestContext();
         GadgetRegistryService service = uiForm.getApplicationComponent(GadgetRegistryService.class);
         String url = uiForm.getUIStringInput(FIELD_URL).getValue();
         String name = "gadget" + url.hashCode();
         UIGadgetManagement uiManagement = uiForm.getParent();
         // check url exist
         boolean urlExist = checkUrlExist(uiManagement.getGadgets(), url);
         if (urlExist == true)
         {
            UIApplication uiApp = context.getUIApplication();
            uiApp.addMessage(new ApplicationMessage("UIAddGadget.label.urlExist", null));
            return;
         }
         Gadget gadget;
         try
         {
            gadget = GadgetUtil.toGadget(name, url, false);
         }
         catch (Exception e)
         {
            UIApplication uiApp = context.getUIApplication();
            uiApp.addMessage(new ApplicationMessage("UIAddGadget.label.urlError", new String[]{url}));
            return;
         }
         service.saveGadget(gadget);
         uiManagement.initData();
         uiManagement.setSelectedGadget(name);
         context.addUIComponentToUpdateByAjax(uiManagement);
      }

      private boolean checkUrlExist(List<Gadget> gadgets, String url) throws Exception
      {
         for (Gadget ele : gadgets)
         {
            String urlReRrocedure = GadgetUtil.reproduceUrl(ele.getUrl(), ele.isLocal());
            if (urlReRrocedure.equals(url))
            {
               return true;
            }
         }
         return false;
      }
   }

   public static class CancelActionListener extends EventListener<UIAddGadget>
   {

      public void execute(Event<UIAddGadget> event) throws Exception
      {
         UIAddGadget uiForm = event.getSource();
         UIGadgetManagement uiManagement = uiForm.getParent();
         Gadget selectedGadget = uiManagement.getSelectedGadget();
         if (selectedGadget != null)
         {
            uiManagement.setSelectedGadget(selectedGadget.getName());
         }
         else
            uiManagement.reload();
         event.getRequestContext().addUIComponentToUpdateByAjax(uiManagement);
      }

   }

}