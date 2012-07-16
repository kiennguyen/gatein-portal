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
import java.util.HashMap;
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
import org.gatein.portal.controller.resource.script.ScriptResource;
import org.json.JSONArray;
import org.json.JSONObject;
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
            
            Map<ResourceId, FetchMode> ids = new HashMap<ResourceId, FetchMode>();
            for (String id : resourceIds)
            {
               ids.put(new ResourceId(ResourceScope.SHARED, id), null);
            }         
            Map<ScriptResource, FetchMode> resolvedIds = service.resolveIds(ids);
            
            LinkedList<ResourceId> immediate = new LinkedList<ResourceId>();
            List<ResourceId> on_load = new LinkedList<ResourceId>();
            for (ScriptResource sc : resolvedIds.keySet())
            {
               if (FetchMode.IMMEDIATE.equals(sc.getFetchMode())) 
               {
                  immediate.add(sc.getId());
               }
               else
               {
                  on_load.add(sc.getId());
               }
            }                     
            
            ResourceId bootstrapID = new ResourceId(ResourceScope.SHARED, "bootstrap");
            //Only add bootstrap when there are on_load resources
            immediate.remove(bootstrapID);
                                    
            try
            {                                     
               WebAppController webAppController = (WebAppController)pcontainer.getComponentInstanceOfType(
                  WebAppController.class);
               ControllerContext context = new ControllerContext(webAppController.getRouter(), null);
               Locale en = new Locale("en");
               
               Document doc = content.getDocument();                  
               if (on_load.size() > 0) 
               {
                  JSONObject reqConfig = service.getJSConfig(context, en);
                  appendScriptToHead(null, "var require=" + reqConfig, doc);
                  
                  JSONObject paths = reqConfig.getJSONObject("paths");
                  immediate.addFirst(bootstrapID);
                  for (ResourceId id : immediate)
                  {
                     appendScriptToHead(paths.getString(id.toString()) + ".js", null, doc);
                  }
                  
                  StringBuilder loadBuilder = new StringBuilder("require(");
                  loadBuilder.append(new JSONArray(on_load));
                  loadBuilder.append(")");
                  appendScriptToHead(null, loadBuilder.toString(), doc);                     
               }
               else
               {
                  for (ResourceId id : immediate) 
                  {
                     FetchMap<ResourceId> tmp = new FetchMap<ResourceId>();
                     tmp.put(id, null);                  
                     Map<String, FetchMode> immediateURL =
                              service.resolveURLs(context, tmp, !PropertyManager.isDevelopping(), en);
                     
                     appendScriptToHead(immediateURL.keySet().iterator().next(), null, doc);                     
                  }                  
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