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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.shindig.common.xml.DomUtil;
import org.apache.shindig.gadgets.Gadget;
import org.apache.shindig.gadgets.http.HttpResponse;
import org.apache.shindig.gadgets.rewrite.GadgetRewriter;
import org.apache.shindig.gadgets.rewrite.MutableContent;
import org.apache.shindig.gadgets.rewrite.RewritingException;
import org.apache.shindig.gadgets.spec.Feature;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.WebAppController;
import org.exoplatform.web.application.javascript.JavascriptConfigService;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.portal.controller.resource.ResourceId;
import org.gatein.portal.controller.resource.ResourceScope;
import org.gatein.portal.controller.resource.script.FetchMode;
import org.gatein.portal.controller.resource.script.ScriptResource;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Look up GateIn resource with given Id and inject resource URL into html
 * header of gadget's content
 * 
 * @author <a href="kienna@exoplatform.com">Kien Nguyen</a>
 * @version $Revision$
 */
public class GateInResourcesRewriter implements GadgetRewriter
{
   private static String GATEIN_RESOURCES_FEATURE = "gatein-resources";

   public final static String SCRIPTS = "scripts";

   private static String AMD = "amd";

   private JavascriptConfigService service;

   /** . */
   final Logger log = LoggerFactory.getLogger(GateInResourcesRewriter.class);

   private ResourceId reqJS = new ResourceId(ResourceScope.SHARED, "bootstrap");

   public GateInResourcesRewriter()
   {
      PortalContainer pcontainer = PortalContainer.getInstance();
      service = (JavascriptConfigService)pcontainer.getComponentInstanceOfType(JavascriptConfigService.class);
   }

   public void rewrite(Gadget gadget, MutableContent content) throws RewritingException
   {
      if (gadget.getAllFeatures().contains(GATEIN_RESOURCES_FEATURE))
      {
         Feature grFeature = gadget.getSpec().getModulePrefs().getFeatures().get(GATEIN_RESOURCES_FEATURE);
         JSONObject reqConfig = getJSConfig();
         
         Document doc = content.getDocument();
         Element head = (Element)DomUtil.getFirstNamedChildNode(doc.getDocumentElement(), "head");
         Element body = (Element)DomUtil.getFirstNamedChildNode(doc.getDocumentElement(), "body");
         
         Set<ResourceId> modules = resolveIds(grFeature.getParam(AMD), FetchMode.ON_LOAD);
         if (modules != null)
         {
            // Add RequireJS lib
            head.appendChild(createScript(null, "var require=" + reqConfig, doc));
            head.appendChild(createScript(getPath(reqJS, reqConfig), null, doc));

            // Add modules
            if (modules.size() > 0)
            {
               StringBuilder loadBuilder = new StringBuilder("require(");
               loadBuilder.append(new JSONArray(modules));
               loadBuilder.append(");");
               body.appendChild(createScript(null, loadBuilder.toString(), doc));
            }
         }

         // Add immediate scripts configured in Gatein resource feature
         Set<ResourceId> scripts = resolveIds(grFeature.getParam(SCRIPTS), FetchMode.IMMEDIATE);
         if (scripts != null)
         {
            scripts.remove(reqJS);
            for (ResourceId script : scripts)
            {
               head.appendChild(createScript(getPath(script, reqConfig), null, doc));
            }
         }
      }
   }

   private JSONObject getJSConfig() throws RewritingException
   {
      PortalContainer pcontainer = PortalContainer.getInstance();
      WebAppController webAppController =
         (WebAppController)pcontainer.getComponentInstanceOfType(WebAppController.class);
      ControllerContext context = new ControllerContext(webAppController.getRouter(), null);
      Locale en = new Locale("en");
      try
      {
         return service.getJSConfig(context, en);
      }
      catch (Exception e)
      {
         throw new RewritingException(e, HttpResponse.SC_INTERNAL_SERVER_ERROR);
      }
   }

   private Set<ResourceId> resolveIds(String resourceParams, FetchMode mode)
   {
      if (resourceParams != null)
      {
         Map<ResourceId, FetchMode> ids = new HashMap<ResourceId, FetchMode>();
         for (String module : resourceParams.split(","))
         {
            module = module.trim();
            ids.put(new ResourceId(ResourceScope.SHARED, module), mode);
         }
         Set<ResourceId> result = new HashSet<ResourceId>();
         for (ScriptResource res : service.resolveIds(ids).keySet())
         {
            result.add(res.getId());
         }
         return result;
      }
      else
      {
         return null;
      }
   }

   private Element createScript(String url, String content, Document doc)
   {
      Element script = doc.createElement("script");
      if (url != null)
      {
         script.setAttribute("src", url);
      }
      else
      {
         script.setTextContent(content);
      }
      return script;
   }

   private String getPath(ResourceId id, JSONObject reqConfig) throws RewritingException
   {
      try
      {
         JSONObject paths = reqConfig.getJSONObject("paths");
         return paths.getString(id.toString()) + ".js";
      }
      catch (JSONException ex)
      {
         throw new RewritingException(ex, HttpResponse.SC_INTERNAL_SERVER_ERROR);
      }
   }
}