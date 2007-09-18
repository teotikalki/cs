/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui ;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.service.MessageHeader;
import org.exoplatform.mail.service.Tag;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.popup.UIComposeForm;
import org.exoplatform.mail.webui.popup.UIPopupAction;
import org.exoplatform.mail.webui.popup.UIPopupActionContainer;
import org.exoplatform.mail.webui.popup.UITagForm;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
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
    template =  "app:/templates/mail/webui/UIMessageList.gtmpl",
    events = {
        @EventConfig(listeners = UIMessageList.SelectMessageActionListener.class),
        @EventConfig(listeners = UIMessageList.AddStarActionListener.class),
        @EventConfig(listeners = UIMessageList.RemoveStarActionListener.class),
        @EventConfig(listeners = UIMessageList.ReplyActionListener.class),
        @EventConfig(listeners = UIMessageList.ReplyAllActionListener.class),
        @EventConfig(listeners = UIMessageList.ForwardActionListener.class), 
        @EventConfig(listeners = UIMessageList.MarkAsReadActionListener.class),
        @EventConfig(listeners = UIMessageList.MarkAsUnReadActionListener.class),
        @EventConfig(listeners = UIMessageList.AddStarActionListener.class),
        @EventConfig(listeners = UIMessageList.RemoveStarActionListener.class),
        @EventConfig(listeners = UIMessageList.AddTagActionListener.class),
        @EventConfig(listeners = UIMessageList.MoveMessagesActionListener.class),
        @EventConfig(listeners = UIMessageList.ImportActionListener.class),
        @EventConfig(listeners = UIMessageList.ExportActionListener.class)
    }
)

public class UIMessageList extends UIForm {
  private String selectedMessageId_ = null ;
  private String selectedFolderId_ = Utils.FD_INBOX ;
  private String selectedTagName_ = null ;
  private Map<String, Message> messageMap_ = new HashMap<String, Message>(); 

  public UIMessageList() throws Exception {}

  protected String getSelectedMessageId() {return selectedMessageId_ ;}
  protected void setSelectedMessageId(String messageId) {selectedMessageId_ = messageId ;}
  
  protected String getSelectedFolderId() {return selectedFolderId_ ;}
  protected void setSelectedFolderId(String folderId) {selectedFolderId_ = folderId ;}
  
  protected String getSelectedTagName() {return selectedTagName_ ;}
  protected void setSelectedTagName(String tagName) {selectedTagName_ = tagName ;}
  
  public void addCheckboxForMessages() throws Exception {
    for (Message msg : getMessageList()) {
      UIFormCheckBoxInput<Boolean> checkbox = getChildById(msg.getId());
      if (checkbox != null) removeChild(getUIFormCheckBoxInput(msg.getId()).getClass());
      addChild(new UIFormCheckBoxInput<Boolean>(msg.getId(), msg.getId(), null));
    }
  }
  
  public void setMessageList(List<Message> messageList) throws Exception {
    getChildren().clear();
    messageMap_.clear();
    for (Message message : messageList) {
      addUIFormInput(new UIFormCheckBoxInput<Boolean>(message.getId(), message.getId(), false));
      messageMap_.put(message.getId(), message);
    }
  }
  
  public void updateMessage(Message message, boolean isNew) throws Exception {
    if (isNew) addUIFormInput(new UIFormCheckBoxInput<Boolean>(message.getId(), message.getId(), false));
    messageMap_.put(message.getId(), message);
  }
  
  public void removeMessages(List<Message> messages) throws Exception {
    for (Message message : messages) {
      messageMap_.remove(message.getId());
      removeChild(getUIFormCheckBoxInput(message.getId()).getClass());
    }
  }
  
  public List<Message> getMessageList() throws Exception {
    return new ArrayList<Message>(messageMap_.values());
  }
  
  public List<String> getCheckedMessage() throws Exception {
    List<String> messageList = new ArrayList<String>();
    for (Message msg : getMessageList()) {
      UIFormCheckBoxInput<Boolean> uiCheckbox = getChildById(msg.getId());
      if (uiCheckbox != null && uiCheckbox.isChecked()) {
        messageList.add(msg.getId());
      }
    }
    return messageList;
  }
  
  static public class SelectMessageActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIMessageList uiMessageList = event.getSource();
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class);
      UIMessagePreview uiMessagePreview = uiPortlet.findFirstComponentOfType(UIMessagePreview.class);
      UIMessageArea uiMessageArea = uiPortlet.findFirstComponentOfType(UIMessageArea.class);
      UIFolderContainer uiFolderContainer = uiPortlet.findFirstComponentOfType(UIFolderContainer.class);
      String username = uiPortlet.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      MailService mailServ = uiPortlet.getApplicationComponent(MailService.class);
      Message msg = mailServ.getMessageById(username, msgId, accountId);
      uiMessageList.setSelectedMessageId(msgId);
      msg.setUnread(false);
      uiMessagePreview.setMessage(msg);
      mailServ.saveMessage(username, accountId, msg, false);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageArea);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiFolderContainer);
    }
  }
  
  static public class AddStarActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;      
    }
  }
  static public class RemoveStarActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;      
    }
  }

  static public class ReplyActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource() ; 
      System.out.println(" =========== > Reply Action");
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class) ;
      UINavigationContainer uiNavigation = uiPortlet.getChild(UINavigationContainer.class) ;
      UISelectAccount uiSelect = uiNavigation.getChild(UISelectAccount.class) ;
      String accId = uiSelect.getSelectedValue() ;
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
      UIPopupActionContainer uiPopupContainer = uiPopupAction.activate(UIPopupActionContainer.class, 1000) ;
      
      UIComposeForm uiComposeForm = uiPopupContainer.createUIComponent(UIComposeForm.class, null, null);
      List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
      MailService mailSvr = uiMessageList.getApplicationComponent(MailService.class) ;
      String username = uiPortlet.getCurrentUser() ;
      if (uiMessageList.getSelectedMessageId() != null) {
        String messageId = uiMessageList.getSelectedMessageId();
        Message message = mailSvr.getMessageById(username, messageId, accId);
        uiComposeForm.setFieldSubjectValue("Re: " + message.getSubject());
        uiComposeForm.setFieldToValue(Utils.formatAddress(message.getFrom()));
        uiComposeForm.setFieldMessageContentValue(message.getMessageBody());
      }
      for(Account acc : mailSvr.getAccounts(username)) {
        SelectItemOption<String> itemOption = new SelectItemOption<String>(acc.getUserDisplayName() + " &lt;" + acc.getEmailAddress() + 
            "&gt;", acc.getUserDisplayName() + "<" + acc.getEmailAddress() + ">");
        if (acc.getId() == accId) itemOption.setSelected(true);
        options.add(itemOption) ;
      }
      uiComposeForm.setFieldFromValue(options) ;
      uiPopupContainer.addChild(uiComposeForm) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
  
  static  public class ReplyAllActionListener extends EventListener<UIMessageList> {    
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource() ; 
      System.out.println(" =========== > Reply All Action");
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class) ;
      UINavigationContainer uiNavigation = uiPortlet.getChild(UINavigationContainer.class) ;
      UISelectAccount uiSelect = uiNavigation.getChild(UISelectAccount.class) ;
      String accId = uiSelect.getSelectedValue() ;
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
      UIPopupActionContainer uiPopupContainer = uiPopupAction.activate(UIPopupActionContainer.class, 1000) ;
      
      UIComposeForm uiComposeForm = uiPopupContainer.createUIComponent(UIComposeForm.class, null, null);
      List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
      MailService mailSvr = uiMessageList.getApplicationComponent(MailService.class) ;
      String username = uiPortlet.getCurrentUser() ;
      if (uiMessageList.getSelectedMessageId() != null) {
        String messageId = uiMessageList.getSelectedMessageId();
        Message message = mailSvr.getMessageById(username, messageId, accId);
        uiComposeForm.setFieldSubjectValue("Re: " + message.getSubject());
        uiComposeForm.setFieldToValue(Utils.formatAddress(message.getFrom() + "," + 
            message.getMessageCc() + "," + message.getMessageBcc()));
        uiComposeForm.setFieldMessageContentValue(message.getMessageBody());
      }
      for(Account acc : mailSvr.getAccounts(username)) {
        SelectItemOption<String> itemOption = new SelectItemOption<String>(acc.getUserDisplayName() + " &lt;" + acc.getEmailAddress() + 
            "&gt;", acc.getUserDisplayName() + "<" + acc.getEmailAddress() + ">");
        if (acc.getId() == accId) itemOption.setSelected(true);
        options.add(itemOption) ;
      }
      uiComposeForm.setFieldFromValue(options) ;
      uiPopupContainer.addChild(uiComposeForm) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
     
  static public class ForwardActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource() ; 
      System.out.println(" =========== > Forward Action");
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class) ;
      UINavigationContainer uiNavigation = uiPortlet.getChild(UINavigationContainer.class) ;
      UISelectAccount uiSelect = uiNavigation.getChild(UISelectAccount.class) ;
      String accId = uiSelect.getSelectedValue() ;
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
      UIPopupActionContainer uiPopupContainer = uiPopupAction.activate(UIPopupActionContainer.class, 1000) ;
      
      UIComposeForm uiComposeForm = uiPopupContainer.createUIComponent(UIComposeForm.class, null, null);
      List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
      MailService mailSvr = uiMessageList.getApplicationComponent(MailService.class) ;
      String username = uiPortlet.getCurrentUser() ;
      if (uiMessageList.getSelectedMessageId() != null) {
        String messageId = uiMessageList.getSelectedMessageId();
        Message message = mailSvr.getMessageById(username, messageId, accId);
        uiComposeForm.setFieldSubjectValue("Fwd: " + message.getSubject());
        String forwardedText = "\n\n\n-------- Original Message --------\n" +
            "Subject: " + message.getSubject() + "\nDate: " + message.getSendDate() + 
            "\nFrom: " + Utils.formatAddress(message.getFrom()) + 
            "\nTo: " + Utils.formatAddress(message.getMessageTo()) + 
            "\n\n" + message.getMessageBody();         
        message.setMessageBody(forwardedText);
        uiComposeForm.setFieldMessageContentValue(message.getMessageBody());
      }
      for(Account acc : mailSvr.getAccounts(username)) {
        SelectItemOption<String> itemOption = new SelectItemOption<String>(acc.getUserDisplayName() + " &lt;" + acc.getEmailAddress() + 
            "&gt;", acc.getUserDisplayName() + "<" + acc.getEmailAddress() + ">");
        if (acc.getId() == accId) itemOption.setSelected(true);
        options.add(itemOption) ;
      }
      uiComposeForm.setFieldFromValue(options) ;
      uiPopupContainer.addChild(uiComposeForm) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }  
  
  static public class MarkAsReadActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiActionBar = event.getSource() ;      
    }
  }
  
  static public class MarkAsUnReadActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiActionBar = event.getSource() ;      
    }
  }

  static public class AddTagActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource() ; 
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class);
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class);
      UITagForm uiTagForm = uiMessageList.createUIComponent(UITagForm.class, null, null) ;
      String username = uiPortlet.getCurrentUser();
      MailService mailService = uiMessageList.getApplicationComponent(MailService.class);
      UINavigationContainer uiNavigation = uiPortlet.getChild(UINavigationContainer.class) ;
      UISelectAccount uiSelect = uiNavigation.getChild(UISelectAccount.class) ;
      String accId = uiSelect.getSelectedValue() ;
      List<Tag> listTags = mailService.getTags(username, accId);
      uiTagForm.createCheckBoxTagList(listTags) ;
      for (String msgId : uiMessageList.getCheckedMessage()) {
        uiTagForm.messageMap.put(msgId, msgId); 
      }     
      uiPopupAction.activate(uiTagForm, 600, 0, true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
    }
  }
  
  static public class MoveMessagesActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiActionBar = event.getSource() ;      
    }
  }
  
  static public class ImportActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiActionBar = event.getSource() ;      
    }
  }
  
  static public class ExportActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiActionBar = event.getSource() ;      
    }
  }
}