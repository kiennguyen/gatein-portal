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

import com.google.inject.CreationException;
import com.google.inject.name.Names;
import com.google.inject.spi.Message;

import org.apache.commons.io.IOUtils;
import org.apache.shindig.common.util.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

import com.google.inject.AbstractModule;

/**
 * @author <a href="kienna@exoplatform.com">Kien Nguyen</a>
 * @version $Revision$
 */
public class ExoPropertiesModule extends AbstractModule
{
   private final static String DEFAULT_PROPERTIES = "containers/shindig.properties";
   
   private final Properties properties;

   public ExoPropertiesModule()
   {
      super();
      this.properties = readPropertyFile(getPropertiesFilePath());
   }

   public ExoPropertiesModule(String propertyFile)
   {
      this.properties = readPropertyFile(propertyFile);
   }

   public ExoPropertiesModule(Properties properties)
   {
      this.properties = properties;
   }

   @Override
   protected void configure()
   {
      Names.bindProperties(this.binder(), getProperties());
      // This could be generalized to inject any system property...
      this.binder().bindConstant().annotatedWith(Names.named("shindig.port")).to(getServerPort());
      this.binder().bindConstant().annotatedWith(Names.named("shindig.host")).to(getServerHostname());
   }

   /**
    * Should return the port that the current server is running on.  Useful for testing and working out of the box configs.
    * Looks for a port in system properties "shindig.port" then "jetty.port", if not set uses fixed value of "8080"
    * @return an integer port number as a string.
    */
   protected String getServerPort()
   {
      return System.getProperty("shindig.port") != null ? System.getProperty("shindig.port") : System
         .getProperty("jetty.port") != null ? System.getProperty("jetty.port") : "8080";
   }

   /*
    * Should return the hostname that the current server is running on.  Useful for testing and working out of the box configs.
    * Looks for a hostname in system properties "shindig.host", if not set uses fixed value of "localhost"
    * @return a hostname
    */

   protected String getServerHostname()
   {
      return System.getProperty("shindig.host") != null ? System.getProperty("shindig.host") : System
         .getProperty("jetty.host") != null ? System.getProperty("jetty.host") : "localhost";
   }

   protected static String getPropertiesFilePath()
   {
      return DEFAULT_PROPERTIES;
   }

   protected Properties getProperties()
   {
      return properties;
   }

   private Properties readPropertyFile(String propertyFile)
   {
      Properties properties = new Properties();
      InputStream is = null;
      try
      {
         GateInContainerConfigLoader currentLoader = GateInGuiceServletContextListener.getCurrentLoader();
         is = currentLoader.loadResourceAsStream(propertyFile);
         
         if (is == null)
         {
            is = ResourceLoader.openResource(propertyFile);
         }
         
         properties.load(is);
      }
      catch (IOException e)
      {
         throw new CreationException(Arrays.asList(new Message("Unable to load properties: " + propertyFile)));
      }
      finally
      {
         IOUtils.closeQuietly(is);
      }

      return properties;
   }

}
