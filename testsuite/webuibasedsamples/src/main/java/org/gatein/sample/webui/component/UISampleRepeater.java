package org.gatein.sample.webui.component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.exoplatform.commons.utils.LazyPageList;
import org.gatein.commons.utils.ListAccessImpl;
import org.gatein.sample.webui.component.bean.User;
import org.gatein.web.application.ApplicationMessage;
import org.gatein.webui.application.WebuiRequestContext;
import org.gatein.webui.config.annotation.ComponentConfig;
import org.gatein.webui.config.annotation.EventConfig;
import org.gatein.webui.core.UIContainer;
import org.gatein.webui.core.UIGrid;
import org.gatein.webui.core.UIPageIterator;
import org.gatein.webui.core.lifecycle.UIContainerLifecycle;
import org.gatein.webui.event.Event;
import org.gatein.webui.event.EventListener;

@ComponentConfig(lifecycle = UIContainerLifecycle.class, events = {
   @EventConfig(listeners = UISampleRepeater.ViewActionListener.class),
   @EventConfig(listeners = UISampleRepeater.EditActionListener.class),
   @EventConfig(listeners = UISampleRepeater.DeleteActionListener.class)})
public class UISampleRepeater extends UIContainer
{
   public static final String BEAN_ID = "userName";

   public static final String[] BEAN_NAMES = {BEAN_ID, "favoriteColor", "position", "dateOfBirth"};

   public static final String[] ACTIONS = {"View", "Edit", "Delete"};

   public UISampleRepeater() throws Exception
   {
      UIGrid uiRepeater = addChild(UIGrid.class, null, null);
      uiRepeater.configure(BEAN_ID, BEAN_NAMES, ACTIONS);
      
      UIPageIterator pageIterator = uiRepeater.getUIPageIterator();
      pageIterator.setPageList(makeDataSource());      
      pageIterator.setParent(this);
   }
   
   public void showPopupMessage(String msg) {
      WebuiRequestContext rcontext = WebuiRequestContext.getCurrentInstance();
      rcontext.getUIApplication().addMessage(new ApplicationMessage(msg, null));   
   }

   private LazyPageList<User> makeDataSource()
   {
      List<User> userList = makeUserList();
      return new LazyPageList<User>(new ListAccessImpl<User>(User.class, userList), 5);
   }

   private List<User> makeUserList()
   {
      List<User> userList = new ArrayList<User>();           
      for (int i = 0; i < 30; i++) {
         userList.add(new User("user " + i, "color " + i, "position " + i, new Date()));
      }
      return userList;
   }

   static public class ViewActionListener extends EventListener<UISampleRepeater>
   {
      @Override
      public void execute(Event<UISampleRepeater> event) throws Exception
      {
         event.getSource().showPopupMessage("View " + event.getRequestContext().getRequestParameter(OBJECTID));       
      }
   }

   static public class EditActionListener extends EventListener<UISampleRepeater>
   {
      @Override
      public void execute(Event<UISampleRepeater> event) throws Exception
      {
         event.getSource().showPopupMessage("Edit " + event.getRequestContext().getRequestParameter(OBJECTID));           
      }
   }

   static public class DeleteActionListener extends EventListener<UISampleRepeater>
   {
      @Override
      public void execute(Event<UISampleRepeater> event) throws Exception
      {
         event.getSource().showPopupMessage("Delete " + event.getRequestContext().getRequestParameter(OBJECTID));  
      }
   }   
}
