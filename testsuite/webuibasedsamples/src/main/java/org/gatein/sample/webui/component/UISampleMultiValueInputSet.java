package org.gatein.sample.webui.component;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.gatein.upload.UploadResource;
import org.gatein.web.application.ApplicationMessage;
import org.gatein.webui.application.WebuiRequestContext;
import org.gatein.webui.config.annotation.ComponentConfig;
import org.gatein.webui.config.annotation.EventConfig;
import org.gatein.webui.core.UIComponent;
import org.gatein.webui.core.lifecycle.UIFormLifecycle;
import org.gatein.webui.event.Event;
import org.gatein.webui.event.EventListener;
import org.gatein.webui.form.UIForm;
import org.gatein.webui.form.UIFormDateTimeInput;
import org.gatein.webui.form.UIFormInput;
import org.gatein.webui.form.UIFormInputBase;
import org.gatein.webui.form.UIFormMultiValueInputSet;
import org.gatein.webui.form.UIFormStringInput;
import org.gatein.webui.form.UIFormTextAreaInput;
import org.gatein.webui.form.UIFormUploadInput;
import org.gatein.webui.form.ext.UIFormColorPicker;
import org.gatein.webui.form.ext.UIFormColorPicker.Colors;
import org.gatein.webui.form.ext.UIFormColorPicker.Colors.Color;

@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIFormWithTitle.gtmpl", events = {@EventConfig(listeners = UISampleMultiValueInputSet.SubmitActionListener.class)})
public class UISampleMultiValueInputSet extends UIForm
{

   public static final String MULTI_UPLOAD = "UploadInput";

   public static final String MULTI_DATE = "DateTimeInput";

   public static final String MULTI_COLOR = "ColorInput";

   public static final String MULTI_STRING = "StringInput";

   public static final String MULTI_TEXTAREA = "TextAreaInput";

   public static final String[] ACTIONS = {"Submit"};

   public UISampleMultiValueInputSet() throws Exception
   {
      UIFormMultiValueInputSet uiFormMultiValueInputSet;
      
      // UIFormUploadInput
      addUIFormInput(makeMultiValueInputSet(MULTI_UPLOAD, UIFormUploadInput.class, new Class[] {String.class, String.class, int.class}));
      // UIFormDateTimeInput
      addUIFormInput(makeMultiValueInputSet(MULTI_DATE, UIFormDateTimeInput.class));

      // UIFormColorPicker
      uiFormMultiValueInputSet = new UIFormMultiValueInputSet(MULTI_COLOR, MULTI_COLOR);
      uiFormMultiValueInputSet.setType(UIFormColorPicker.class);
      uiFormMultiValueInputSet.setConstructorParameterTypes(new Class[] {String.class, String.class, String.class});
      uiFormMultiValueInputSet.setConstructorParameterValues(new Object[] {"ABC", "XYZ", null});
      addUIFormInput(uiFormMultiValueInputSet);

      // UIFormStringInput
      addUIFormInput(makeMultiValueInputSet(MULTI_STRING, UIFormStringInput.class));

      // UIFormTextAreaInput
      addUIFormInput(makeMultiValueInputSet(MULTI_TEXTAREA, UIFormTextAreaInput.class));

      setActions(ACTIONS);
   }

   @SuppressWarnings("unchecked")
   private UIFormInput makeMultiValueInputSet(String name, Class<? extends UIFormInputBase> type) throws Exception
   {
      UIFormMultiValueInputSet multiInput = new UIFormMultiValueInputSet(name, null);
      multiInput.setType(type);
      return multiInput;
   }
   
   private UIFormInput makeMultiValueInputSetHasValue(String name, Class<? extends UIFormInputBase> type, Object[] parameterValues) throws Exception
   {
      UIFormMultiValueInputSet multiInput = new UIFormMultiValueInputSet(name, null);
      multiInput.setType(type);
      multiInput.setConstructorParameterTypes(new Class[] {String.class, String.class, String.class});
      multiInput.setConstructorParameterValues(parameterValues);
      return multiInput;
   }
   
   private UIFormInput makeMultiValueInputSet(String name, Class<? extends UIFormInputBase> type, Class<?>... parameterTypes) throws Exception 
   {
      UIFormMultiValueInputSet multiInput = new UIFormMultiValueInputSet(name, null);
      multiInput.setType(type);
      multiInput.setConstructorParameterTypes(parameterTypes);
      return multiInput;
   }

   static public class SubmitActionListener extends EventListener<UISampleMultiValueInputSet>
   {
      @Override
      public void execute(Event<UISampleMultiValueInputSet> event) throws Exception
      {
         WebuiRequestContext rcontext = event.getRequestContext();
         rcontext.getUIApplication().addMessage(makeMsg(event.getSource()));
      }

      @SuppressWarnings("unchecked")
      private ApplicationMessage makeMsg(UISampleMultiValueInputSet uiForm)
      {
         StringBuilder msgBuild = new StringBuilder();

         for (UIComponent child : uiForm.getChildren())
         {
            UIFormMultiValueInputSet multiInput = (UIFormMultiValueInputSet)child;

            if (multiInput.getUIFormInputBase().equals(UIFormUploadInput.class))
            {
               makeUploadInputMsg(multiInput, msgBuild);
            }
            else if (multiInput.getUIFormInputBase().equals(UIFormDateTimeInput.class))
            {
               makeDateInputMsg(multiInput, msgBuild);
            }
            else
            {
               for (UIComponent multiInputChild : multiInput.getChildren())
               {
                  msgBuild.append(" " + ((UIFormInputBase)multiInputChild).getValue());
               }
            }
            msgBuild.append("<br/>");
         }

         return new ApplicationMessage(msgBuild.toString().replace(".", "*"), null);
      }

      private void makeDateInputMsg(UIFormMultiValueInputSet multiInput, StringBuilder msgBuild)
      {
         for (UIComponent multiInputChild : multiInput.getChildren())
         {
            UIFormDateTimeInput dateInput = (UIFormDateTimeInput)multiInputChild;
            Calendar calendar = dateInput.getCalendar();
            if (calendar != null)
            {
               SimpleDateFormat dateFormat = new SimpleDateFormat(dateInput.getDatePattern_());
               msgBuild.append(" " + dateFormat.format(dateInput.getCalendar().getTime()));
            }
            else
            {
               msgBuild.append("null");
            }
         }
      }

      private void makeUploadInputMsg(UIFormMultiValueInputSet multiInput, StringBuilder msgBuild)
      {
         for (UIComponent multiInputChild : multiInput.getChildren())
         {
            UploadResource uploadResource = ((UIFormUploadInput)multiInputChild).getUploadResource();
            if (uploadResource != null)
            {
               msgBuild.append(" " + uploadResource.getFileName());
            }
            else
            {
               msgBuild.append("null");
            }
         }
      }
   }
}
