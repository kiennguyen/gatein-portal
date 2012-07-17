/**
 * Copyright (C) 2009 eXo Platform SAS.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

define("eXo.gadget.servMgnt", ["SHARED/jquery"], function($) {
  var _module = {
    DEFAULT_SERVICES_URL : "/portal/rest/management",
    
    init : function() { 
    	var prefs = new _IG_Prefs();
    	var servicesURL = prefs.getString("servicesURL");
    	if (servicesURL && $.trim(servicesURL) != "") {
    		this.SERVICES_URL = $.trim(servicesURL);
    	} else {
    		this.SERVICES_URL = this.DEFAULT_SERVICES_URL;
    	}
    	
    	function getContext(url) {		
    		if (!url) return "";
    		var fslash = url.indexOf("/");
    		var lslash = url.indexOf("/", fslash + 2);
    		var context = url.substring(0, lslash);
    		return context;
    	}
    	
    	if (this.SERVICES_URL.indexOf("http://") == 0 || 
    			this.SERVICES_URL.indexOf("https://") == 0) {
    		if (getContext(document.location.href) !== getContext(this.SERVICES_URL)) {
    			alert(prefs.getMsg("failManage"));
    			return;
    		}
    	}
    	
    	this.makeRequest(this.SERVICES_URL, this.renderServiceSelector);
    },
    
    renderServiceSelector : function(services) {
    	if (!services || !services.value || services.value.length == 0) {
    		alert(new _IG_Prefs().getMsg("noServices"));		
    	}
    	var servicesSelector = $("#servicesSelector");
    	var optionsHtml = "";
    
    	if (services && services.value) {
    		var serviceNames = services.value;
    
    		for ( var i = 0; i < serviceNames.length; i++) {
    			optionsHtml += "<option>" + gadgets.util.escapeString(serviceNames[i])
    					+ "</option>";
    		}
    	}
    
    	servicesSelector.html(optionsHtml);
    	servicesSelector.change();
    },
    
    renderServiceDetailForHome : function(data) {
        if (data) {
            if(data.description) {
                $("#ServiceDescription").html(data.description);    
            } else {
            	$("#ServiceDescription").html(new _IG_Prefs().getMsg("noDescription"));
            }
            
            if(data.methods) {
                _module.renderMethodSelector(data);
            }
            
            if(data.properties) {
                _module.renderPropertySelector(data);
            }
        }
    },
    
    renderMethodSelector : function(methodData) {
    	var methodSelector = $("#methodsSelector");
    	var optionsHtml = "";
    	var methods = null;
    
    	if (methodData && methodData.methods) {
    		methods = methodData.methods;
    
    		for ( var i = 0; i < methods.length; i++) {
    			optionsHtml += "<option>" + gadgets.util.escapeString(methods[i].name)
    					+ "</option>";
    		}
    	}
    
    	if (optionsHtml == "") {
    		optionsHtml = "<option></option>";
    	}
    
    	methodSelector.html(optionsHtml);
    	methodSelector.data('methods', methods);
    	methodSelector.change();
    },
    
    renderPropertySelector : function(propertyData) {
        var propertySelector = $("#propertiesSelector");
        var optionsHtml = "";
        var properties = null;
    
        if (propertyData && propertyData.properties) {
            properties = propertyData.properties;
    
            for ( var i = 0; i < properties.length; i++) {
                optionsHtml += "<option>" + gadgets.util.escapeString(properties[i].name)
                        + "</option>";
            }
        }
    
        if (optionsHtml == "") {
            optionsHtml = "<option></option>";
        }
    
        propertySelector.html(optionsHtml);
        propertySelector.data('properties', properties);
        propertySelector.change();
    },
    
    renderMethodDetail : function(method) {
    	if (!method) {
    		method = {
    			name : "",
    			description : "",
    			method : "",
    			parameters : []
    		};
    	}
    	var util = gadgets.util;
    
    	$("#methodName").html(util.escapeString(method.name));
    	$("#methodDescription").html(util.escapeString(method.description ? method.description : ""));
    	$("#reqMethod").html(util.escapeString(method.method));
    
    	var paramTable = "<table>";
    	for ( var i = 0; i < method.parameters.length; i++) {
    		paramTable += "<tr><td>" + util.escapeString(method.parameters[i].name)
    				+ "</td></tr>";
    	}
    
    	if (paramTable == "<table>") {
    		paramTable += "<tr><td>[]</td></tr>";
    	}
    	paramTable += "</table>";
    	$("#parametersTable").html(paramTable);
    	_module.resetHeight();
    },
    
    renderPropertyDetail : function(property) {
        if (!property) {
            property = {
                name : "",
                description : ""
            };
        }
        var util = gadgets.util;
    
        $("#propertyName").html(util.escapeString(property.name));
        $("#propertyDescription").html(util.escapeString(property.description ? property.description : ""));
        _module.resetHeight();
    },
    // End Home View
    
    // Start Canvas view
    renderServiceDetailForCanvas : function(data) {
    	if (data) {				
            if(data.description) {
                $("#ServiceDescription").html(data.description);    
            } else {
            	$("#ServiceDescription").html(new _IG_Prefs().getMsg("noDescription"));
            }
    	    
    		if(data.methods) {
    			_module.renderMethodsForCanvas(data);
    		}
    		
    		if(data.properties) {
    		  _module.renderPropertiesForCanvas(data);
    		}				
    
    		_module.fadeIn($(".ContentSelected")[0]);
    	}
    },
    
    getContentContainer : function(tab) {
    	if (tab.id == "MethodsTab") {
    		return $("#ServiceMethods")[0];
    	} else {
    		return  $("#ServiceProperties")[0];
    	}
    },
    
    fadeIn : function(target, callback) {
    	$(target).hide();
    	$(target).fadeIn(700, callback);		
    	_module.resetHeight();
    },
    
    renderMethodsForCanvas : function(methodData) {
    	if (!methodData || !methodData.methods) {
    		return;
    	}
    
    	var methods = methodData.methods;
    	var methodForCanvas = "";
    	var util = gadgets.util;
    
    	for ( var i = 0; i < methods.length; i++) {
    		var method = methods[i];
    		var methodName = util.escapeString(method.name);
    		var methodDescription = util.escapeString(method.description ? method.description : "");
    		var reqMethod = util.escapeString(method.method);
    
    		var rowClass = i % 2 == 0 ? "EvenRow" : "OddRow";
    		methodForCanvas += "<tr class='" + rowClass + "'>"
    				+ "<td><div class='Text methodName'>" + methodName + "</div></td>"
    				+ "<td><div class='Text methodDescription'>" + methodDescription + "</div></td>"
    				+ "<td><div class='Text reqMethod'>" + reqMethod + "</div></td>"
    				+ "<td><form style='margin-bottom: 0px;'>";
    		for ( var j = 0; j < method.parameters.length; j++) {
    			methodForCanvas += "<div class='SkinID'>"
    					+ util.escapeString(method.parameters[j].name) + " "
    					+ "<input type='text' name='"
    					+ util.escapeString(method.parameters[j].name) + "'>" + "</div>";
    		}
    		methodForCanvas += "</form></td>" + "<td>"
    				+ "<div class='MethodActionButton GadgetStyle FL'>"
    				+ "<div class='ButtonLeft'>" + "<div class='ButtonRight'>"
    				+ "<div class='ButtonMiddle'>" + "<a href='#'>Run</a>" + "</div>"
    				+ "</div>" + "</div>" + "</div>" + "</td></tr>";
    
    	}
    	if (methodForCanvas == "") {
    		methodForCanvas = "<tr class='EventRow'><td colspan='5' align='center'><div class='Text'>" + new _IG_Prefs().getMsg("noMethod") + "</div></td></tr>";
    	}
    	$("#methodsForCanvas").html(methodForCanvas);	
    },
    
    /**
     * data is not null
     */
    renderPropertiesForCanvas : function(data) {
    	var props = data.properties;
    	var propertyForCanvas = "";
    	var util = gadgets.util;
    
    	for ( var i = 0; i < props.length; i++) {
    		var prop = props[i];
    		var propName = util.escapeString(prop.name);
    		var propDescription = util.escapeString(prop.description ? prop.description : "");
    
    		var rowClass = i % 2 == 0 ? "EvenRow" : "OddRow";
    		propertyForCanvas += "<tr class='" + rowClass + "'>"
    				+ "<td><div class='Text propName'>" + propName + "</div></td>"
    				+ "<td><div class='Text propDescription'>" + propDescription + "</div></td>";
    
    		propertyForCanvas += "<td>"
    				+ "<div class='PropertyActionButton GadgetStyle FL'>"
    				+ "<div class='ButtonLeft'>" + "<div class='ButtonRight'>"
    				+ "<div class='ButtonMiddle'>" + "<a href='#'>Get</a>" + "</div>"
    				+ "</div>" + "</div>" + "</div>" + "</td></tr>";
    
    	}
    	if (propertyForCanvas == "") {
    		propertyForCanvas = "<tr class='EvenRow'><td colspan='3' align='center'><div class='Text'>" + new _IG_Prefs().getMsg("noProperty") + "</div></td></tr>";
    	}
    	$("#propertiesForCanvas").html(propertyForCanvas);	
    },
    
    showMinimessage : function(jsonMessage) {
        var msgObj = $("#resultMessage")[0];
        $(msgObj).css("Visibility", "hidden");
        $(msgObj).html("");
      
    	var parsedObj;
    	try {
    		parsedObj = gadgets.json.parse(jsonMessage);
    	} catch (e) {
    		parsedObj = jsonMessage;
    	}
    	var htmlTable = $.trim(_module.objToTable(parsedObj));
    	if (htmlTable == "" || htmlTable == "empty object") {
    		htmlTable = "Method's executed, return no result";
    	}
    
    	var msg = new gadgets.MiniMessage("ServicesManagement", msgObj);
    	var executeMsg = msg.createDismissibleMessage(htmlTable, function() {
    		window.setTimeout(function() {gadgets.window.adjustHeight($(".UIGadget").height()); }, 500);					
    		return true;
    	});
    	
    	executeMsg.style.height = "100px";
    	executeMsg.style.overflow = "auto";
    	$(".mmlib_xlink").each(function() {
    		$(this.parentNode).attr("style", "vertical-align: top");
    		$(this).html("");
    	});
    	$(".mmlib_table .UIGrid").each(function() {
    		$(this.parentNode).attr("style", "vertical-align: top");
    	});
    	
    	_module.resetHeight();	
    	$(msgObj).hide();
    	$(msgObj).slideDown(1200);
    	$(msgObj).css("Visibility", "visible");
    },
    
    objToTable : function(obj) {
    	var type = typeof (obj);
    	if (type != "object") {
    		return gadgets.util.escapeString(obj + "");
    	}
    
    	if (!obj || $.isEmptyObject(obj)
    			|| (obj.constructor == Array && obj.length == 0)) {
    		return "empty object";
    	}
    
    	var str = "<table cellspacing='0' class='UIGrid'>";
    	if (obj.constructor == Array) {
    		for ( var i = 0; i < obj.length; i++) {
    			var rowClass = i % 2 == 0 ? "EvenRow" : "OddRow";
    			str += "<tr class='" + rowClass + "'><td><div class='Text'>";
    			str += _module.objToTable(obj[i]);
    			str += "</div></td></tr>";
    		}
    	} else {
    		str += "<tr>";
    		for ( var prop in obj) {
    			str += "<th>";
    			str += _module.objToTable(prop);
    			str += "</th>";
    		}
    		str += "</tr>";
    
    		str += "<tr>";
    		for ( var prop in obj) {
    			str += "<td>";
    			str += _module.objToTable(obj[prop]);
    			str += "</td>";
    		}
    		str += "</tr>";
    	}
    
    	str += "</table>";
    	return str;
    },
    
    resetHeight : function() {
    	if ($.browser.safari) {
    		gadgets.window.adjustHeight($(".UIGadget").height());
    	} else {
    		gadgets.window.adjustHeight();
    	}
    },
    
    /**
     * @param reqUrl - String
     * @param callback - Function
     * @param sendData - Data that will be send to server 
     * @param returnType - String html/xml/json/script
     * @param reqMethod - GET/POST/PUT...
     * @return XMLHttpRequest object
     */
    makeRequest : function(reqUrl, callback, sendData, returnType, reqMethod) {	
    	if (reqUrl == "") {
    		return;
    	}
    	reqMethod = reqMethod ? reqMethod : "GET";
    	returnType = returnType ? returnType : "json";
    	
    	return $.ajax({
    					  url: reqUrl,
    					  type: reqMethod,					  
    					  success: callback,
    					  contentType: "application/x-www-form-urlencoded",
    					  error: function() {
    						  var prefs = new _IG_Prefs();
    						  alert(prefs.getMsg("badURL"));
    					  },
    					  data: sendData,
    					  dataType: returnType,
    					  beforeSend: function(xhr) {
    					  	xhr.setRequestHeader("If-Modified-Since", "Thu, 1 Jan 1970 00:00:00 GMT");
    					  } 
    					});
    },
    
    initDOM : function() {
      
      // ======================Handler======================================//
      $("#servicesSelector").change(function () {
        var serviceName = $(this).val();
        serviceName = gadgets.util.unescapeString(!serviceName ? "" : serviceName);
        var methodsURL = _module.SERVICES_URL + "/" + encodeURIComponent(serviceName);
        
        var currView = gadgets.views.getCurrentView().getName();
        if (currView == "home") {
          _module.makeRequest(methodsURL, _module.renderServiceDetailForHome);
        } else {
          _module.makeRequest(methodsURL, _module.renderServiceDetailForCanvas);
        }
      });

      $(".Tab").click(function () {
        var selectedTab = $(".TabSelected")[0];     
        if (this == selectedTab) {
          return;
        }

        $(selectedTab).removeClass("TabSelected");
        $(this).addClass("TabSelected");
        
        var selectedContent = _module.getContentContainer(selectedTab);
        var content = _module.getContentContainer(this);
        
        $(selectedContent).removeClass("ContentSelected");
        $(selectedContent).hide();
        _module.fadeIn(content, function() {
          $(this).addClass("ContentSelected");
        });   
      });
      
      $(".DesIconHome").click(function () {
        _module.fadeIn($(".DescriptionBox")[0], function() {
          var desBox = this; 
          window.setTimeout(function() {
            $(desBox).fadeOut(2000, function() {
              _module.resetHeight();
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

        _module.renderPropertyDetail(property);
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

          _module.renderMethodDetail(method);
        });
      
      $('.MethodActionButton').live('click', function(event) {
        event.preventDefault();
        var tr = this.parentNode.parentNode;    
        var methodName = gadgets.util.unescapeString($(".methodName", tr).text());
        var reqMethod = gadgets.util.unescapeString($(".reqMethod", tr).text());
        var serviceName = $("#servicesSelector").val();
        serviceName = gadgets.util.unescapeString(!serviceName ? "" : serviceName);
        var param = $("form", tr).serialize();
        
        var execLink = _module.SERVICES_URL + "/" + 
                            encodeURIComponent(serviceName) + "/" + 
                            encodeURIComponent(methodName);
        _module.makeRequest(execLink, _module.showMinimessage, param, "text", reqMethod);
      });
      
      $('.PropertyActionButton').live('click', function(event) {
          event.preventDefault();
          var tr = this.parentNode.parentNode;        
          var propName = gadgets.util.unescapeString($(".propName", tr).text());
          var reqMethod = "GET";
          var serviceName = $("#servicesSelector").val();
          serviceName = gadgets.util.unescapeString(!serviceName ? "" : serviceName);
          
          var execLink = _module.SERVICES_URL + "/" + 
                                                    encodeURIComponent(serviceName) + "/" + 
                                                    encodeURIComponent(propName);
          _module.makeRequest(execLink, _module.showMinimessage, null, "text", reqMethod);
        })
    }
  };
  
	return _module;
});