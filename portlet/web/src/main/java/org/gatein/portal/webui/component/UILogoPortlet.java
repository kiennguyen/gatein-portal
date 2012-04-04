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

package org.gatein.portal.webui.component;

import org.exoplatform.services.organization.User;
import org.exoplatform.services.security.ConversationState;
import org.gatein.portal.mop.SiteType;
import org.gatein.portal.mop.user.UserNavigation;
import org.gatein.portal.mop.user.UserNode;
import org.gatein.portal.webui.util.Util;
import org.gatein.web.CacheUserProfileFilter;
import org.gatein.webui.application.WebuiRequestContext;
import org.gatein.webui.application.portlet.PortletRequestContext;
import org.gatein.webui.config.annotation.ComponentConfig;
import org.gatein.webui.core.UIPortletApplication;
import org.gatein.webui.core.lifecycle.UIApplicationLifecycle;
import org.gatein.webui.organization.OrganizationUtils;

import javax.portlet.PortletPreferences;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform October 2, 2009
 */
@ComponentConfig(lifecycle = UIApplicationLifecycle.class, template = "app:/groovy/portal/webui/component/UILogoPortlet.gtmpl")
public class UILogoPortlet extends UIPortletApplication
{
   public UILogoPortlet() throws Exception
   {
      addChild(UILogoEditMode.class, null, null);
   }

   public String getURL()
   {
      PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance();
      PortletPreferences pref = pcontext.getRequest().getPreferences();
      return pref.getValue("url", "");
   }

   public String getNavigationTitle() throws Exception
   {
      UserNode navPath = Util.getUIPortal().getNavPath();
      UserNavigation nav = navPath.getNavigation();
      if (nav.getKey().getType().equals(SiteType.GROUP))
      {
         return OrganizationUtils.getGroupLabel(nav.getKey().getName());
      }
      else if (nav.getKey().getType().equals(SiteType.USER))
      {
         ConversationState state = ConversationState.getCurrent();
         User user = (User)state.getAttribute(CacheUserProfileFilter.USER_PROFILE);
         return user.getFullName();
      }
      return "";
   }
}
