/**
 * Copyright (C) 2012 eXo Platform SAS.
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
package org.exoplatform.portal.gadget.core;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.shindig.common.xml.DomUtil;
import org.apache.shindig.gadgets.Gadget;
import org.apache.shindig.gadgets.http.HttpResponse;
import org.apache.shindig.gadgets.rewrite.GadgetRewriter;
import org.apache.shindig.gadgets.rewrite.MutableContent;
import org.apache.shindig.gadgets.rewrite.RewritingException;
import org.apache.shindig.gadgets.spec.Feature;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.WebAppController;
import org.exoplatform.web.application.javascript.JavascriptConfigService;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.portal.controller.resource.ResourceId;
import org.gatein.portal.controller.resource.ResourceScope;
import org.gatein.portal.controller.resource.script.FetchMap;
import org.gatein.portal.controller.resource.script.FetchMode;
import org.json.JSONArray;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Look up GateIn resource with given Id and inject resource URL into html header of gadget's content
 * 
 * @author <a href="kienna@exoplatform.com">Kien Nguyen</a>
 * @version $Revision$
 */
public class GateInResourcesRewriter implements GadgetRewriter
{
   private static String GATEIN_RESOURCES_FEATURE = "gatein-resources";

   private static String RESOURCE_ID = "resource-id";
   
   /** . */
   final Logger log = LoggerFactory.getLogger(GateInResourcesRewriter.class);

   public void rewrite(Gadget gadget, MutableContent content) throws RewritingException
   {
      if (gadget.getAllFeatures().contains(GATEIN_RESOURCES_FEATURE))
      {
         Feature grFeature = gadget.getSpec().getModulePrefs().getFeatures().get(GATEIN_RESOURCES_FEATURE);
         Collection<String> resourceIds = grFeature.getParamCollection(RESOURCE_ID);

         if (resourceIds.size() > 0)
         {
            PortalContainer pcontainer = PortalContainer.getInstance();
            JavascriptConfigService service =
               (JavascriptConfigService)pcontainer.getComponentInstanceOfType(
                  JavascriptConfigService.class);            
            
            List<ResourceId> ids = new LinkedList<ResourceId>();            
            for (String id : resourceIds)
            {
               ids.add(new ResourceId(ResourceScope.SHARED, id));
            }           

            //We need to add bootstrap separately to others
            ResourceId bootstrapID = new ResourceId(ResourceScope.SHARED, "bootstrap"); 
            ids.remove(bootstrapID);
                                    
            try
            {                                     
               if (ids.size() > 0)
               {
                  WebAppController webAppController = (WebAppController)pcontainer.getComponentInstanceOfType(
                     WebAppController.class);
                  ControllerContext context = new ControllerContext(webAppController.getRouter(), null);
                  Locale en = new Locale("en");
                  
                  FetchMap<ResourceId> tmp = new FetchMap<ResourceId>();
                  tmp.put(bootstrapID, null);                  
                  Map<String, FetchMode> bootstrapURL =
                           service.resolveURLs(context, tmp, !PropertyManager.isDevelopping(), en);
                  
                  //Add bootstrap
                  Document doc = content.getDocument();                  
                  appendScriptToHead(null, "var require=" + service.getJSConfig(context, en), doc);
                  appendScriptToHead(bootstrapURL.keySet().iterator().next(), null, doc);
                  
                  //Add others
                  StringBuilder loadBuilder = new StringBuilder("require(");
                  loadBuilder.append(new JSONArray(ids));
                  loadBuilder.append(")");
                  appendScriptToHead(null, loadBuilder.toString(), doc);
               }
            }
            catch (Exception e) 
            {
               throw new RewritingException("Error",
                  HttpResponse.SC_INTERNAL_SERVER_ERROR);
            }
         }
         else
         {
            log.warn("There is no GateIn resources configured in the gadget " + gadget.getSpec().getUrl());
         }
      }
   }

   private void appendScriptToHead(String url, String content, Document doc)
   {
      if (url == null && content == null)
      {
         return;
      }
      Element head = (Element)DomUtil.getFirstNamedChildNode(doc.getDocumentElement(), "head");
      Element script = doc.createElement("script");
      if (url != null)
      {
         script.setAttribute("src", url);
      }
      else
      {
         script.setTextContent(content);
      }
      head.appendChild(script);
   }
}