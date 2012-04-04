package org.gatein.web.controller.performance;

import junit.framework.TestCase;

import org.gatein.web.controller.QualifiedName;
import org.gatein.web.controller.metadata.ControllerDescriptor;
import org.gatein.web.controller.metadata.DescriptorBuilder;
import org.gatein.web.controller.router.RegexFactory;
import org.gatein.web.controller.router.RenderContext;
import org.gatein.web.controller.router.Router;
import org.gatein.web.controller.router.URIWriter;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Standalone extends TestCase
{

   /** . */
   private static final QualifiedName HANDLER = QualifiedName.parse("gtn:handler");

   /** . */
   private static final QualifiedName SITETYPE = QualifiedName.parse("gtn:sitetype");

   /** . */
   private static final QualifiedName SITENAME = QualifiedName.parse("gtn:sitename");

   /** . */
   private static final QualifiedName PATH = QualifiedName.parse("gtn:path");

   public void testFoo() throws Exception
   {

      URL url = ControllerRendererDriver.class.getResource("controller.xml");
      DescriptorBuilder builder = new DescriptorBuilder();
      ControllerDescriptor descriptor = builder.build(url.openStream());
      Router router = descriptor.build(RegexFactory.JAVA);

      //
      Map<QualifiedName, String> map = new HashMap<QualifiedName, String>();
      map.put(HANDLER, "portal");
      map.put(SITETYPE, "portal");
      map.put(SITENAME, "classic");
      map.put(PATH, "page");

      //
      RenderContext context = new RenderContext(map);

      //
      URIWriter writer = new URIWriter(NullAppendable.INSTANCE);

      //
      String s = router.render(map);
      assertNotNull(s);

      //
      while (true)
      {
         writer.reset(NullAppendable.INSTANCE);
         router.render(context, writer);
      }
   }
}
