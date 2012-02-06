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

import org.apache.shindig.common.xml.DomUtil;
import org.apache.shindig.gadgets.Gadget;
import org.apache.shindig.gadgets.http.HttpResponse;
import org.apache.shindig.gadgets.rewrite.GadgetRewriter;
import org.apache.shindig.gadgets.rewrite.MutableContent;
import org.apache.shindig.gadgets.rewrite.RewritingException;
import org.apache.shindig.gadgets.spec.Feature;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Collection;

/**
 * @author <a href="kienna@exoplatform.com">Kien Nguyen</a>
 * @version $Revision$
 */
public class GateInResourcesRewriter implements GadgetRewriter
{
   private static String GATEIN_RESOURCES_FEATURE = "gatein-resources";

   private static String FEATURE_ID = "id";

   public void rewrite(Gadget gadget, MutableContent content) throws RewritingException
   {
      if (gadget.getAllFeatures().contains(GATEIN_RESOURCES_FEATURE))
      {
         Feature grFeature = gadget.getSpec().getModulePrefs().getFeatures().get(GATEIN_RESOURCES_FEATURE);
         Collection<String> resourceIds = grFeature.getParamCollection(FEATURE_ID);

         if (resourceIds.size() > 0)
         {
            Document doc = content.getDocument();
            Element head = (Element)DomUtil.getFirstNamedChildNode(doc.getDocumentElement(), "head");
            Element script = head.getOwnerDocument().createElement("script");

            //TODO should use resource controller for finding resources URL
            //Temporary hardcode for testing jquery resource
            if (resourceIds.contains("jquery"))
            {
               String resourceUrl = "http://localhost:8080/eXoResources/javascript/jquery.js";
               script.setAttribute("src", resourceUrl);
            }

            head.appendChild(script);
         }
         else
         {
            throw new RewritingException(GATEIN_RESOURCES_FEATURE + " required Param: " + FEATURE_ID,
               HttpResponse.SC_INTERNAL_SERVER_ERROR);
         }
      }

   }
}
