package org.gatein.portal.gadget.core;

import org.apache.shindig.gadgets.oauth.OAuthStore.TokenInfo;
import org.gatein.wci.security.Credentials;
import org.gatein.web.security.Token;


public class GadgetToken extends TokenInfo implements Token
{
   public GadgetToken(String accessToken, String tokenSecret, String sessionHandle,
      long tokenExpireMillis)
   {
      super(accessToken, tokenSecret, sessionHandle, tokenExpireMillis);
   }

   public boolean isExpired()
   {
      return false;
   }

   public long getExpirationTimeMillis()
   {
      return getTokenExpireMillis();
   }

   public Credentials getPayload()
   {
      // Should we return something ?
      return null;
   }
}
