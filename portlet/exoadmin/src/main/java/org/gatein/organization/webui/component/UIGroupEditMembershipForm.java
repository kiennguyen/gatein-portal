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

package org.gatein.organization.webui.component;

import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.gatein.commons.serialization.api.annotations.Serialized;
import org.gatein.web.application.ApplicationMessage;
import org.gatein.webui.config.annotation.ComponentConfig;
import org.gatein.webui.config.annotation.EventConfig;
import org.gatein.webui.core.UIApplication;
import org.gatein.webui.core.UIPopupWindow;
import org.gatein.webui.core.lifecycle.UIFormLifecycle;
import org.gatein.webui.core.model.SelectItemOption;
import org.gatein.webui.event.Event;
import org.gatein.webui.event.EventListener;
import org.gatein.webui.form.UIForm;
import org.gatein.webui.form.UIFormSelectBox;
import org.gatein.webui.form.UIFormStringInput;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Dec 2, 2008          
 */

@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIForm.gtmpl", events = {
   @EventConfig(listeners = UIGroupEditMembershipForm.SaveActionListener.class),
   @EventConfig(listeners = UIGroupEditMembershipForm.CancelActionListener.class)})
@Serialized
public class UIGroupEditMembershipForm extends UIForm
{

   private List<SelectItemOption<String>> listOption = new ArrayList<SelectItemOption<String>>();

   private final static String USER_NAME = "username";

   private final static String MEMBER_SHIP = "membership";

   private String membershipId;

   private String groupId;

   public UIGroupEditMembershipForm() throws Exception
   {
      addUIFormInput(new UIFormStringInput(USER_NAME, USER_NAME, null).setReadOnly(true));
      addUIFormInput(new UIFormSelectBox(MEMBER_SHIP, MEMBER_SHIP, listOption).setSize(1));
   }

   public void setValue(Membership memberShip, Group selectedGroup) throws Exception
   {
      this.membershipId = memberShip.getId();
      this.groupId = selectedGroup.getId();
      getUIStringInput(USER_NAME).setValue(memberShip.getUserName());
      OrganizationService service = getApplicationComponent(OrganizationService.class);
      List<?> collection = (List<?>)service.getMembershipTypeHandler().findMembershipTypes();
      for (Object ele : collection)
      {
         MembershipType mt = (MembershipType)ele;
         SelectItemOption<String> option =
            new SelectItemOption<String>(mt.getName(), mt.getName(), mt.getDescription());
         if (mt.getName().equals(memberShip.getMembershipType()))
            option.setSelected(true);
         listOption.add(option);
      }
   }

   static public class SaveActionListener extends EventListener<UIGroupEditMembershipForm>
   {
      public void execute(Event<UIGroupEditMembershipForm> event) throws Exception
      {
         UIGroupEditMembershipForm uiForm = event.getSource();
         UIApplication uiApp = event.getRequestContext().getUIApplication();
         UIPopupWindow uiPopup = uiForm.getParent();
         OrganizationService service = uiForm.getApplicationComponent(OrganizationService.class);
         
         Membership formMembership =  service.getMembershipHandler().findMembership(uiForm.membershipId);
         if (formMembership == null)
         {
            uiApp.addMessage(new ApplicationMessage("UIGroupEditMembershipForm.msg.membership-delete", null));
            uiPopup.setUIComponent(null);
            uiPopup.setShow(false);
            return;
         }
         String userName = formMembership.getUserName();
         Group group = service.getGroupHandler().findGroupById(uiForm.groupId);
         User user = service.getUserHandler().findUserByName(userName);
         MembershipHandler memberShipHandler = service.getMembershipHandler();
         String memberShipTypeStr = uiForm.getUIFormSelectBox(MEMBER_SHIP).getValue();
         MembershipType membershipType = service.getMembershipTypeHandler().findMembershipType(memberShipTypeStr);
         Membership membership =
            memberShipHandler.findMembershipByUserGroupAndType(userName, group.getId(), membershipType.getName());
         if (membership != null)
         {
            uiApp.addMessage(new ApplicationMessage("UIGroupEditMembershipForm.msg.membership-exist", null));
            return;
         }
         memberShipHandler.removeMembership(uiForm.membershipId, true);
         memberShipHandler.linkMembership(user, group, membershipType, true);

         uiPopup.setUIComponent(null);
         uiPopup.setShow(false);
      }
   }

   static public class CancelActionListener extends EventListener<UIGroupEditMembershipForm>
   {
      public void execute(Event<UIGroupEditMembershipForm> event) throws Exception
      {
         UIGroupEditMembershipForm uiForm = event.getSource();
         UIPopupWindow uiPopup = uiForm.getParent();
         uiPopup.setUIComponent(null);
         uiPopup.setShow(false);
      }
   }
}
