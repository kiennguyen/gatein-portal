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

package org.gatein.portal.application;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.SessionContainer;
import org.exoplatform.container.SessionManagerContainer;
import org.gatein.web.application.Application;
import org.gatein.web.application.ApplicationLifecycle;
import org.gatein.web.application.RequestFailure;
import org.gatein.webui.application.WebuiRequestContext;

public class PortalApplicationLifecycle implements ApplicationLifecycle<WebuiRequestContext>
{

   @SuppressWarnings("unused")
   public void onInit(Application app)
   {
   }

   @SuppressWarnings("unused")
   public void onStartRequest(Application app, WebuiRequestContext rcontext) throws Exception
   {
      ExoContainer pcontainer = ExoContainerContext.getCurrentContainer();
      SessionContainer.setInstance(((SessionManagerContainer)pcontainer).getSessionManager().getSessionContainer(
         rcontext.getSessionId()));
   }
   
   @SuppressWarnings("unused")
   public void onFailRequest(Application app, WebuiRequestContext rontext, RequestFailure failureType) throws Exception
   {
      
   }

   @SuppressWarnings("unused")
   public void onEndRequest(Application app, WebuiRequestContext rcontext) throws Exception
   {
      SessionContainer.setInstance(null);
      ExoContainerContext.setCurrentContainer(null);
   }

   @SuppressWarnings("unused")
   public void onDestroy(Application app)
   {
   }

}
