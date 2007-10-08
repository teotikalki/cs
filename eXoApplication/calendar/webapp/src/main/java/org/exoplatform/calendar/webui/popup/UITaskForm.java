/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui.popup;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.Attachment;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.service.Reminder;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.calendar.webui.UICalendarViewContainer;
import org.exoplatform.calendar.webui.UIMiniCalendar;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormTabPane;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Pham
 *          tuan.pham@exoplatform.com
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIFormTabPane.gtmpl", 
    events = {
      @EventConfig(listeners = UITaskForm.SaveActionListener.class),
      @EventConfig(listeners = UITaskForm.AddCategoryActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UITaskForm.AddEmailAddressActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UITaskForm.AddAttachmentActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UITaskForm.RemoveAttachmentActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UITaskForm.SelectUserActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UITaskForm.CancelActionListener.class)
    }
)
public class UITaskForm extends UIFormTabPane implements UIPopupComponent, UISelector{
  final public static String TAB_TASKDETAIL = "eventDetail".intern() ;
  final public static String TAB_EVENTREMINDER = "eventReminder".intern() ;
  //final public static String TAB_EVENTSHARE = "eventShare".intern() ;
  //final public static String TAB_EVENTATTENDER = "eventAttender".intern() ;

  //final public static String FIELD_SHARE = "shareEvent".intern() ;
  final public static String FIELD_STATUS = "status".intern() ;
 //final public static String FIELD_MEETING = "meeting".intern() ;
  //final public static String FIELD_PARTICIPANT = "participant".intern() ;

  final public static String ITEM_PUBLIC = "public".intern() ;
  final public static String ITEM_PRIVATE = "private".intern() ;
  final public static String ITEM_AVAILABLE = "available".intern() ;
  final public static String ITEM_BUSY = "busy".intern() ;

  final public static String ITEM_REPEAT = "true".intern() ;
  final public static String ITEM_UNREPEAT = "false".intern() ;

  final public static String ACT_REMOVE = "RemoveAttachment".intern() ;
  final public static String ACT_ADDEMAIL = "AddEmailAddress".intern() ;
  final public static String ACT_ADDCATEGORY = "AddCategory".intern() ;
  final public static String ACT_SELECTUSER = "SelectUser".intern() ;
  
  private boolean isAddNew_ = true ;
  private CalendarEvent calendarEvent_ = null ;
  final public static String TIME_PATTERNS_12 ="hh:mm a" ;
  final public static String TIME_PATTERNS_24 ="HH:mm" ;

  private String errorMsg_ = null ;

  private int timeInterval_ = 15 ;

  public UITaskForm() throws Exception {
    super("UIEventForm", false);
    UITaskDetailTab uiTaskDetailTab =  new UITaskDetailTab(TAB_TASKDETAIL) ;
    addChild(uiTaskDetailTab) ;
    UIEventReminderTab eventReminderTab =  new UIEventReminderTab(TAB_EVENTREMINDER) ;
    addChild(eventReminderTab) ;
    setRenderedChild(TAB_TASKDETAIL) ;
    initForm() ;
  }
  public String getLabel(String id) {
    String label = id ;
    try {
      label = super.getLabel(id) ;
    } catch (Exception e) {
    }
    return label ;
  }
  public void initForm() {
    java.util.Calendar cal = GregorianCalendar.getInstance() ;
    int beginMinute = (cal.get(java.util.Calendar.MINUTE)/timeInterval_)* timeInterval_ ;
    cal.set(java.util.Calendar.MINUTE, beginMinute) ;
    setEventFromDate(cal.getTime()) ;
    cal.add(java.util.Calendar.MINUTE, timeInterval_) ;
    setEventToDate(cal.getTime()) ;
  }
  public void initForm(String calendarId, String categoryId) {
    reset() ;
    setSelectedCalendarId(calendarId) ;
    setSelectedCategory(categoryId) ;
  }
  public void initForm(String calendarId) {
    reset() ;
    setSelectedCalendarId(calendarId) ;
  }
  public void reset() {
    super.reset() ;
    calendarEvent_ = null;
  }
  public void initForm(java.util.Calendar date) {
    reset() ;
    setEventFromDate(date.getTime()) ;
    date.add(java.util.Calendar.MINUTE, 5) ;
    setEventToDate(date.getTime()) ;
  }
  public void initForm(CalendarEvent eventCalendar) throws Exception {
    reset() ;
    if(eventCalendar != null) {
      isAddNew_ = false ;
      calendarEvent_ = eventCalendar ;
      setEventSumary(eventCalendar.getSummary()) ;
      setEventDescription(eventCalendar.getDescription()) ;
      java.util.Calendar cal = java.util.Calendar.getInstance() ;
      cal.setTime(eventCalendar.getFromDateTime()) ;
      setEventFromDate(cal.getTime()) ;
      cal.setTime(eventCalendar.getToDateTime()) ;
      setEventToDate(eventCalendar.getToDateTime()) ;
      setSelectedCalendarId(eventCalendar.getCalendarId()) ;
      setSelectedCategory(eventCalendar.getEventCategoryId()) ;
      setEventDelegation(eventCalendar.getTaskDelegator()) ;
      setSelectedEventPriority(eventCalendar.getPriority()) ;
      setAttachments(eventCalendar.getAttachment()) ;
      setEventReminders(eventCalendar.getReminders()) ;
    } else {
      initForm() ;
    }
  }
  public static List<SelectItemOption<String>> getCategory() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    List<EventCategory> eventCategories = calendarService.getEventCategories(Util.getPortalRequestContext().getRemoteUser()) ;
    for(EventCategory category : eventCategories) {
      options.add(new SelectItemOption<String>(category.getName(), category.getName())) ;
    }
    return options ;
  }

  protected void refreshCategory()throws Exception {
    UIFormInputWithActions taskDetailTab = getChildById(TAB_TASKDETAIL) ;
    taskDetailTab.getUIFormSelectBox(UITaskDetailTab.FIELD_CATEGORY).setOptions(getCategory()) ;
  }

  private List<SelectItemOption<String>> getShareValue() {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    options.add(new SelectItemOption<String>(ITEM_PUBLIC, ITEM_PUBLIC)) ;
    options.add(new SelectItemOption<String>(ITEM_PRIVATE, ITEM_PRIVATE)) ;
    return options ;
  }
  private List<SelectItemOption<String>> getStatusValue() {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    options.add(new SelectItemOption<String>(ITEM_AVAILABLE, ITEM_AVAILABLE)) ;
    options.add(new SelectItemOption<String>(ITEM_BUSY, ITEM_BUSY)) ;
    return options ;
  }

  public String[] getActions() {
    return new String[]{"AddAttachment","Save", "Cancel"} ;
  }
  public void activate() throws Exception {
    // TODO Auto-generated method stub

  }

  public void deActivate() throws Exception {
    // TODO Auto-generated method stub

  }

  public void updateSelect(String selectField, String value) throws Exception {
    // TODO Auto-generated method stub

  }
  protected boolean isEventDetailValid(){
    if(CalendarUtils.isEmpty(getEventSumary())) {
      errorMsg_ = getId() + ".msg.event-summary-required" ;
      return false ;
    }
    if(CalendarUtils.isEmpty(getCalendarId())) {
      errorMsg_ = getId() + ".msg.event-calendar-required" ;
      return false ;
    } 
    if(CalendarUtils.isEmpty(getEventCategory())) {
      errorMsg_ = getId() + ".msg.event-category-required" ;
      return false ;
    }
    if(CalendarUtils.isEmpty(getEventFormDateValue())) {
      errorMsg_ = getId() + ".msg.event-fromdate-required" ;
      return false ;
    } else {
      try {
        getEventFromDate() ;
      } catch (Exception e) {
        e.printStackTrace() ;
        errorMsg_ = getId() + ".msg.event-fromdate-notvalid" ;
        return false ;
      }
    }
    if(!getEventAllDate()) {
      if(CalendarUtils.isEmpty(getEventToDateValue())){
        errorMsg_ = getId() + ".msg.event-todate-required" ;
        return false ;
      } else {
        try {
          getEventToDate() ;
        } catch (Exception e) {
          e.printStackTrace() ;
          errorMsg_ = getId() + ".msg.event-todate-notvalid" ;
          return false ;
        }
        try {
          if(getEventFromDate().after(getEventToDate()) || getEventFromDate().equals(getEventToDate())){
            errorMsg_ = getId() +".msg.event-date-time-logic" ;
            return false ;
          }
        } catch (Exception e) {
          e.printStackTrace() ;
          errorMsg_ = getId() + ".msg.event-date-time-getvalue" ;
          return false ;
        }
      }
    }
    if(getEmailReminder() && CalendarUtils.isEmpty(getEmailAddress())) {
      errorMsg_ = getId() + ".msg.event-email-required" ;
      return false ;
    } 
    errorMsg_ = null ;
    return true ;
  }
  protected String getEventSumary() {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    return taskDetailTab.getUIStringInput(UITaskDetailTab.FIELD_EVENT).getValue() ;
  }
  protected void setEventSumary(String value) {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    taskDetailTab.getUIStringInput(UITaskDetailTab.FIELD_EVENT).setValue(value) ;
  }
  protected String getEventDescription() {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    return taskDetailTab.getUIFormTextAreaInput(UITaskDetailTab.FIELD_DESCRIPTION).getValue() ;
  }
  protected void setEventDescription(String value) {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    taskDetailTab.getUIFormTextAreaInput(UITaskDetailTab.FIELD_DESCRIPTION).setValue(value) ;
  }
  protected String getCalendarId() {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    return taskDetailTab.getUIFormSelectBox(UITaskDetailTab.FIELD_CALENDAR).getValue() ;
  }
  protected void setSelectedCalendarId(String value) {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    taskDetailTab.getUIFormSelectBox(UITaskDetailTab.FIELD_CALENDAR).setValue(value) ;
  }

  protected String getEventCategory() {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    return taskDetailTab.getUIFormSelectBox(UITaskDetailTab.FIELD_CATEGORY).getValue() ;
  }
  protected void setSelectedCategory(String value) {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    taskDetailTab.getUIFormSelectBox(UITaskDetailTab.FIELD_CATEGORY).setValue(value) ;
  }

  protected Date getEventFromDate() throws Exception {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    UIFormSelectBox timeField = taskDetailTab.getUIFormSelectBox(UITaskDetailTab.FIELD_FROM_TIME) ;
    UIFormDateTimeInput fromField = taskDetailTab.getChildById(UITaskDetailTab.FIELD_FROM) ;
    DateFormat df = SimpleDateFormat.getInstance() ;
    return df.parse(fromField.getValue() + " " + timeField.getValue()) ;
  }
  protected String getEventFormDateValue () {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    UIFormDateTimeInput fromField = taskDetailTab.getChildById(UITaskDetailTab.FIELD_FROM) ;
    return fromField.getValue() ;
  }
  protected void setEventFromDate(Date date) {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    UIFormDateTimeInput fromField = taskDetailTab.getChildById(UITaskDetailTab.FIELD_FROM) ;
    UIFormSelectBox timeFile = taskDetailTab.getUIFormSelectBox(UITaskDetailTab.FIELD_FROM_TIME) ;
    DateFormat df = new SimpleDateFormat("MM/dd/yyyy") ;
    fromField.setValue(df.format(date)) ;
    df = new SimpleDateFormat(TIME_PATTERNS_12) ;
    timeFile.setValue(df.format(date)) ;
  }

  protected Date getEventToDate() throws Exception {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    UIFormSelectBox timeField = taskDetailTab.getUIFormSelectBox(UITaskDetailTab.FIELD_TO_TIME) ;
    UIFormDateTimeInput toField = taskDetailTab.getChildById(UITaskDetailTab.FIELD_TO) ;
    DateFormat df = SimpleDateFormat.getInstance() ;
    return df.parse(toField.getValue() + " " + timeField.getValue()) ;
  }
  protected String getEventToDateValue () {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    UIFormDateTimeInput toField = taskDetailTab.getChildById(UITaskDetailTab.FIELD_TO) ;
    return toField.getValue() ;
  }
  protected void setEventToDate(Date date) {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    UIFormDateTimeInput toField = taskDetailTab.getChildById(UITaskDetailTab.FIELD_TO) ;
    UIFormSelectBox timeField = taskDetailTab.getUIFormSelectBox(UITaskDetailTab.FIELD_TO_TIME) ;
    DateFormat df = new SimpleDateFormat("MM/dd/yyyy") ;
    toField.setValue(df.format(date)) ;
    df = new SimpleDateFormat(TIME_PATTERNS_12) ;
    timeField.setValue(df.format(date)) ;
  }

  protected boolean getEventAllDate() {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    return taskDetailTab.getUIFormCheckBoxInput(UITaskDetailTab.FIELD_CHECKALL).isChecked() ;
  }
  protected void setEventAllDate(boolean isCheckAll) {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    taskDetailTab.getUIFormCheckBoxInput(UITaskDetailTab.FIELD_CHECKALL).setChecked(isCheckAll) ;
  }
  protected String getEventDelegation() {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    return taskDetailTab.getUIStringInput(UITaskDetailTab.FIELD_DELEGATION).getValue();
  }
  protected void setEventDelegation(String value) {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    taskDetailTab.getUIStringInput(UITaskDetailTab.FIELD_DELEGATION).setValue(value) ;
  }

  protected boolean getEmailReminder() {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_EVENTREMINDER) ;
    return taskDetailTab.getUIFormCheckBoxInput(UIEventReminderTab.FIELD_EMAIL_REMINDER).isChecked() ;
  }
  protected void setEmailReminder(boolean isChecked) {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_EVENTREMINDER) ;
    taskDetailTab.getUIFormCheckBoxInput(UIEventReminderTab.FIELD_EMAIL_REMINDER).setChecked(isChecked) ;
  }
  protected String getEmailReminderTime() {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_EVENTREMINDER) ;
    return taskDetailTab.getUIStringInput(UIEventReminderTab.FIELD_EMAIL_TIME).getValue() ;
  }
  protected void setEmailReminderTime(String value) {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_EVENTREMINDER) ;
    taskDetailTab.getUIStringInput(UIEventReminderTab.FIELD_EMAIL_TIME).setValue(value) ;
  }

  protected String getEmailAddress() {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_EVENTREMINDER) ;
    return taskDetailTab.getUIStringInput(UIEventReminderTab.FIELD_EMAIL_ADDRESS).getValue() ;
  }

  protected void setEmailAddress(String value) {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_EVENTREMINDER) ;
    taskDetailTab.getUIFormTextAreaInput(UIEventReminderTab.FIELD_EMAIL_ADDRESS).setValue(value) ;
  }

  protected boolean getPopupReminder() {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_EVENTREMINDER) ;
    return taskDetailTab.getUIFormCheckBoxInput(UIEventReminderTab.FIELD_POPUP_REMINDER).isChecked() ;
  }
  protected void setPopupReminder(boolean isChecked) {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_EVENTREMINDER) ;
    taskDetailTab.getUIFormCheckBoxInput(UIEventReminderTab.FIELD_POPUP_REMINDER).setChecked(isChecked) ;
  }
  protected String getPopupReminderTime() {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_EVENTREMINDER) ;
    return taskDetailTab.getUIStringInput(UIEventReminderTab.FIELD_POPUP_TIME).getValue() ;
  }

  protected void setPopupReminderTime(String value) {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_EVENTREMINDER) ;
    taskDetailTab.getUIStringInput(UIEventReminderTab.FIELD_POPUP_TIME).setValue(value) ;
  }
  protected long getPopupReminderSnooze() {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_EVENTREMINDER) ;
    try {
      String time =  taskDetailTab.getUIFormSelectBox(UIEventReminderTab.FIELD_SNOOZE_TIME).getValue() ;
      return Long.parseLong(time) ;
    } catch (Exception e){
      e.printStackTrace() ;
    }
    return 0 ;
  }
  protected void setPopupReminderSnooze(long value) {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_EVENTREMINDER) ;
    taskDetailTab.getUIFormSelectBox(UIEventReminderTab.FIELD_SNOOZE_TIME).setValue(String.valueOf(value)) ;
  }
  protected List<Attachment>  getAttachments(String eventId, boolean isAddNew) {
    UITaskDetailTab taskDetailTab = getChild(UITaskDetailTab.class) ;
    return taskDetailTab.getAttachments() ;
  }
  protected void setAttachments(List<Attachment> attachment) throws Exception {
    UITaskDetailTab taskDetailTab = getChild(UITaskDetailTab.class) ;
    taskDetailTab.setAttachments(attachment) ;
    taskDetailTab.refreshUploadFileList() ;
  }
  protected void setEventReminders(List<Reminder> reminders){
    for(Reminder r : reminders) {
      if(Reminder.TYPE_EMAIL.equals(r.getReminder())) {
        setEmailReminder(true) ;
        setEmailAddress(r.getEmailAddress()) ;
        setEmailReminderTime(r.getAlarmBefore()) ; 
      }else {
        setPopupReminder(true) ;
        setPopupReminderTime(r.getAlarmBefore()) ;
        setPopupReminderSnooze(r.getSnooze()) ;
      }
    }
  }
  protected List<Reminder>  getEventReminders() {
    List<Reminder> reminders = new ArrayList<Reminder>() ;
    if(isAddNew_) {
      if(getEmailReminder()) { 
        Reminder email = new Reminder(Reminder.TYPE_EMAIL) ;
        email.setAlarmBefore(getEmailReminderTime()) ;
        email.setEmailAddress(getEmailAddress()) ;
        reminders.add(email) ;
      }
      if(getPopupReminder()) {
        Reminder popup = new Reminder(Reminder.TYPE_POPUP) ;
        popup.setAlarmBefore(getEmailReminderTime()) ;
        popup.setSnooze(getPopupReminderSnooze()) ;
        reminders.add(popup) ;
      }
    } else {
      if(getEmailReminder()) {
        for(Reminder r : calendarEvent_.getReminders()) {
          if(Reminder.TYPE_EMAIL.equals(r.getReminder())) {
            Reminder email = r ;
            email.setAlarmBefore(getEmailReminderTime()) ;
            email.setEmailAddress(getEmailAddress()) ;
            reminders.add(email) ;
          } 
        }
      }
      if(getPopupReminder()) {
        for(Reminder r : calendarEvent_.getReminders()) {
          if(Reminder.TYPE_POPUP.equals(r.getReminder())) {
            Reminder popup = r ;
            popup.setAlarmBefore(getEmailReminderTime()) ;
            popup.setSnooze(getPopupReminderSnooze()) ;
            reminders.add(popup) ;
          } 
        }
      }
    }
    return reminders ;
  }

  protected String getEventPriority() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_TASKDETAIL) ;
    return eventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_PRIORITY).getValue() ;
  }
  protected void setSelectedEventPriority(String value) {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_TASKDETAIL) ;
    eventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_PRIORITY).setValue(value) ;
  }

  static  public class AddCategoryActionListener extends EventListener<UITaskForm> {
    public void execute(Event<UITaskForm> event) throws Exception {
      UITaskForm uiForm = event.getSource() ;
      System.out.println("\n\n AddCategoryActionListener");
      UIPopupContainer uiContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiChildPopup = uiContainer.getChild(UIPopupAction.class) ;
      uiChildPopup.activate(UIEventCategoryManager.class, 500) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;
    }
  }
  static  public class AddEmailAddressActionListener extends EventListener<UITaskForm> {
    public void execute(Event<UITaskForm> event) throws Exception {
      System.out.println("\n\n AddEmailAddressActionListener");
      /*UIEventForm uiForm = event.getSource() ;
      UIPopupContainer uiPopupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiPopupAction  = uiPopupContainer.getChild(UIPopupAction.class) ;
      UIAddEmailAddress uiEmailAddressFrom = uiPopupAction.activate(UIAddEmailAddress.class, 500) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupContainer) ;*/
    }
  }
  static  public class AddAttachmentActionListener extends EventListener<UITaskForm> {
    public void execute(Event<UITaskForm> event) throws Exception {
      UITaskForm uiForm = event.getSource() ;
      //UIPopupAction uiParentPopup = uiForm.getAncestorOfType(UIPopupAction.class) ;
      UIPopupContainer uiContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiChildPopup = uiContainer.getChild(UIPopupAction.class) ;
      uiChildPopup.activate(UIAttachFileForm.class, 500) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;
    }
  }
  static  public class RemoveAttachmentActionListener extends EventListener<UITaskForm> {
    public void execute(Event<UITaskForm> event) throws Exception {
      UITaskForm uiForm = event.getSource() ;
      UIEventDetailTab uiEventDetailTab = uiForm.getChild(UIEventDetailTab.class) ;
      String attFileId = event.getRequestContext().getRequestParameter(OBJECTID);
      Attachment attachfile = new Attachment();
      for (Attachment att : uiEventDetailTab.attachments_) {
        if (att.getId().equals(attFileId)) {
          attachfile = (Attachment) att;
        }
      }
      uiEventDetailTab.removeFromUploadFileList(attachfile);
      uiEventDetailTab.refreshUploadFileList() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
    }
  }
  static  public class AddCalendarActionListener extends EventListener<UITaskForm> {
    public void execute(Event<UITaskForm> event) throws Exception {
      UITaskForm uiForm = event.getSource() ;
      System.out.println( "\n\n ==========> AddCalendarActionListener");
    }
  }

  static  public class SelectUserActionListener extends EventListener<UITaskForm> {
    public void execute(Event<UITaskForm> event) throws Exception {
      UITaskForm uiForm = event.getSource() ;
      System.out.println( "\n\n ==========> AddParticipantActionListener");
    }
  }

  static  public class SaveActionListener extends EventListener<UITaskForm> {
    public void execute(Event<UITaskForm> event) throws Exception {
      UITaskForm uiForm = event.getSource() ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      UICalendarViewContainer uiViewContainer = calendarPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
      if(uiForm.isEventDetailValid()) {
        CalendarService calendarService = uiForm.getApplicationComponent(CalendarService.class) ;
        String username = event.getRequestContext().getRemoteUser() ;
        String calendarId = uiForm.getCalendarId() ;
        CalendarEvent calendarEvent = new CalendarEvent() ;
        if(!uiForm.isAddNew_){
          calendarEvent = uiForm.calendarEvent_ ; 
        }
        calendarEvent.setEventType(CalendarEvent.TYPE_TASK) ;
        calendarEvent.setSummary(uiForm.getEventSumary()) ;
        calendarEvent.setDescription(uiForm.getEventDescription()) ;
        Date from = uiForm.getEventFromDate() ;
        Date to = uiForm.getEventToDate() ;
        if(uiForm.getEventAllDate()) {
          Calendar cal = Calendar.getInstance() ;
          cal.setTime(from) ;
          cal.set(Calendar.HOUR, 0) ;
          from = cal.getTime() ;
          cal.add(Calendar.DATE, 1) ;
          to = cal.getTime() ;
        }
        calendarEvent.setFromDateTime(from) ;
        calendarEvent.setToDateTime(to);
        
        calendarEvent.setCalendarId(calendarId) ;
        calendarEvent.setEventCategoryId(uiForm.getEventCategory()) ;
        calendarEvent.setLocation(uiForm.getEventDelegation()) ;
       // calendarEvent.setRepeatType(uiForm.getEventRepeat()) ;
        calendarEvent.setPriority(uiForm.getEventPriority()) ; 
        //calendarEvent.setPrivate(UITaskForm.ITEM_PRIVATE.equals(uiForm.getShareType())) ;
        //calendarEvent.setEventState(uiForm.getEventState()) ;
        calendarEvent.setAttachment(uiForm.getAttachments(calendarEvent.getId(), uiForm.isAddNew_)) ;
        calendarEvent.setReminders(uiForm.getEventReminders()) ;
       // if(uiForm.getMeetingInvitation() != null) calendarEvent.setInvitation(uiForm.getMeetingInvitation()) ;
        //if(uiForm.getParticipant() != null) calendarEvent.setParticipant(uiForm.getParticipant()) ;
        try {
          calendarService.saveUserEvent(username, calendarId, calendarEvent, uiForm.isAddNew_) ;
          uiViewContainer.refresh() ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiViewContainer) ;
          UIMiniCalendar uiMiniCalendar = calendarPortlet.findFirstComponentOfType(UIMiniCalendar.class) ;
          uiMiniCalendar.refresh() ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiMiniCalendar) ;
          uiForm.getAncestorOfType(UIPopupAction.class).deActivate() ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIPopupAction.class)) ;
          if(uiForm.isAddNew_) {
            uiApp.addMessage(new ApplicationMessage(uiForm.getId() + ".msg.add-event-successfully", null));
          } else {
            uiApp.addMessage(new ApplicationMessage(uiForm.getId() + ".msg.update-event-successfully", null));
          }
          uiForm.reset() ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        }catch (Exception e) {
          uiApp.addMessage(new ApplicationMessage(uiForm.getId() + ".msg.add-event-error", null));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          e.printStackTrace() ;
        }
      } else {
        uiApp.addMessage(new ApplicationMessage(uiForm.errorMsg_, null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        uiForm.setRenderedChild(TAB_TASKDETAIL) ;
      }
    }
  }
  static  public class CancelActionListener extends EventListener<UITaskForm> {
    public void execute(Event<UITaskForm> event) throws Exception {
      UITaskForm uiForm = event.getSource() ;
      UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIPopupAction.class);
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
}
