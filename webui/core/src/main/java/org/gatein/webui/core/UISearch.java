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

package org.gatein.webui.core;

import org.gatein.commons.serialization.api.annotations.Serialized;
import org.gatein.webui.config.annotation.ComponentConfig;
import org.gatein.webui.core.UIContainer;
import org.gatein.webui.core.model.SelectItemOption;
import org.gatein.webui.form.UIFormInputSet;
import org.gatein.webui.form.UISearchForm;

import java.util.List;

/**
 * Author : Nguyen Viet Chung
 *          chung.nguyen@exoplatform.com
 * Jun 22, 2006
 * @version: $Id$
 * 
 * A container that holds a UISearchForm
 * @see UISearchForm
 */
@ComponentConfig()
@Serialized
abstract public class UISearch extends UIContainer
{

   public UISearch(List<SelectItemOption<String>> searchOption) throws Exception
   {
      UISearchForm uiForm = addChild(UISearchForm.class, null, null);
      uiForm.setOptions(searchOption);
   }

   public UISearchForm getUISearchForm()
   {
      return (UISearchForm)getChild(0);
   }

   abstract public void quickSearch(UIFormInputSet quickSearchInput) throws Exception;

   abstract public void advancedSearch(UIFormInputSet advancedSearchInput) throws Exception;
}
