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

package org.gatein.web.portal;

import org.gatein.component.test.ConfigurationUnit;
import org.gatein.component.test.ConfiguredBy;
import org.gatein.component.test.ContainerScope;
import org.gatein.portal.AbstractPortalTest;
import org.gatein.portal.config.DataStorage;
import org.gatein.portal.config.UserPortalConfig;
import org.gatein.portal.config.UserPortalConfigService;
import org.gatein.portal.config.model.PortalConfig;
import org.gatein.portal.mop.SiteKey;
import org.gatein.portal.mop.navigation.NavigationContext;
import org.gatein.portal.mop.navigation.NavigationService;
import org.gatein.portal.mop.navigation.NavigationState;
import org.gatein.portal.mop.navigation.NodeContext;
import org.gatein.portal.mop.navigation.NodeModel;
import org.gatein.portal.mop.navigation.Scope;
import org.gatein.portal.mop.user.SimpleUserPortalContext;
import org.gatein.portal.mop.user.UserNavigation;
import org.gatein.portal.mop.user.UserPortal;
import org.gatein.services.resources.Orientation;
import org.gatein.web.application.RequestContext;
import org.gatein.web.application.URLBuilder;
import org.gatein.web.url.PortalURL;
import org.gatein.web.url.ResourceType;
import org.gatein.web.url.URLFactory;

import java.util.List;
import java.util.Locale;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
@ConfiguredBy({
   @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"),
   @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration.xml"),
   @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration.xml"),
   @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "org/gatein/web/portal/configuration.xml")
})
public class TestRefreshCurrentUserPortal extends AbstractPortalTest
{

   /** . */
   private RequestContext requestContext;

   /** . */
   private UserPortal userPortal;

   @Override
   protected void setUp() throws Exception
   {
      begin();

      UserPortalConfigService upcs = (UserPortalConfigService)getContainer().getComponentInstanceOfType(UserPortalConfigService.class);
      UserPortalConfig upc = upcs.getUserPortalConfig("classic", "root", new SimpleUserPortalContext(Locale.ENGLISH));
      final UserPortal userPortal = upc.getUserPortal();

      //
      RequestContext requestContext = new RequestContext(null)
      {
         @Override
         public URLFactory getURLFactory()
         {
            throw new UnsupportedOperationException();
         }

         @Override
         public <R, U extends PortalURL<R, U>> U newURL(ResourceType<R, U> resourceType, URLFactory urlFactory)
         {
            throw new UnsupportedOperationException();
         }

         @Override
         public Orientation getOrientation()
         {
            throw new UnsupportedOperationException();
         }

         @Override
         public String getRequestParameter(String name)
         {
            throw new UnsupportedOperationException();
         }

         @Override
         public String[] getRequestParameterValues(String name)
         {
            throw new UnsupportedOperationException();
         }

         @Override
         public URLBuilder<?> getURLBuilder()
         {
            throw new UnsupportedOperationException();
         }

         @Override
         public boolean useAjax()
         {
            throw new UnsupportedOperationException();
         }

         @Override
         public UserPortal getUserPortal()
         {
            return userPortal;
         }
      };

      //
      this.userPortal = userPortal;
      this.requestContext  = requestContext;
   }

   @Override
   protected void tearDown() throws Exception
   {
      end();
   }

   public void testCreate() throws Exception
   {
      List<UserNavigation> navs = userPortal.getNavigations();
      assertEquals(2, navs.size());
      RequestContext.setCurrentInstance(requestContext);
      NavigationService ns = (NavigationService)getContainer().getComponentInstanceOfType(NavigationService.class);
      ns.saveNavigation(new NavigationContext(SiteKey.group("/platform"), new NavigationState(1)));
      navs = userPortal.getNavigations();
      assertEquals(3, navs.size());
      RequestContext.setCurrentInstance(null);
   }

   public void testUpdate() throws Exception
   {
      List<UserNavigation> navs = userPortal.getNavigations();
      assertEquals(2, navs.size());
      NavigationService ns = (NavigationService)getContainer().getComponentInstanceOfType(NavigationService.class);
      NavigationContext nav = new NavigationContext(SiteKey.group("/platform"), new NavigationState(1));
      ns.saveNavigation(nav);
      navs = userPortal.getNavigations();
      assertEquals(2, navs.size());
      RequestContext.setCurrentInstance(requestContext);
      NodeContext root = ns.loadNode(NodeModel.SELF_MODEL, nav, Scope.ALL, null);
      root.add(null, "foo");
      ns.saveNode(root, null);
      navs = userPortal.getNavigations();
      assertEquals(3, navs.size());
      RequestContext.setCurrentInstance(null);
   }

   public void testDestroy() throws Exception
   {
      NavigationService ns = (NavigationService)getContainer().getComponentInstanceOfType(NavigationService.class);
      NavigationContext nav = new NavigationContext(SiteKey.group("/platform"), new NavigationState(1));
      ns.saveNavigation(nav);
      List<UserNavigation> navs = userPortal.getNavigations();
      assertEquals(3, navs.size());
      RequestContext.setCurrentInstance(requestContext);
      ns.destroyNavigation(nav);
      navs = userPortal.getNavigations();
      assertEquals(2, navs.size());
      RequestContext.setCurrentInstance(null);
   }
}
