package org.gatein.portal.mop.management.operations.page;

import org.gatein.portal.mop.SiteKey;
import org.gatein.portal.pom.config.Utils;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class PageKey
{
   private final SiteKey siteKey;
   private final String pageName;
   private final String pageId;

   public PageKey(SiteKey siteKey, String pageName)
   {
      this.siteKey = siteKey;
      this.pageName = pageName;
      this.pageId = Utils.join("::", siteKey.getTypeName(), siteKey.getName(), pageName);
   }

   public SiteKey getSiteKey()
   {
      return siteKey;
   }

   public String getPageName()
   {
      return pageName;
   }

   public String getPageId()
   {
      return pageId;
   }

   @Override
   public String toString()
   {
      return "PageKey{siteKey=" + siteKey + ", pageName='" + pageName + "'}";
   }
}
