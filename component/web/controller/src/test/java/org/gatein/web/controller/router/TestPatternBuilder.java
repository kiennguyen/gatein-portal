/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.gatein.web.controller.router;

import org.gatein.web.controller.router.PatternBuilder;
import org.gatein.web.controller.router.Regex;
import org.gatein.web.controller.router.RegexFactory;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestPatternBuilder extends TestCase
{

   public void testEscapeReservedChar() throws Exception
   {
      assertLiteral('^');
      assertLiteral('*');
      assertLiteral('$');
      assertLiteral('[');
      assertLiteral(']');
      assertLiteral('.');
      assertLiteral('|');
      assertLiteral('+');
      assertLiteral('(');
      assertLiteral(')');
      assertLiteral('?');
   }

   private void assertLiteral(char c)
   {
      PatternBuilder pb = new PatternBuilder();
      pb.expr("^");
      pb.literal(c);
      pb.expr("$");
      Regex pattern = RegexFactory.JAVA.compile(pb.build());
      assertTrue(pattern.matcher().matches(Character.toString(c)));
   }
}
