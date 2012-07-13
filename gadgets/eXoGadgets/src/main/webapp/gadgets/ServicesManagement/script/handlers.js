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
 
require(["SHARED/jquery", "eXo.gadget.ServicesManagement"], function($, ServicesManagement) {
	
	//======================Handler======================================//
	$("#servicesSelector").change(function () {
	  var serviceName = $(this).val();
	  serviceName = gadgets.util.unescapeString(!serviceName ? "" : serviceName);
	  var methodsURL = ServicesManagement.SERVICES_URL + "/" + encodeURIComponent(serviceName);
	  
	  var currView = gadgets.views.getCurrentView().getName();
	  if (currView == "home") {
	  	ServicesManagement.makeRequest(methodsURL, ServicesManagement.renderServiceDetailForHome);
	  } else {
	  	ServicesManagement.makeRequest(methodsURL, ServicesManagement.renderServiceDetailForCanvas);
	  }
	});

	$(".Tab").click(function () {
		var selectedTab = $(".TabSelected")[0];			
		if (this == selectedTab) {
			return;
		}

		$(selectedTab).removeClass("TabSelected");
		$(this).addClass("TabSelected");
		
		var selectedContent = ServicesManagement.getContentContainer(selectedTab);
		var content = ServicesManagement.getContentContainer(this);
		
		$(selectedContent).removeClass("ContentSelected");
		$(selectedContent).hide();
		ServicesManagement.fadeIn(content, function() {
			$(this).addClass("ContentSelected");
		});		
	});
	
	$(".DesIconHome").click(function () {
		ServicesManagement.fadeIn($(".DescriptionBox")[0], function() {
			var desBox = this; 
			window.setTimeout(function() {
				$(desBox).fadeOut(2000, function() {
					ServicesManagement.resetHeight();
				});
			}, 5000);
		});
	});
	
	$("#propertiesSelector").change(function () {
	  var propertyName = $(this).val();
	  propertyName = gadgets.util.unescapeString(!propertyName ? "" : propertyName);

	  var propertyData = $(this).data('properties');	
	  var property = null;
	  if (propertyData) {
		  for (var i = 0; i < propertyData.length; i++) {
		  	if (propertyData[i].name == propertyName) {
		  		property = propertyData[i];
		  	}
		  }
	  }

	  ServicesManagement.renderPropertyDetail(property);
	});
	
	$("#methodsSelector").change(function () {
      var methodName = $(this).val();
      methodName = gadgets.util.unescapeString(!methodName ? "" : methodName);

      var methodData = $(this).data('methods');
      var method = null;
      if (methodData) {
          for (var i = 0; i < methodData.length; i++) {
            if (methodData[i].name == methodName) {
                method = methodData[i];
            }
          }
      }

      ServicesManagement.renderMethodDetail(method);
    });
	
	$('.MethodActionButton').live('click', function(event) {
		event.preventDefault();
		var tr = this.parentNode.parentNode;		
		var methodName = gadgets.util.unescapeString($(".methodName", tr).text());
	  var reqMethod = gadgets.util.unescapeString($(".reqMethod", tr).text());
	  var serviceName = $("#servicesSelector").val();
	  serviceName = gadgets.util.unescapeString(!serviceName ? "" : serviceName);
	  var param = $("form", tr).serialize();
	  
		var execLink = ServicesManagement.SERVICES_URL + "/" + 
												encodeURIComponent(serviceName) + "/" + 
												encodeURIComponent(methodName);
		ServicesManagement.makeRequest(execLink, ServicesManagement.showMinimessage, param, "text", reqMethod);
	});
	
	$('.PropertyActionButton').live('click', function(event) {
      event.preventDefault();
      var tr = this.parentNode.parentNode;        
      var propName = gadgets.util.unescapeString($(".propName", tr).text());
      var reqMethod = "GET";
      var serviceName = $("#servicesSelector").val();
      serviceName = gadgets.util.unescapeString(!serviceName ? "" : serviceName);
      
      var execLink = ServicesManagement.SERVICES_URL + "/" + 
                                                encodeURIComponent(serviceName) + "/" + 
                                                encodeURIComponent(propName);
      ServicesManagement.makeRequest(execLink, ServicesManagement.showMinimessage, null, "text", reqMethod);
    });
});