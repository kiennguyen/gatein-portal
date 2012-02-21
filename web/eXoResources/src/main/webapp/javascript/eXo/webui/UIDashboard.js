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

eXo.webui.UIDashboard = {
	
	currentCol : null ,

	targetObj : null,
	
	init : function (dragItem, dragObj) {
		
		var BROWSER = eXo.core.Browser;

    var jqDragObj = xj(dragObj);//JQuery wrapper of dragObj, that facilitates JQuery integration

    var portletFrag = jqDragObj.closest(".PORTLET-FRAGMENT");

    var gadgetContainer = portletFrag.find("div.GadgetContainer").eq(0);

		eXo.core.DragDrop2.init(dragItem, dragObj);

    dragObj.onDragStart = function(x, y, lastMouseX, lastMouseY, e)
    {

      var workingWS = document.getElementById("UIWorkingWorkspace");

      var ggwidth = dragObj.offsetWidth;

      //find position to put drag object in
      var mx = BROWSER.findMouseRelativeX(workingWS, e);
      var ox = BROWSER.findMouseRelativeX(dragObj, e);
      var x = mx - ox;

      var my = BROWSER.findMouseRelativeY(workingWS, e);
      var oy = BROWSER.findMouseRelativeY(dragObj, e);
      var y = my - oy;

      jqDragObj.parentsUntil(portletFrag).each(function()
      {
        if (this.scrollLeft > 0)
        {
          x -= this.scrollLeft;
        }
        if (this.scrollTop > 0)
        {
          y -= this.scrollTop;
        }
      });

      if (!jqDragObj.hasClass("SelectItem"))
      {
        var targetArea = xj("<div>").attr("id", "UITarget").addClass("UITarget").css("height", dragObj.offsetHeight + "px");
        eXo.webui.UIDashboard.targetObj = targetArea;
        jqDragObj.after(targetArea);
      }
      else
      {
        var copy = jqDragObj.clone(true).addClass("CopyObj");
        jqDragObj.before(copy);
      }

      //increase speed of mouse when over iframe by create div layer above it
      gadgetContainer.find("div.UIGadget").each(function()
      {
        var gadgetWindow = xj(this);
        var mask = gadgetWindow.find("div.UIMask").eq(0);
        if (mask)
        {
          var app = gadgetWindow.find("div.GadgetApplication")[0];
          mask.css({"marginTop" : - app.offsetHeight + "px", "height" : app.offsetHeight + "px", "width" : app.offsetWidth + "px", "display" : "block", "backgroundColor" : "white"});
          BROWSER.setOpacity(mask[0], 3);
        }
      });

      if (!jqDragObj.hasClass("Dragging"))
      {
        jqDragObj.addClass("Dragging");
      }

      jqDragObj.css("width", ggwidth + "px").css("position", "absolute");

      //set position of drag object
      BROWSER.setPositionInContainer(workingWS, dragObj, x, y);
    }

    dragObj.onDrag = function(nx, ny, ex, ey, e)
    {

      eXo.webui.UIDashboard.scrollOnDrag(dragObj);

      var targetArea = eXo.webui.UIDashboard.targetObj;
      if (eXo.webui.UIDashboardUtil.isIn(ex, ey, gadgetContainer[0]))
      {
        if (!targetArea)
        {
          targetArea = xj("<div>").attr("id", "UITarget").addClass("UITarget").css("height", dragObj.offsetHeight + "px");
          eXo.webui.UIDashboard.targetObj = targetArea;
        }

        var column = eXo.webui.UIDashboard.currentCol;
        if (!column)
        {
          column = eXo.webui.UIDashboardUtil.findContainingColumn(gadgetContainer, ex);
        }

        if (!column)
        {
          return; //That happens as user drags mouse out of the gadget container
        }

        if (eXo.webui.UIDashboardUtil.isInColumn(column, ex, gadgetContainer.scrollLeft()))
        {
          var addToLast = true;
          column.find("div.UIGadget").not("#" + dragObj.id).each(function()
          {
            if (ey <= BROWSER.findPosY(this) + (this.offsetHeight / 3) - gadgetContainer.scrollTop())
            {
              xj(this).before(targetArea);
              addToLast = false;

              return false;
            }
          });

          //That happens as user drags gadget to the bottom of a column or to an empty column
          if (addToLast)
          {
            column.append(targetArea);
          }
        }
        else
        {
          eXo.webui.UIDashboard.currentCol = eXo.webui.UIDashboardUtil.findContainingColumn(gadgetContainer, ex);
        }
      }
      else if (targetArea != null && jqDragObj.hasClass("SelectItem"))
      {
        //prevent dragging item form selector popup out of gadget container
        targetArea.remove();
        eXo.webui.UIDashboard.targetObj = targetArea = null;
      }
    };


    dragObj.onDragEnd = function(x, y, clientX, clientY)
    {
      var uiDashboardUtil = eXo.webui.UIDashboardUtil;

      gadgetContainer.find("div.UIMask").each(function()
      {
        eXo.core.Browser.setOpacity(this, 100);
        xj(this).css("display", "none");
      });

      var targetArea = eXo.webui.UIDashboard.targetObj;
      if (targetArea && !targetArea[0].parentNode)
      {
        targetArea = null;
      }

      jqDragObj.removeClass("Dragging").css("position", "static");

      var copyObj = portletFrag.find("div.CopyObj");
      if (copyObj)
      {
        copyObj.replaceWith(jqDragObj);
        jqDragObj.css("width", "auto");
      }

      if (targetArea)
      {
        //if drag object is not gadget module, create an module
        var col = uiDashboardUtil.findColIndexInDashboard(targetArea[0]);
        var row = uiDashboardUtil.findRowIndexInDashboard(targetArea[0]);
        var compId = portletFrag.parent().attr("id");

        if (jqDragObj.hasClass("SelectItem"))
        {
          var params = [
            {name: "colIndex", value: col},
            {name: "rowIndex", value: row},
            {name: "objectId", value: jqDragObj.attr("id")}
          ];
          var url = uiDashboardUtil.createRequest(compId, 'AddNewGadget', params);
          ajaxGet(url);
        }
        else
        {
          //in case: drop to old position
          if (uiDashboardUtil.findColIndexInDashboard(dragObj) == col
            && uiDashboardUtil.findRowIndexInDashboard(dragObj) == (row - 1))
          {
            targetArea.remove();
          }
          else
          {
            targetArea.replaceWith(jqDragObj);
            row = uiDashboardUtil.findRowIndexInDashboard(dragObj);
            var params = [
              {name: "colIndex", value: col},
              {name: "rowIndex", value: row},
              {name: "objectId", value: dragObj.id}
            ];
            var url = uiDashboardUtil.createRequest(compId, 'MoveGadget', params);
            ajaxGet(url);
          }
        }
      }

      gadgetContainer.find("div.UITarget").each(function()
      {
        xj(this).remove();
      });

      eXo.webui.UIDashboard.targetObj = eXo.webui.UIDashboard.currentCol = null;
    };


    dragObj.onCancel = function(e)
    {
      if (BROWSER.browserType == "ie" && BROWSER.findMouseYInClient() < 0)
      {
        eXo.core.DragDrop2.end(e);
      }
    };
  },
	
	onLoad : function(windowId, canEdit) {
    var portletWindow = xj("#" + windowId).eq(0);
    if(!portletWindow)
    {
      return;
    }

    var portletFrag = portletWindow.find(".PORTLET-FRAGMENT").eq(0);
    var dashboard = portletFrag.find("div.UIDashboard").eq(0);
    var container = dashboard.find("div.UIDashboardContainer").eq(0);
    if (!container)
    {
      return;
    }

    dashboard.css("overflow", "hidden");
    portletFrag.css("overflow", "hidden");

    var selectPopup = container.prev("div");
    selectPopup.find("a.CloseButton").eq(0).click(function()
    {
       eXo.webui.UIDashboard.hideSelectPopup(selectPopup);
    });

    var gadgetCont = container.children("div.GadgetContainer").eq(0);
    if (eXo.core.Browser.isIE6())
    {
      gadgetCont.css("width", "99.5%");
    }
    gadgetCont.children("div.UIColumns").eq(0).css("width", "100%");

		//Todo: nguyenanhkien2a@gmail.com
		//We set and increase waiting time for initDragDrop function to make sure all UI (tag, div, iframe, etc) 
		//was loaded and to avoid some potential bugs (ex: GTNPORTAL-1068)
		setTimeout("eXo.webui.UIDashboard.initDragDrop('" + windowId + "'," + canEdit + ");", 400) ;
	},
	
	initDragDrop : function(windowId, canEdit) {
    var portletWindow = xj("#" + windowId);

    //TODO: Improve this by seperate gadget control appearing in select popup and ones appearing in the dashboard
    portletWindow.find("div.GadgetControl").each(function()
    {
      var gadgetControl = xj(this);
      var gadget = gadgetControl.closest(".UIGadget");
      var minimizeButton = gadget.find("span.MinimizedAction").eq(0);//That might be undefined if actual gadget is the item in Select Gadget popup
      if(canEdit)
      {
        eXo.webui.UIDashboard.init(gadgetControl[0], gadget[0]);
        if(minimizeButton)
        {
          minimizeButton.css("display", "block");
        }
      }
      else
      {
        if(minimizeButton)
        {
          minimizeButton.css("display", "none");
          minimizeButton.siblings("div.CloseGadget,div.EditGadget").css("display", "none");
        }
      }
    });
	},
	
	initPopup : function(popup) {
		if(typeof(popup) == "string") popup = document.getElementById(popup);
		if(!popup || popup.style.display == "none") return;
		var uiDashboard = eXo.core.DOMUtil.findAncestorByClass(popup, "UIDashboard");
		var deltaY = Math.ceil((uiDashboard.offsetHeight - popup.offsetHeight) / 2);
		if (deltaY < 0) {
			deltaY = 0;
		}
		popup.style.top = eXo.core.Browser.findPosY(uiDashboard) + deltaY + "px";
	},

	/**
	 * Build a UITarget element (div element) with properties in parameters
	 * @param {Number} width
	 * @param {Number} height
	 */
	createTarget : function(width, height) {
		var uiTarget = document.createElement("div");
		uiTarget.id = "UITarget";
		uiTarget.className = "UITarget";
		uiTarget.style.width = width + "px";
		uiTarget.style.height = height + "px";
		return uiTarget;
	},
	 /**
   * Build a UITarget element (div element) with properties equal to object's properties in parameter
   * @param {Object} obj object
   */
	createTargetOfAnObject : function(obj) {
		var uiTarget = document.createElement("div");
		uiTarget.id = "UITarget";
		uiTarget.className = "UITarget";
		uiTarget.style.height = obj.offsetHeight + "px";
		return uiTarget;
	},

	 /**
   * Show SelectPopup as user click on 'Add Gadgets' link. The argument link represents <a> element of 'Add Gadgets' link.
   */
  showSelectPopup : function(link)
  {
    var jqLink = xj(link).parent();
    jqLink.css("visibility", "hidden");

    var portletID = jqLink.closest(".PORTLET-FRAGMENT").parent().attr("id");
    var url = eXo.webui.UIDashboardUtil.createRequest(portletID, "SetShowSelectContainer", [
      {name : "isShow", value : true}
    ]);
    ajaxGet(url);
  },

  hideSelectPopup : function(selectPopup)
  {
    selectPopup.css({"visibility" : "hidden", "display" : "none"});

    var dashboardCont = selectPopup.next("div.UIDashboardContainer");
    dashboardCont.find("a.AddIcon").eq(0).css("visibility", "visible");

    var portletID = dashboardCont.closest(".PORTLET-FRAGMENT").parent().attr("id");

    var url = eXo.webui.UIDashboardUtil.createRequest(portletID, "SetShowSelectContainer", [
      {name : "isShow", value : false}
    ]);
    ajaxAsyncGetRequest(url, false);
  },

  /**
   * Using when click event happens on a dashboard tab
   * @param {Object} clickElement
   * @param {String} normalStyle a css style
   * @param {String} selectedType a css style
   */
	onTabClick : function(clickElement, normalStyle, selectedType) {
		var DOMUtil = eXo.core.DOMUtil;
		var category = DOMUtil.findAncestorByClass(clickElement, "GadgetCategory");
		var categoryContent = DOMUtil.findFirstChildByClass(category, "div", "ItemsContainer");
		var categoriesContainer = DOMUtil.findAncestorByClass(category, "GadgetItemsContainer");
		var categories = DOMUtil.findChildrenByClass(categoriesContainer, "div", "GadgetCategory");
		var gadgetTab = DOMUtil.findFirstChildByClass(category, "div", "GadgetTab");
		
		if(DOMUtil.hasClass(gadgetTab, normalStyle)) {
			for(var i=0; i<categories.length; i++) {
				DOMUtil.findFirstChildByClass(categories[i], "div", "GadgetTab").className = "GadgetTab " + normalStyle;
				DOMUtil.findFirstChildByClass(categories[i], "div", "ItemsContainer").style.display = "none";
			}
			DOMUtil.findFirstChildByClass(category, "div", "GadgetTab").className = "GadgetTab " + selectedType;
			categoryContent.style.display = "block";
		} else {
			DOMUtil.findFirstChildByClass(category, "div", "GadgetTab").className = "GadgetTab " + normalStyle;
			categoryContent.style.display = "none";
		}
		
		var popupContent = DOMUtil.findAncestorByClass(clickElement, "PopupContent");
		var browser = eXo.core.Browser;
		if(browser.getBrowserHeight() - 100 < categoriesContainer.offsetHeight) {
			popupContent.style.height = (browser.getBrowserHeight() - 100) + "px";
		}	else {
			popupContent.style.height = "auto";
		}	
	},
	/**
	 * Change disabled object to enable state
	 * @param {Object} elemt object to enable
	 */
	enableContainer : function(elemt) {
		var DOMUtil = eXo.core.DOMUtil;
		if(DOMUtil.hasClass(elemt, "DisableContainer")) {
			DOMUtil.replaceClass(elemt, " DisableContainer", "");
		}
		var arrow = DOMUtil.findFirstChildByClass(elemt, "div", "Arrow");
		if(DOMUtil.hasClass(arrow, "DisableArrowIcon")) DOMUtil.replaceClass(arrow," DisableArrowIcon", "");
	},
	 /**
   * Change object to disable state
   * @param {Object} elemt object to enable
   */
	disableContainer : function(elemt) {
		var DOMUtil = eXo.core.DOMUtil;
		if(!DOMUtil.hasClass(elemt, "DisableContainer")) {
			DOMUtil.addClass(elemt, "DisableContainer");
		}
		var arrow = DOMUtil.findFirstChildByClass(elemt, "div", "Arrow");
		if(!DOMUtil.hasClass(arrow, "DisableArrowIcon")) DOMUtil.addClass(arrow," DisableArrowIcon");
	},
	
	scrollOnDrag : function(dragObj) {
		var DOMUtil = eXo.core.DOMUtil;
		var uiDashboard = DOMUtil.findAncestorByClass(dragObj, "UIDashboard");
		var gadgetContainer = DOMUtil.findFirstDescendantByClass(uiDashboard, "div", "GadgetContainer");
		var colCont = DOMUtil.findFirstChildByClass(gadgetContainer, "div", "UIColumns");
		
		if(!DOMUtil.findFirstDescendantByClass(colCont, "div", "UITarget")) return;
		
		var visibleWidth = gadgetContainer.offsetWidth;
		var visibleHeight = gadgetContainer.offsetHeight;
		var trueWidth = colCont.offsetWidth;
		var trueHeight = colCont.offsetHeight;
		
		var browser = eXo.core.Browser;
		var objLeft = browser.findPosXInContainer(dragObj, gadgetContainer);
		var objRight = objLeft + dragObj.offsetWidth;
		var objTop = browser.findPosYInContainer(dragObj, gadgetContainer);
		var objBottom = objTop + dragObj.offsetHeight;
		
		//controls horizontal scroll
		var deltaX = gadgetContainer.scrollLeft;
		if((trueWidth - (visibleWidth + deltaX) > 0) && objRight > visibleWidth) {
			gadgetContainer.scrollLeft += 5;
		} else {
			if(objLeft < 0 && deltaX > 0) gadgetContainer.scrollLeft -= 5;
		}
		
		//controls vertical scroll
		var controlBar = DOMUtil.findFirstChildByClass(gadgetContainer, "div", "ContainerControlBarL");
		var buttonHeight = 0 ;
		if(controlBar) buttonHeight = controlBar.offsetHeight;
		var deltaY = gadgetContainer.scrollTop;
		if((trueHeight - (visibleHeight -10 - buttonHeight + deltaY) > 0) && objBottom > visibleHeight) {
			gadgetContainer.scrollTop += 5;
		}	else {
			if(objTop < 0 && deltaY > 0) gadgetContainer.scrollTop -= 5;
		}
	}
};