/*
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

package org.gatein.portal.application;

import org.gatein.portal.config.UserPortalConfigService;
import org.gatein.portal.mop.SiteKey;
import org.gatein.portal.mop.SiteType;
import org.gatein.portal.url.PortalURLContext;
import org.gatein.web.ControllerContext;
import org.gatein.web.WebRequestHandler;
import org.gatein.web.url.URLFactoryService;
import org.gatein.web.url.navigation.NavigationResource;
import org.gatein.web.url.navigation.NodeURL;

import javax.servlet.http.HttpServletResponse;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class DefaultRequestHandler extends WebRequestHandler
{

   /** . */
   private final UserPortalConfigService configService;

   /** . */
   private final URLFactoryService urlFactory;

   public DefaultRequestHandler(UserPortalConfigService configService, URLFactoryService urlFactory)
   {
      this.configService = configService;
      this.urlFactory = urlFactory;
   }

   @Override
   public String getHandlerName()
   {
      return "default";
   }

   @Override
   public boolean execute(ControllerContext context) throws Exception
   {
      String defaultPortal = configService.getDefaultPortal();
      PortalURLContext urlContext = new PortalURLContext(context, SiteKey.portal(defaultPortal));
      NodeURL url = urlFactory.newURL(NodeURL.TYPE, urlContext);
      String s = url.setResource(new NavigationResource(SiteType.PORTAL, defaultPortal, "")).toString();
      HttpServletResponse resp = context.getResponse();
      resp.sendRedirect(resp.encodeRedirectURL(s));
      return true;
   }

   @Override
   protected boolean getRequiresLifeCycle()
   {
      return false;
   }
}
