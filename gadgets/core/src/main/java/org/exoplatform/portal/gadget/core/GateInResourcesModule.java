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

import org.apache.shindig.gadgets.rewrite.GadgetRewriter;

import com.google.inject.AbstractModule;

import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

/**
 * @author <a href="kienna@exoplatform.com">Kien Nguyen</a>
 * @version $Revision$
 */
public class GateInResourcesModule extends AbstractModule
{
   @Override
   protected void configure()
   {
      configureGateInFeatures();
      configureGateInFeaturesRewriter();
   }
   
   /**
    * Adds the gatein-features directory to the FeatureRegistry
    */
   protected void configureGateInFeatures() {
     Multibinder<String> featureBinder = Multibinder.newSetBinder(binder(), String.class, Names.named("org.apache.shindig.features-extended")); 
     featureBinder.addBinding().toInstance("res://gatein-features/features.txt");
   }
   
   /**
    * Adds the rewriter to process gatein resources
    */
   private void configureGateInFeaturesRewriter()
   {
      Multibinder<GadgetRewriter> rewriterbinder = Multibinder.newSetBinder(binder(), GadgetRewriter.class, Names.named("shindig.rewriters.gadget"));       
      rewriterbinder.addBinding().to(GateInResourcesRewriter.class);
   }
}
