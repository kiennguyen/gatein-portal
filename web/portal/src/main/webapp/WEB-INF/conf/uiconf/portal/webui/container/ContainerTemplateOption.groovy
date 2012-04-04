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

import org.gatein.webui.core.model.SelectItemOption ;
import org.gatein.webui.core.model.SelectItemCategory ;
import java.util.List;
import java.util.ArrayList;

  List options = new ArrayList() ;
  
  SelectItemCategory defaultTemp  = new SelectItemCategory("Default");
  defaultTemp.addSelectItemOption(new SelectItemOption("Container template",
                                  "system:/groovy/portal/webui/container/UIContainer.gtmpl",
                                  "Description", "Default"));  
  options.add(defaultTemp);
  
  SelectItemCategory tableTemp  = new SelectItemCategory("Table Column");
  tableTemp.addSelectItemOption(new SelectItemOption("Table Column template",
                                "system:/groovy/portal/webui/container/UITableColumnContainer.gtmpl",
                                "Description","Default"));  
  options.add(tableTemp);

return options;