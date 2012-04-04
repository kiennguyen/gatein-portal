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

package org.gatein.portal.webui.application;

import org.gatein.webui.application.WebuiRequestContext;
import org.gatein.webui.config.annotation.ComponentConfig;
import org.gatein.webui.form.UIFormInputBase;

/**
 * Created by The eXo Platform SARL
 * Author : Tung Pham
 *          tung.pham@exoplatform.com
 * Nov 5, 2007  
 */

@ComponentConfig(template = "system:/groovy/webui/form/UIFormInputThemeSelector.gtmpl")
public class UIFormInputThemeSelector extends UIFormInputBase<String>
{

   final static private String FIELD_THEME = "UIItemThemeSelector";

   public UIFormInputThemeSelector(String name, String bindingField) throws Exception
   {
      super(name, bindingField, String.class);
      setComponentConfig(UIFormInputThemeSelector.class, null);
      UIItemThemeSelector uiThemeSelector = new UIItemThemeSelector(FIELD_THEME, null);
      addChild(uiThemeSelector);
   }

   public void decode(Object input, WebuiRequestContext context) throws Exception
   {
   }
}