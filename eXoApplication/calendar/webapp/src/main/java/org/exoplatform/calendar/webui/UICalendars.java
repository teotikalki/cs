/**
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 **/
package org.exoplatform.calendar.webui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.webui.popup.UICalDavForm;
import org.exoplatform.calendar.webui.popup.UICalendarCategoryForm;
import org.exoplatform.calendar.webui.popup.UICalendarCategoryManager;
import org.exoplatform.calendar.webui.popup.UICalendarForm;
import org.exoplatform.calendar.webui.popup.UICalendarSettingForm;
import org.exoplatform.calendar.webui.popup.UIEventCategoryManager;
import org.exoplatform.calendar.webui.popup.UIExportForm;
import org.exoplatform.calendar.webui.popup.UIImportForm;
import org.exoplatform.calendar.webui.popup.UIPopupAction;
import org.exoplatform.calendar.webui.popup.UIPopupContainer;
import org.exoplatform.calendar.webui.popup.UIQuickAddEvent;
import org.exoplatform.calendar.webui.popup.UIRssForm;
import org.exoplatform.calendar.webui.popup.UISharedForm;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/templates/calendar/webui/UICalendars.gtmpl",
    events = {
      @EventConfig(listeners = UICalendars.AddCalendarActionListener.class),
      @EventConfig(listeners = UICalendars.AddEventCategoryActionListener.class),
      @EventConfig(listeners = UICalendars.EditGroupActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UICalendars.DeleteGroupActionListener.class, confirm="UICalendars.msg.confirm-delete-group"), 
      @EventConfig(listeners = UICalendars.ExportCalendarActionListener.class), 
      @EventConfig(listeners = UICalendars.ExportCalendarsActionListener.class), 
      @EventConfig(listeners = UICalendars.ImportCalendarActionListener.class),
      @EventConfig(listeners = UICalendars.GenerateRssActionListener.class), 
      @EventConfig(listeners = UICalendars.GenerateCalDavActionListener.class), 
      @EventConfig(listeners = UICalendars.AddEventActionListener.class),
      @EventConfig(listeners = UICalendars.AddTaskActionListener.class),
      @EventConfig(listeners = UICalendars.EditCalendarActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UICalendars.RemoveCalendarActionListener.class, confirm="UICalendars.msg.confirm-delete-calendar"),
      @EventConfig(listeners = UICalendars.AddCalendarCategoryActionListener.class),
      @EventConfig(listeners = UICalendars.ShareCalendarActionListener.class),
      @EventConfig(listeners = UICalendars.ChangeColorActionListener.class),
      @EventConfig(listeners = UICalendars.CalendarSettingActionListener.class)
    }
)

public class UICalendars extends UIForm  {
  public static String CALENDARID = "calendarid".intern() ;
  public static String CALTYPE = "calType".intern() ;
  public static String CALNAME = "calName".intern() ;
  public static String CALCOLOR = "calColor".intern() ;
  private String[] publicCalendarIds = {} ;
  private LinkedHashMap<String, String> colorMap_ = new LinkedHashMap<String, String>() ;

  public UICalendars() throws Exception {

  } 

  public String[] getPublicCalendarIds(){ return publicCalendarIds ; }
  private SessionProvider getSession() {
    return SessionProviderFactory.createSessionProvider() ;
  }
  private SessionProvider getSystemSession() {
    return SessionProviderFactory.createSystemProvider() ;
  }
  
  protected List<GroupCalendarData> getPrivateCalendars() throws Exception{
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    //List<CalendarCategory> categories = calendarService.getCategories(SessionsUtils.getSessionProvider(), username) ;
    List<GroupCalendarData> groupCalendars = calendarService.getCalendarCategories(getSession(), username, false) ;
    for(GroupCalendarData group : groupCalendars) {
      List<Calendar> calendars = group.getCalendars() ;
      if(calendars != null) {
        for(Calendar calendar : calendars) {
          colorMap_.put(calendar.getId(), calendar.getCalendarColor()) ;
          if(getUIFormCheckBoxInput(calendar.getId()) == null){
            addUIFormInput(new UIFormCheckBoxInput<Boolean>(calendar.getId(), calendar.getId(), false).setChecked(true)) ;
          } else {
            //TODO wait for dunghm about javaScript
            //getUIFormCheckBoxInput(calendar.getId()).setChecked(true) ;
          }
        }
      }
    }
    return groupCalendars;
  }

  protected List<GroupCalendarData> getPublicCalendars() throws Exception{
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    String[] groups = CalendarUtils.getUserGroups(username) ;
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    List<GroupCalendarData> groupCalendars = calendarService.getGroupCalendars(getSystemSession(), groups, false, username) ;
    Map<String, String> map = new HashMap<String, String> () ;    
    for(GroupCalendarData group : groupCalendars) {
      List<Calendar> calendars = group.getCalendars() ;
      for(Calendar calendar : calendars) {
        map.put(calendar.getId(), calendar.getId()) ;
        colorMap_.put(calendar.getId(), calendar.getCalendarColor()) ;
        if(getUIFormCheckBoxInput(calendar.getId()) == null){
          addUIFormInput(new UIFormCheckBoxInput<Boolean>(calendar.getId(), calendar.getId(), false).setChecked(true)) ;
        }
      }
    }
    publicCalendarIds = map.values().toArray(new String[]{}) ;
    return groupCalendars ;
  }

  protected GroupCalendarData getSharedCalendars() throws Exception{
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    GroupCalendarData groupCalendars = calendarService.getSharedCalendars(getSystemSession(), CalendarUtils.getCurrentUser(), false) ;
    if(groupCalendars != null) {
      List<Calendar> calendars = groupCalendars.getCalendars() ;
      for(Calendar calendar : calendars) {
        colorMap_.put(calendar.getId(), calendar.getCalendarColor()) ;
        if(getUIFormCheckBoxInput(calendar.getId()) == null){
          addUIFormInput(new UIFormCheckBoxInput<Boolean>(calendar.getId(), calendar.getId(), false).setChecked(true)) ;
        }
      }
    }
    return groupCalendars ;
  }

  public LinkedHashMap<String, String> getColorMap() {
    return colorMap_;
  }
  public String[] getColors() {
    return Calendar.COLORS ;
  }
  static  public class AddCalendarActionListener extends EventListener<UICalendars> {
    public void execute(Event<UICalendars> event) throws Exception {
      UICalendars uiComponent = event.getSource() ;
      String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UICalendarPortlet uiCalendarPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
      UIPopupAction popupAction = uiCalendarPortlet.getChild(UIPopupAction.class) ;
      UIPopupContainer uiPopupContainer = uiCalendarPortlet.createUIComponent(UIPopupContainer.class, null, null) ;
      uiPopupContainer.setId(UIPopupContainer.UICALENDARPOPUP) ;
      UICalendarForm calendarForm = uiPopupContainer.addChild(UICalendarForm.class, null, null) ;
      calendarForm.setSelectedGroup(categoryId) ;
      popupAction.activate(uiPopupContainer, 600, 0, true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiComponent.getParent()) ;
    }
  }
  static  public class AddEventCategoryActionListener extends EventListener<UICalendars> {
    public void execute(Event<UICalendars> event) throws Exception {
      UICalendars uiCalendars = event.getSource() ;
      UICalendarPortlet calendarPortlet = uiCalendars.getAncestorOfType(UICalendarPortlet.class) ;
      UIPopupAction popupAction = calendarPortlet.getChild(UIPopupAction.class) ;
      popupAction.activate(UIEventCategoryManager.class, 470) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  static  public class EditGroupActionListener extends EventListener<UICalendars> {
    public void execute(Event<UICalendars> event) throws Exception {
      UICalendars uiComponent = event.getSource() ;
      UICalendarPortlet uiCalendarPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
      UIPopupAction popupAction = uiCalendarPortlet.getChild(UIPopupAction.class) ;
      UICalendarCategoryManager uiManager = popupAction.activate(UICalendarCategoryManager.class, 470) ;
      UICalendarCategoryForm uiForm = uiManager.getChild(UICalendarCategoryForm.class) ;
      String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiForm.init(categoryId) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiComponent.getParent()) ;
    }
  }
  static  public class DeleteGroupActionListener extends EventListener<UICalendars> {
    public void execute(Event<UICalendars> event) throws Exception {
      UICalendars uiComponent = event.getSource() ;
      String calendarCategoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UICalendarPortlet uiPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
      CalendarService calService = uiComponent.getApplicationComponent(CalendarService.class) ;
      String username = event.getRequestContext().getRemoteUser() ;
      calService.removeCalendarCategory(uiComponent.getSession(), username, calendarCategoryId) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiComponent) ; 
      UICalendarWorkingContainer uiWorkingContainer = uiPortlet.findFirstComponentOfType(UICalendarWorkingContainer.class) ;
      UICalendarViewContainer uiViewContainer = uiPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
      uiViewContainer.refresh() ;
      try{       
        UIMiniCalendar uiMiniCalendar = uiPortlet.findFirstComponentOfType(UIMiniCalendar.class) ;
        uiMiniCalendar.updateMiniCal() ;
      } catch (Exception e) {
        e.printStackTrace() ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingContainer) ;
    }
  }

  static  public class AddEventActionListener extends EventListener<UICalendars> {
    public void execute(Event<UICalendars> event) throws Exception {
      UICalendars uiComponent = event.getSource() ;
      String calendarId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String calendarName = event.getRequestContext().getRequestParameter(CALNAME) ;
      String calType = event.getRequestContext().getRequestParameter(CALTYPE) ;
      UICalendarPortlet uiCalendarPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
      UIPopupAction popupAction = uiCalendarPortlet.getChild(UIPopupAction.class) ;
      UIQuickAddEvent uiQuickAddEvent = popupAction.activate(UIQuickAddEvent.class, 600) ;
      uiQuickAddEvent.setEvent(true) ;  
      uiQuickAddEvent.setId("UIQuickAddEvent") ;
      uiQuickAddEvent.init(uiCalendarPortlet.getCalendarSetting(), null, null) ;
      List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
      if(calType.equals(CalendarUtils.PRIVATE_TYPE)) {
        options = null ;
      } else if(calType.equals(CalendarUtils.SHARED_TYPE)) {
        GroupCalendarData calendarData = uiComponent.getSharedCalendars() ;
        for(Calendar cal : calendarData.getCalendars()) {
          options.add(new SelectItemOption<String>(cal.getName(), cal.getId())) ;
        }
      } else if(calType.equals(CalendarUtils.PUBLIC_TYPE)) {
        for (GroupCalendarData calendarData : uiComponent.getPublicCalendars()) {
          for(Calendar cal : calendarData.getCalendars()) {
            options.add(new SelectItemOption<String>(cal.getName(), cal.getId())) ;
          }
        }
      }    
      uiQuickAddEvent.update(calType, options) ;
      uiQuickAddEvent.setSelectedCalendar(calendarId) ;
      uiQuickAddEvent.setSelectedCategory("Meeting") ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiComponent) ;
    }
  }

  static  public class AddTaskActionListener extends EventListener<UICalendars> {
    public void execute(Event<UICalendars> event) throws Exception {
      UICalendars uiComponent = event.getSource() ;
      String calendarId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String calendarName = event.getRequestContext().getRequestParameter(CALNAME) ;
      String calType = event.getRequestContext().getRequestParameter(CALTYPE) ;
      UICalendarPortlet uiCalendarPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
      UIPopupAction popupAction = uiCalendarPortlet.getChild(UIPopupAction.class) ;
      UIQuickAddEvent uiQuickAddEvent = popupAction.activate(UIQuickAddEvent.class, 600) ;
      uiQuickAddEvent.setEvent(false) ;  
      uiQuickAddEvent.setId("UIQuickAddTask") ;
      uiQuickAddEvent.init(uiCalendarPortlet.getCalendarSetting(), null, null) ;
      List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
      if(calType.equals(CalendarUtils.PRIVATE_TYPE)) {
        options = null ;
      } else if(calType.equals(CalendarUtils.SHARED_TYPE)) {
        GroupCalendarData calendarData = uiComponent.getSharedCalendars() ;
        for(Calendar cal : calendarData.getCalendars()) {
          options.add(new SelectItemOption<String>(cal.getName(), cal.getId())) ;
        }
      } else if(calType.equals(CalendarUtils.PUBLIC_TYPE)) {
        for (GroupCalendarData calendarData : uiComponent.getPublicCalendars()) {
          for(Calendar cal : calendarData.getCalendars()) {
            options.add(new SelectItemOption<String>(cal.getName(), cal.getId())) ;
          }
        }
      }    
      uiQuickAddEvent.update(calType, options) ;
      uiQuickAddEvent.setSelectedCalendar(calendarId) ;
      uiQuickAddEvent.setSelectedCategory("Meeting") ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiComponent.getParent()) ;
    }
  }

  static  public class EditCalendarActionListener extends EventListener<UICalendars> {
    public void execute(Event<UICalendars> event) throws Exception {
      UICalendars uiComponent = event.getSource() ;
      String calendarId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String username = event.getRequestContext().getRemoteUser() ;
      UICalendarPortlet uiCalendarPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
      UIPopupAction popupAction = uiCalendarPortlet.getChild(UIPopupAction.class) ;
      UIPopupContainer uiPopupContainer = uiCalendarPortlet.createUIComponent(UIPopupContainer.class, null, null) ;
      uiPopupContainer.setId(UIPopupContainer.UICALENDARPOPUP) ;
      UICalendarForm uiCalendarForm = uiPopupContainer.addChild(UICalendarForm.class, null, null) ;
      CalendarService calService = uiCalendarForm.getApplicationComponent(CalendarService.class) ;
      Calendar calendar = calService.getUserCalendar(uiComponent.getSession(), username, calendarId) ;
      uiCalendarForm.init(calendar) ;
      popupAction.activate(uiPopupContainer, 600, 0, true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiComponent.getParent()) ;
    }
  }
  static  public class RemoveCalendarActionListener extends EventListener<UICalendars> {
    public void execute(Event<UICalendars> event) throws Exception {
      UICalendars uiComponent = event.getSource() ;
      String username = event.getRequestContext().getRemoteUser() ;
      String calendarId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String calType = event.getRequestContext().getRequestParameter(CALTYPE) ;
      UICalendarPortlet uiPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
      UIMiniCalendar uiMiniCalendar = uiPortlet.findFirstComponentOfType(UIMiniCalendar.class) ;
      UICalendarViewContainer uiViewContainer = uiPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
      UICalendarWorkingContainer workingContainer = uiComponent.getAncestorOfType(UICalendarWorkingContainer.class) ;
      if(calType.equals(CalendarUtils.PRIVATE_TYPE)) {
        CalendarUtils.getCalendarService().removeUserCalendar(uiComponent.getSession(), username, calendarId) ;
      }else if(calType.equals(CalendarUtils.SHARED_TYPE)) {
        CalendarUtils.getCalendarService().removeSharedCalendar(uiComponent.getSystemSession(), username, calendarId) ;
      }else if(calType.equals(CalendarUtils.PUBLIC_TYPE)) {
        CalendarUtils.getCalendarService().removeGroupCalendar(uiComponent.getSystemSession(), calendarId) ;
      }
      try{        
        uiMiniCalendar.updateMiniCal() ;
        uiViewContainer.refresh() ;
        uiPortlet.setCalendarSetting(null) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(workingContainer) ;
      } catch (Exception e) {
        e.printStackTrace() ;
      }
    }
  }

  static  public class AddCalendarCategoryActionListener extends EventListener<UICalendars> {
    public void execute(Event<UICalendars> event) throws Exception {
      UICalendars uiComponent = event.getSource() ;
      UICalendarPortlet uiCalendarPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
      UIPopupAction popupAction = uiCalendarPortlet.getChild(UIPopupAction.class) ;
      popupAction.activate(UICalendarCategoryManager.class, 470) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiComponent.getParent()) ;
    }
  }

  static  public class ExportCalendarActionListener extends EventListener<UICalendars> {
    public void execute(Event<UICalendars> event) throws Exception {
      UICalendars uiComponent = event.getSource() ;
      String selectedCalendarId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String calType = event.getRequestContext().getRequestParameter(CALTYPE) ;
      UICalendarPortlet uiCalendarPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
      UIPopupAction popupAction = uiCalendarPortlet.getChild(UIPopupAction.class) ;
      UIExportForm exportForm = popupAction.activate(UIExportForm.class, 500) ;
      exportForm.update(calType, selectedCalendarId) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiComponent.getParent()) ;
    }
  }
  static  public class ExportCalendarsActionListener extends EventListener<UICalendars> {
    public void execute(Event<UICalendars> event) throws Exception {
      UICalendars uiComponent = event.getSource() ;
      String groupId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UICalendarPortlet uiCalendarPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
      UIPopupAction popupAction = uiCalendarPortlet.getChild(UIPopupAction.class) ;
      UIExportForm exportForm = popupAction.activate(UIExportForm.class, 500) ;
      String username = event.getRequestContext().getRemoteUser() ;
      exportForm.initCheckBox(CalendarUtils.getCalendarService().getUserCalendarsByCategory(uiComponent.getSession(), username, groupId), null) ;
      //exportForm.addUIFormInput(arg0)
      //exportForm.update("0", groupId) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiComponent.getParent()) ;
    }
  }
  static  public class ImportCalendarActionListener extends EventListener<UICalendars> {
    public void execute(Event<UICalendars> event) throws Exception {
      UICalendars uiComponent = event.getSource() ;
      UICalendarPortlet uiCalendarPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
      UIPopupAction popupAction = uiCalendarPortlet.getChild(UIPopupAction.class) ;
      popupAction.activate(UIImportForm.class, 600) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiComponent.getParent()) ;
    }
  }

  static  public class GenerateRssActionListener extends EventListener<UICalendars> {
    public void execute(Event<UICalendars> event) throws Exception {
      UICalendars uiComponent = event.getSource() ;
      UICalendarPortlet uiCalendarPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
      UIPopupAction popupAction = uiCalendarPortlet.getChild(UIPopupAction.class) ;
      UIRssForm uiRssForm = popupAction.activate(UIRssForm.class, 600) ;
      uiRssForm.init() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiComponent.getParent()) ;
    }
  }
  static  public class GenerateCalDavActionListener extends EventListener<UICalendars> {
    public void execute(Event<UICalendars> event) throws Exception {
      UICalendars uiComponent = event.getSource() ;
      UICalendarPortlet uiCalendarPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
      UIPopupAction popupAction = uiCalendarPortlet.getChild(UIPopupAction.class) ;
      UICalDavForm uiCalDavForm = popupAction.activate(UICalDavForm.class, 600) ;
      uiCalDavForm.init() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiComponent.getParent()) ;
    }
  }
  static  public class ShareCalendarActionListener extends EventListener<UICalendars> {
    public void execute(Event<UICalendars> event) throws Exception {
      UICalendars uiComponent = event.getSource() ;
      String selectedCalendarId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UICalendarPortlet uiCalendarPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
      UIPopupAction popupAction = uiCalendarPortlet.getChild(UIPopupAction.class) ;
      UIPopupContainer uiPopupContainer = popupAction.activate(UIPopupContainer.class, 400) ;
      uiPopupContainer.setId("UIPermissionSelectPopup") ;
      UISharedForm uiSharedForm = uiPopupContainer.addChild(UISharedForm.class, null, null) ;
      CalendarService calService = CalendarUtils.getCalendarService() ;
      String username = event.getRequestContext().getRemoteUser() ;
      Calendar cal = calService.getUserCalendar(uiComponent.getSession(), username, selectedCalendarId) ;
      uiSharedForm.init(null, cal, true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendarPortlet) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiComponent.getParent()) ;
    }
  }
  static  public class ChangeColorActionListener extends EventListener<UICalendars> {
    public void execute(Event<UICalendars> event) throws Exception {
      System.out.println("\n\n ChangeColorActionListener");
      UICalendars uiComponent = event.getSource() ;
      String calendarId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String color = event.getRequestContext().getRequestParameter(CALCOLOR) ;
      String calType = event.getRequestContext().getRequestParameter(CALTYPE) ;
      CalendarService calService = CalendarUtils.getCalendarService() ;
      SessionProvider session = uiComponent.getSession() ;
      SessionProvider systemSession = uiComponent.getSystemSession() ;
      String username = event.getRequestContext().getRemoteUser() ;
      try{        
        Calendar calendar = null ;
        if(CalendarUtils.PRIVATE_TYPE.equals(calType)) {
          calendar = calService.getUserCalendar(session, username, calendarId) ;
          calendar.setCalendarColor(color) ;
          calService.saveUserCalendar(session, username, calendar, false) ;
        } else if(CalendarUtils.SHARED_TYPE.equals(calType)){
          Iterator iter = calService.getSharedCalendars(systemSession, username, true).getCalendars().iterator() ;
          while (iter.hasNext()) {
            Calendar cal = ((Calendar)iter.next()) ;
            if(cal.getId().equals(calendarId)) {
              calendar = cal ;
              break ;
            }  
          }
          calendar.setCalendarColor(color) ;
          calService.saveSharedCalendar(systemSession, username, calendar) ;
          //calService.save UserCalendar(SessionsUtils.getSessionProvider(), username, calendar, false) ;
        } else if(CalendarUtils.PUBLIC_TYPE.equals(calType)){
          calendar = calService.getGroupCalendar(systemSession, calendarId) ;
          calendar.setCalendarColor(color) ;
          calService.savePublicCalendar(systemSession, calendar, false, username) ;
        }
      } catch (Exception e) {
        e.printStackTrace() ;
      }
      uiComponent.colorMap_.put(calendarId, color) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiComponent.getAncestorOfType(UICalendarWorkingContainer.class)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiComponent.getParent()) ;
    }
  }
  static  public class CalendarSettingActionListener extends EventListener<UICalendars> {
    public void execute(Event<UICalendars> event) throws Exception {
      UICalendars uiComponent = event.getSource() ;
      UICalendarPortlet uiCalendarPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
      UIPopupAction popupAction = uiCalendarPortlet.getChild(UIPopupAction.class) ;
      UICalendarSettingForm uiCalendarSettingForm = popupAction.activate(UICalendarSettingForm.class, 600) ;
      CalendarService cservice = CalendarUtils.getCalendarService() ;
      //String username = Util.getPortalRequestContext().getRemoteUser() ;
      CalendarSetting calendarSetting = uiComponent.getAncestorOfType(UICalendarPortlet.class).getCalendarSetting() ; 
      // = cservice.getCalendarSetting(uiComponent.getSession(), username) ;
      uiCalendarSettingForm.init(calendarSetting, cservice) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiComponent.getParent()) ;
    }
  }
}
