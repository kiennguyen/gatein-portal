package org.gatein.sample.webui.component;

import org.gatein.webui.config.annotation.ComponentConfig;
import org.gatein.webui.config.annotation.EventConfig;
import org.gatein.webui.core.UIPortletApplication;
import org.gatein.webui.core.lifecycle.UIApplicationLifecycle;
import org.gatein.webui.event.Event;
import org.gatein.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS Author : Nguyen Duc Khoi
 * khoi.nguyen@exoplatform.com Apr 28, 2010
 */

@ComponentConfig(lifecycle = UIApplicationLifecycle.class, template = "app:/groovy/webui/component/UISamplePortlet.gtmpl", events = {@EventConfig(listeners = UISamplePortlet.ClickSplitBarActionListener.class)})
public class UISamplePortlet extends UIPortletApplication
{
   public UISamplePortlet() throws Exception
   {
      addChild(UISampleTOC.class, null, null);
      addChild(UISampleContent.class, null, null);
   }

   public void showUIComponent(int nodeType)
   {
      getChild(UISampleContent.class).showUIComponent(nodeType);
   }
   
   public static class ClickSplitBarActionListener extends EventListener<UISamplePortlet>
   {
      @Override
      public void execute(Event<UISamplePortlet> event) throws Exception
      {
         UISampleTOC uiSampleTOC = event.getSource().getChild(UISampleTOC.class);
         uiSampleTOC.setRendered(!uiSampleTOC.isRendered());
      }
   }
}
