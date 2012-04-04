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

import org.gatein.portal.config.UserPortalConfig;
import org.gatein.portal.config.UserPortalConfigService;
import org.gatein.portal.mop.SiteKey;
import org.gatein.portal.mop.user.UserNavigation;
import org.gatein.portal.mop.user.UserNode;
import org.gatein.portal.mop.user.UserNodeFilterConfig;
import org.gatein.portal.mop.user.UserPortal;
import org.gatein.portal.mop.user.UserPortalContext;
import org.gatein.portal.url.PortalURLContext;
import org.gatein.web.ControllerContext;
import org.gatein.web.WebRequestHandler;
import org.gatein.web.url.MimeType;
import org.gatein.web.url.URLFactoryService;
import org.gatein.web.url.navigation.NavigationResource;
import org.gatein.web.url.navigation.NodeURL;

import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This handler resolves legacy request and redirect them to the new URL computed dynamically against the
 * routing table.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class LegacyRequestHandler extends WebRequestHandler
{

   /** . */
   private final URLFactoryService urlFactory;

   /** . */
   private final UserPortalConfigService userPortalService;
   
   /** . */
   private final UserPortalContext userPortalContext = new UserPortalContext()
   {
      public ResourceBundle getBundle(UserNavigation navigation)
      {
         return null;
      }

      public Locale getUserLocale()
      {
         return Locale.ENGLISH;
      }
   };

   public LegacyRequestHandler(URLFactoryService urlFactory, UserPortalConfigService userPortalService)
   {
      this.urlFactory = urlFactory;
      this.userPortalService = userPortalService;
   }

   @Override
   public String getHandlerName()
   {
      return "legacy";
   }

   @Override
   public boolean execute(ControllerContext context) throws Exception
   {
      String requestSiteName = context.getParameter(PortalRequestHandler.REQUEST_SITE_NAME);
      String requestPath = context.getParameter(PortalRequestHandler.REQUEST_PATH);
      
      SiteKey siteKey = SiteKey.portal(requestSiteName);
      String uri = requestPath;

      // Resolve the user node if node path is indicated
      if (!requestPath.equals(""))
      {
         UserPortalConfig cfg = userPortalService.getUserPortalConfig(requestSiteName, context.getRequest().getRemoteUser(), userPortalContext);
         if (cfg != null)
         {
            UserPortal userPortal = cfg.getUserPortal();
            UserNodeFilterConfig.Builder builder = UserNodeFilterConfig.builder().withAuthMode(UserNodeFilterConfig.AUTH_READ);
            UserNode userNode = userPortal.resolvePath(builder.build(), requestPath);

            if (userNode != null)
            {
               siteKey = userNode.getNavigation().getKey();
               uri = userNode.getURI();
            }
         }
      }

      //
      PortalURLContext urlContext = new PortalURLContext(context, siteKey);
      NodeURL url = urlFactory.newURL(NodeURL.TYPE, urlContext);

      url.setResource(new NavigationResource(siteKey.getType(), siteKey.getName(), uri));
      url.setMimeType(MimeType.PLAIN);

      HttpServletRequest request = context.getRequest();
      Enumeration paraNames = request.getParameterNames();
      while (paraNames.hasMoreElements())
      {
         String parameter = paraNames.nextElement().toString();
         url.setQueryParameterValues(parameter, request.getParameterValues(parameter));
      }

      String s = url.toString();

      HttpServletResponse resp = context.getResponse();
      resp.sendRedirect(resp.encodeRedirectURL(s));
      return true;
   }

   @Override
   protected boolean getRequiresLifeCycle()
   {
      return true;
   }
}
