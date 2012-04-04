/**
 * Copyright (C) 2011 eXo Platform SAS.
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
package org.gatein.portal.webui.page;

import org.gatein.commons.serialization.api.annotations.Serialized;
import org.gatein.webui.config.annotation.ComponentConfig;
import org.gatein.webui.config.annotation.EventConfig;
import org.gatein.webui.core.lifecycle.UIFormLifecycle;
import org.gatein.webui.core.model.SelectItemOption;
import org.gatein.webui.event.Event;
import org.gatein.webui.event.EventListener;
import org.gatein.webui.form.UIForm;
import org.gatein.webui.form.UIFormInputSet;
import org.gatein.webui.form.UIFormSelectBox;
import org.gatein.webui.form.UIFormStringInput;
import org.gatein.webui.form.validator.ExpressionValidator;

import java.util.List;

/**
 * @author <a href="kienna@exoplatform.com">Kien Nguyen</a>
 * @version $Revision$
 */
@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/portal/webui/page/UIPageSearchForm.gtmpl", 
                  events = @EventConfig(listeners = UIPageSearchForm.QuickSearchActionListener.class))
@Serialized
public class UIPageSearchForm extends UIForm
{
   /**
    * The name of the quick search set
    */
   final static public String QUICK_SEARCH_SET = "QuickSearchSet";

   public UIPageSearchForm() throws Exception
   {
      UIFormInputSet uiQuickSearchSet = new UIFormInputSet(QUICK_SEARCH_SET);
      uiQuickSearchSet.addUIFormInput(new UIFormStringInput("pageTitle", "pageTitle", null));
      uiQuickSearchSet.addUIFormInput(new UIFormStringInput("siteName", "siteName", null).addValidator(ExpressionValidator.class, "[^\\'\"]*", "UISearchForm.msg.empty"));
      uiQuickSearchSet.addUIFormInput(new UIFormSelectBox("searchOption", null, null));
      addChild(uiQuickSearchSet);
   }

   public void setOptions(List<SelectItemOption<String>> options)
   {
      UIFormSelectBox uiSelect = (UIFormSelectBox)getQuickSearchInputSet().getChild(2);
      uiSelect.setOptions(options);
   }

   public UIFormInputSet getQuickSearchInputSet()
   {
      return (UIFormInputSet)getChild(0);
   }

   static public class QuickSearchActionListener extends EventListener<UIPageSearchForm>
   {
      public void execute(Event<UIPageSearchForm> event) throws Exception
      {
         UIPageSearchForm uiForm = event.getSource();
         UIPageBrowser uiSearch = uiForm.getParent();
         uiSearch.quickSearch(uiForm.getQuickSearchInputSet());
         event.getRequestContext().addUIComponentToUpdateByAjax(uiSearch);
      }
   }
}
