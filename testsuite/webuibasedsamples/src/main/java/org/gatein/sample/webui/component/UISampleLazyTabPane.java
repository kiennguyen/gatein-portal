package org.gatein.sample.webui.component;

import org.gatein.webui.config.annotation.ComponentConfig;
import org.gatein.webui.core.UIContainer;
import org.gatein.webui.core.UILazyTabPane;
import org.gatein.webui.core.lifecycle.UIContainerLifecycle;

@ComponentConfig(lifecycle=UIContainerLifecycle.class)
public class UISampleLazyTabPane extends UIContainer
{

   public UISampleLazyTabPane() throws Exception
   {
      UILazyTabPane uiLazyTabPane = addChild(UILazyTabPane.class, null, null);
      uiLazyTabPane.addChild(UISampleRightClickPopupMenu.class, null, null);
      uiLazyTabPane.addChild(UISampleRepeater.class, null, null);
      uiLazyTabPane.setSelectedTab(1);
   }
}
