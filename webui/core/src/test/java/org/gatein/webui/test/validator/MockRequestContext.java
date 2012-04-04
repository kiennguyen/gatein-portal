/**
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.gatein.webui.test.validator;

import java.util.Locale;

import org.gatein.portal.mop.user.UserPortal;
import org.gatein.services.resources.Orientation;
import org.gatein.web.application.URLBuilder;
import org.gatein.web.url.PortalURL;
import org.gatein.web.url.ResourceType;
import org.gatein.web.url.URLFactory;
import org.gatein.webui.application.WebuiRequestContext;
import org.gatein.webui.core.UIComponent;

/**
 * @author <a href="mailto:haint@exoplatform.com">Nguyen Thanh Hai</a>
 *
 * @datSep 28, 2011
 */
public class MockRequestContext extends WebuiRequestContext
{
   private Locale locale;

   public MockRequestContext(Locale locale) 
   {
      super(null);
      this.locale = locale;
   }
   
   public Locale getLocale()
   {
      return locale;
   }

   @Override
   public URLBuilder<UIComponent> getURLBuilder()
   {
      return null;
   }

   @Override
   public String getRequestContextPath()
   {
      return null;
   }

   @Override
   public String getPortalContextPath()
   {
      return null;
   }

   @Override
   public <T> T getRequest() throws Exception
   {
      return null;
   }

   @Override
   public <T> T getResponse() throws Exception
   {
      return null;
   }

   @Override
   public void sendRedirect(String url) throws Exception
   {
      
   }

   @Override
   public URLFactory getURLFactory()
   {
      return null;
   }

   @Override
   public <R, U extends PortalURL<R, U>> U newURL(ResourceType<R, U> resourceType, URLFactory urlFactory)
   {
      return null;
   }

   @Override
   public Orientation getOrientation()
   {
      return null;
   }

   @Override
   public String getRequestParameter(String name)
   {
      return null;
   }

   @Override
   public String[] getRequestParameterValues(String name)
   {
      return null;
   }

   @Override
   public boolean useAjax()
   {
      return false;
   }

   @Override
   public UserPortal getUserPortal()
   {
      return null;
   }
}
