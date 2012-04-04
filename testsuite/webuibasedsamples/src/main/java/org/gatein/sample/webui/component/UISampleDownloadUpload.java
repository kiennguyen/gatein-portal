package org.gatein.sample.webui.component;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gatein.download.DownloadResource;
import org.gatein.download.DownloadService;
import org.gatein.download.InputStreamDownloadResource;
import org.gatein.upload.UploadResource;
import org.gatein.upload.UploadService.UploadUnit;
import org.gatein.webui.config.annotation.ComponentConfig;
import org.gatein.webui.config.annotation.EventConfig;
import org.gatein.webui.core.lifecycle.UIFormLifecycle;
import org.gatein.webui.event.Event;
import org.gatein.webui.event.EventListener;
import org.gatein.webui.form.UIForm;
import org.gatein.webui.form.UIFormUploadInput;
import org.gatein.webui.form.input.UIUploadInput;

@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "app:/groovy/webui/component/UISampleDownloadUpload.gtmpl", events = {@EventConfig(listeners = UISampleDownloadUpload.SubmitActionListener.class)})
public class UISampleDownloadUpload extends UIForm
{

   Map<String, String> data = new HashMap<String, String>();

   private String[] downloadLink;

   private String[] fileName;

   private String[] inputName;

   public UISampleDownloadUpload() throws Exception
   {
      addUIFormInput(new UIFormUploadInput("name0", "value0"));
      addUIFormInput(new UIFormUploadInput("name1", "value1", 1));
      addUIFormInput(new UIFormUploadInput("name2", "value2", 200));
      
      addUIFormInput(new UIUploadInput("name3", "name3", 2, 300, UploadUnit.KB));
      UIUploadInput input = new UIUploadInput("name4", "name4", 2, 300);
      input.setAutoUpload(false);
      addUIFormInput(input);
   }

   public void setDownloadLink(String[] downloadLink)
   {
      this.downloadLink = downloadLink;
   }

   public String[] getDownloadLink()
   {
      return downloadLink;
   }

   public void setFileName(String[] fileName)
   {
      this.fileName = fileName;
   }

   public String[] getFileName()
   {
      return fileName;
   }

   public void setInputName(String[] inputName)
   {
      this.inputName = inputName;
   }

   public String[] getInputName()
   {
      return inputName;
   }

   static public class SubmitActionListener extends EventListener<UISampleDownloadUpload>
   {

      public void execute(Event<UISampleDownloadUpload> event) throws Exception
      {
         UISampleDownloadUpload uiForm = event.getSource();
         DownloadService dservice = uiForm.getApplicationComponent(DownloadService.class);
         List<String> downloadLink = new ArrayList<String>();
         List<String> fileName = new ArrayList<String>();
         List<String> inputName = new ArrayList<String>();
         for (int index = 0; index <= 2; index++)
         {
            UIFormUploadInput input = uiForm.getChildById("name" + index);
            UploadResource uploadResource = input.getUploadResource();
            if (uploadResource != null)
            {
               DownloadResource dresource =
                  new InputStreamDownloadResource(input.getUploadDataAsStream(), uploadResource.getMimeType());
               dresource.setDownloadName(uploadResource.getFileName());
               downloadLink.add(dservice.getDownloadLink(dservice.addDownloadResource(dresource)));
               fileName.add(uploadResource.getFileName());
               inputName.add("name" + index);
            }
         }

         for(int index = 3; index < 5; index++) {
            UIUploadInput input = uiForm.getChildById("name" + index);
            UploadResource[] uploadResources = input.getUploadResources();
            for(UploadResource uploadResource : uploadResources) {
            DownloadResource dresource =
            new InputStreamDownloadResource(new FileInputStream(new File(uploadResource.getStoreLocation())), uploadResource.getMimeType());
            dresource.setDownloadName(uploadResource.getFileName());
            downloadLink.add(dservice.getDownloadLink(dservice.addDownloadResource(dresource)));
            fileName.add(uploadResource.getFileName());
            inputName.add("name" + index);
            }
        }
         
         uiForm.setDownloadLink(downloadLink.toArray(new String[downloadLink.size()]));
         uiForm.setFileName(fileName.toArray(new String[fileName.size()]));
         uiForm.setInputName(inputName.toArray(new String[inputName.size()]));

         event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent());
      }
   }
}
