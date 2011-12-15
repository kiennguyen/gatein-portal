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

package org.exoplatform.web.application.javascript;

import org.exoplatform.commons.cache.future.FutureMap;
import org.exoplatform.commons.cache.future.Loader;
import org.exoplatform.commons.utils.Safe;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.controller.resource.Resource;
import org.exoplatform.portal.controller.resource.ResourceScope;
import org.exoplatform.portal.resource.AbstractResourceService;
import org.exoplatform.portal.resource.MainResourceResolver;
import org.exoplatform.portal.resource.ResourceResolver;
import org.exoplatform.portal.resource.SkinService;
import org.exoplatform.portal.resource.compressor.ResourceCompressor;
import org.exoplatform.portal.resource.compressor.ResourceType;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.wci.WebAppListener;
import org.gatein.wci.impl.DefaultServletContainerFactory;
import org.picocontainer.Startable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.ServletContext;

public class JavascriptConfigService extends AbstractResourceService implements Startable
{
   /** Our logger. */
   private final Logger log = LoggerFactory.getLogger(JavascriptConfigService.class);

   /** The shared scripts. */
   private HashMap<String, Javascript.Composite> sharedJScripts;

   /** The portal scripts. */
   private HashMap<String, Javascript.Composite> portalJScripts;

   /** . */
   private long lastModified = Long.MAX_VALUE;

   /** . */
   private WebAppListener deployer;

   /** . */
   private CachedJavascript mergedCommonJScripts;

   /** . */
   private final FutureMap<String, CachedJavascript, ResourceResolver> cache;

   public JavascriptConfigService(ExoContainerContext context, ResourceCompressor compressor)
   {
      super(compressor);

      Loader<String, CachedJavascript, ResourceResolver> loader = new Loader<String, CachedJavascript, ResourceResolver>()
      {
         public CachedJavascript retrieve(ResourceResolver context, String key) throws Exception
         {
            org.exoplatform.portal.resource.Resource resource = context.resolve(key);
            if (resource == null)
            {
               return null;
            }

            StringBuilder sB = new StringBuilder();
            try
            {
               BufferedReader reader = new BufferedReader(resource.read());
               String line = reader.readLine();
               try
               {
                  while (line != null)
                  {
                     sB.append(line);
                     if ((line = reader.readLine()) != null)
                     {
                        sB.append("\n");
                     }
                  }
               }
               catch (Exception ex)
               {
                  ex.printStackTrace();
               }
               finally
               {
                  Safe.close(reader);
               }
            }
            catch (Exception e)
            {
               e.printStackTrace();
            }

            return new CachedJavascript(sB.toString());
         }
      };
      cache = new FutureMap<String, CachedJavascript, ResourceResolver>(loader);

      // todo : remove /portal ???
      HashMap<String, Javascript.Composite> tmp = new HashMap<String, Javascript.Composite>();
      tmp.put("common", new Javascript.Composite(new Resource(ResourceScope.SHARED, "common"), "/portal", 0));

      //
      sharedJScripts = tmp;
      deployer = new JavascriptConfigDeployer(context.getPortalContainerName(), this);
      portalJScripts = new HashMap<String, Javascript.Composite>();
   }

   /**
    * Return a collection list This method should return the availables scripts in the service
    *
    * @deprecated Somehow, it should use {@link #getCommonJScripts()} instead.
    * @return
    */
   @Deprecated
   public Collection<String> getAvailableScripts()
   {
      ArrayList<String> list = new ArrayList<String>();
      for (Javascript.Composite shared : sharedJScripts.values())
      {
         for (Javascript js : shared.compounds)
         {
            list.add(js.getModule());
         }
      }
      return list;
   }

   /**
    * Return a collection of all common JScripts
    *
    * @return
    */
   public Collection<Javascript> getCommonJScripts()
   {
      ArrayList<Javascript> list = new ArrayList<Javascript>();
      for (Javascript.Composite shared : sharedJScripts.values())
      {
         list.addAll(shared.compounds);
      }
      return list;
   }

   /**
    * Return a collection of all available JS paths
    *
    * @deprecated Somehow, it should use {@link #getCommonJScripts()} instead.
    *
    * @return A collection of all available JS paths
    */
   @Deprecated
   public Collection<String> getAvailableScriptsPaths()
   {
      ArrayList<String> list = new ArrayList<String>();
      for (Javascript.Composite shared : sharedJScripts.values())
      {
         for (Javascript js : shared.compounds)
         {
            list.add(js.getPath());
         }
      }
      return list;
   }

   /**
    * Add a JScript to {@link #commonJScripts} list and re-sort the list.
    * Then invalidate cache of all merged common JScripts to
    * ensure they will be newly merged next time.
    *
    * @param js
    */
   public void addCommonJScript(Javascript js)
   {
      Javascript.Composite common = sharedJScripts.get("common");
      common.compounds.add(js);
      Collections.sort(common.compounds, new Comparator<Javascript>()
      {
         public int compare(Javascript o1, Javascript o2)
         {
            return o1.getPriority() - o2.getPriority();
         }
      });
      invalidateMergedCommonJScripts();
   }

   /**
    * Remove a JScript for this module from {@link #commonJScripts}
    * and invalidates its cache correspondingly
    *
    * @param module
    */
   public void removeCommonJScript(String module)
   {
      Javascript.Composite common = sharedJScripts.get("common");
      Iterator<Javascript> iterator = common.compounds.iterator();
      while (iterator.hasNext())
      {
         Javascript js = iterator.next();
         if (js.getModule().equals(module))
         {
            iterator.remove();
            invalidateCachedJScript(js.getPath());
            invalidateMergedCommonJScripts();
         }
      }
   }

   /**
    * Return a collection of all PortalJScripts which belong to the specified portalName.
    *
    * @param portalName
    * @return list of JavaScript path which will be loaded by particular portal
    */
   public Collection<Javascript> getPortalJScripts(String portalName)
   {
      Javascript.Composite composite = portalJScripts.get(portalName);
      return composite != null ? Collections.unmodifiableCollection(composite.compounds) : null;
   }

   public InputStream open(Javascript.Internal script) throws IOException
   {
      // We don't implement caching or concurrent serving for now
      // we will do it later
      return script.open(this);
   }

   public Javascript getScript(Resource resource, String module)
   {
      Javascript.Composite script = getScript(resource);
      if (script != null && !"merged".equals(module))
      {
         return script.getCompound(module);
      }
      else
      {
         return script;
      }
   }

   public Javascript.Composite getScript(Resource resource)
   {
      switch (resource.getScope())
      {
         case SHARED:
            return sharedJScripts.get(resource.getId());
         case PORTAL:
            return portalJScripts.get(resource.getId());
      }
      
      //
      return null;
   }
   
   ServletContext getContext(String contextPath)
   {
      return contexts.get(contextPath);
   }

   public Collection<Javascript> getScripts(boolean merge)
   {
      return getScripts(null, merge);
   }

   public Collection<Javascript> getScripts(String portalName, boolean merge)
   {
      ArrayList<Javascript> scripts = new ArrayList<Javascript>();
      
      // First shared scripts
      Javascript.Composite common = sharedJScripts.get("common");
      if (merge)
      {
         scripts.add(common);
         for (Javascript script : common.compounds)
         {
            // For now we do it this way
            if (script instanceof Javascript.External)
            {
               scripts.add(script);
            }
         }
      }
      else
      {
         scripts.addAll(common.compounds);
      }
      
      // Then portal scripts
      if (portalName != null)
      {
         Javascript.Composite portalScript = portalJScripts.get(portalName);
         if (portalScript != null)
         {
            scripts.addAll(portalScript.compounds);
         }
      }
      
      //
      return scripts;
   }

   /**
    * Add a PortalJScript which will be loaded with a specific portal.
    * <p>
    * For now, we don't persist it inside the Portal site storage but just in memory.
    * Therefore we could somehow remove all PortalJScript for a Portal by using {@link #removePortalJScripts(String)}
    * when the portal is being removed.
    *
    * @param js
    */
   public void addPortalJScript(Javascript js)
   {
      Javascript.Composite list = portalJScripts.get(js.getResource().getId());
      if (list == null)
      {
         portalJScripts.put(js.getResource().getId(), list = new Javascript.Composite(js.getResource(), null, 0));
      }

      //
      list.compounds.add(js);
      Collections.sort(list.compounds, new Comparator<Javascript>()
      {
         public int compare(Javascript o1, Javascript o2)
         {
            return o1.getPriority() - o2.getPriority();
         }
      });
   }

   /**
    * Remove portal name from a JavaScript module or remove JavaScript module if it contains only one portal name
    *
    * @param portalName portal's name which you want to remove
    */
   public void removePortalJScripts(String portalName)
   {
      Javascript.Composite list = portalJScripts.remove(portalName);
      for (Javascript js : list.compounds)
      {
         invalidateCachedJScript(js.getPath());
      }
   }

   /**
    * unregister a {@link ServletContext} into {@link MainResourceResolver} of {@link SkinService}
    *
    * @param servletContext ServletContext will unregistered
    */
   public void unregisterServletContext(ServletContext servletContext)
   {
      super.unregisterServletContext(servletContext);
      remove(servletContext);
   }

   /**
    * Remove JavaScripts from availabe JavaScipts by ServletContext
    * @param context the webapp's {@link javax.servlet.ServletContext}
    *
    */
   public synchronized void remove(ServletContext context)
   {
      for (Javascript.Composite shared : sharedJScripts.values())
      {
         Iterator<Javascript> iterator = shared.compounds.iterator();
         while (iterator.hasNext())
         {
            Javascript js = iterator.next();
            if (js.getContextPath().equals(context.getContextPath()))
            {
               iterator.remove();
               invalidateCachedJScript(js.getPath());
               invalidateMergedCommonJScripts();
            }
         }
      }
   }

   /**
    *  @deprecated Use {@link #invalidateMergedCommonJScripts()} instead
    */
   @Deprecated
   public void refreshMergedJavascript()
   {
      invalidateMergedCommonJScripts();
   }

   /**
    * Write the merged javascript in a provided output stream.
    *
    * @param out the output stream
    * @throws IOException any io exception
    */
   @Deprecated
   public void writeMergedJavascript(OutputStream out) throws IOException
   {
      byte[] jsBytes = getMergedJavascript();

      //
      out.write(jsBytes);
   }

   public String getJScript(String path)
   {
      CachedJavascript cachedJScript = getCachedJScript(path);
      if (cachedJScript != null)
      {
         return cachedJScript.getText();
      }
      else
      {
         return null;
      }
   }

   /**
    * Return a CachedJavascript which is lazy loaded from a {@link FutureMap} cache
    *
    * @param path
    * @return
    */
   public CachedJavascript getCachedJScript(String path)
   {
      return cache.get(mainResolver, path);
   }

   /**
    * Returns a string which is merging of all common JScripts
    *
    * @return
    */
   public CachedJavascript getMergedCommonJScripts()
   {
      new Exception().printStackTrace();
      if (mergedCommonJScripts == null)
      {
         StringBuilder sB = new StringBuilder();
         for (Javascript js : sharedJScripts.get("common").compounds)
         {
            if (!js.isExternalScript())
            {
               String jScript = getJScript(js.getPath());
               if (jScript != null)
               {
                  sB.append(jScript).append("\n");
               }

            }
         }

         String jScript = sB.toString();

         try
         {
            jScript = compressor.compress(jScript, ResourceType.JAVASCRIPT);
         }
         catch (Exception e)
         {
         }

         mergedCommonJScripts = new CachedJavascript(jScript);
         lastModified = mergedCommonJScripts.getLastModified();
      }

      return mergedCommonJScripts;
   }

   /**
    * @deprecated Use {@link #getMergedCommonJScripts()} instead.
    * It is more clearly to see what exactly are included in the returned merging
    *
    * @return byte[]
    */
   @Deprecated
   public byte[] getMergedJavascript()
   {
      String mergedCommonJS = getMergedCommonJScripts().getText();

      return mergedCommonJS.getBytes();
   }

   /**
    * @deprecated the last modification should belong to CachedJavascript object
    * @return
    */
   @Deprecated
   public long getLastModified()
   {
      return lastModified;
   }

   /**
    * Check the existence of module in Available Scripts
    * @param module
    * @return true if Available Scripts contain module, else return false
    */
   public boolean isModuleLoaded(CharSequence module)
   {
      for (Javascript.Composite shared : sharedJScripts.values())
      {
         for (Javascript js : shared.compounds)
         {
            if (js.getModule().equals(module))
            {
               return true;
            }
         }
      }
      return false;
   }

   /**
    * Invalidate cache of merged common JScripts
    */
   public void invalidateMergedCommonJScripts()
   {
      mergedCommonJScripts = null;
   }

   /**
    * Invalidate cache for this <tt>path</tt>
    *
    * @param path
    */
   public void invalidateCachedJScript(String path)
   {
      cache.remove(path);
   }

   /**
    * Start service.
    * Registry org.exoplatform.web.application.javascript.JavascriptDeployer,
    * org.exoplatform.web.application.javascript.JavascriptRemoval  into ServletContainer
    * @see org.picocontainer.Startable#start()
    */
   public void start()
   {
      DefaultServletContainerFactory.getInstance().getServletContainer().addWebAppListener(deployer);
   }

   /**
    * Stop service.
    * Remove org.exoplatform.web.application.javascript.JavascriptDeployer,
    * org.exoplatform.web.application.javascript.JavascriptRemoval  from ServletContainer
    * @see org.picocontainer.Startable#stop()
    */
   public void stop()
   {
      DefaultServletContainerFactory.getInstance().getServletContainer().removeWebAppListener(deployer);
   }
}
