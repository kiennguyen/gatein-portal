package org.gatein.sample.webui.component;

import org.gatein.web.application.ApplicationMessage;
import org.gatein.webui.application.WebuiRequestContext;
import org.gatein.webui.application.portlet.PortletRequestContext;
import org.gatein.webui.config.annotation.ComponentConfig;
import org.gatein.webui.config.annotation.EventConfig;
import org.gatein.webui.core.UIApplication;
import org.gatein.webui.core.UIContainer;
import org.gatein.webui.event.Event;
import org.gatein.webui.event.EventListener;

@ComponentConfig(template = "app:/groovy/webui/component/UISamplePopupMessage.gtmpl", events = {@EventConfig(listeners = UISamplePopupMessage.ShowPopupMessageActionListener.class)})
public class UISamplePopupMessage extends UIContainer
{
   static public class ShowPopupMessageActionListener extends EventListener<UISamplePopupMessage>
   {

      @Override
      public void execute(Event<UISamplePopupMessage> event) throws Exception
      {
         int popupType = Integer.parseInt(event.getRequestContext().getRequestParameter(OBJECTID));

         UIApplication uiApp =
            ((PortletRequestContext)WebuiRequestContext.getCurrentInstance()).getUIApplication();
         uiApp.addMessage(new ApplicationMessage("Test Message", null, popupType));
      }

   }
}
