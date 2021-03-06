/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
 */
package org.exoplatform.contact.webui.popup;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.AddressBook;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.webui.UIAddressBooks;
import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SAS
 * Author : Hung
 *          hung.hoang@exoplatform.com
 * Dec 07, 2010  
 */
@ComponentConfig (
    lifecycle = UIContainerLifecycle.class, 
    events = {
      @EventConfig(listeners = UIPublicAddressPermission.EditActionListener.class),
      @EventConfig(listeners = UIPublicAddressPermission.DeleteActionListener.class, confirm = "UIPublicAddressPermission.msg.confirm-delete")
    }
)

public class UIPublicAddressPermission extends UIContainer implements UIPopupComponent {
  public static String[]  BEAN_FIELD = {"viewPermission","editPermission"} ;
  private static String[] ACTION = {"Edit", "Delete"} ;
  private String groupId_ ;

  public UIPublicAddressPermission() throws Exception {
    this.setName("UIPublicAddressPermission");
    UIGrid permissionList = addChild(UIGrid.class, null, "PermissionList") ;
    permissionList.configure("viewPermission", BEAN_FIELD, ACTION);
    permissionList.getUIPageIterator().setId("PermissionListIterator") ;
    addChild(UISelectPermissionsForm.class, null, null) ;
  }
  public void activate() throws Exception { }
  public void deActivate() throws Exception { }

  public void initGroup(AddressBook group) throws Exception{
    groupId_ = group.getId() ;
    updateGroupGrid(group) ;
    getChild(UISelectPermissionsForm.class).init() ; 
  }
  
  public void processRender(WebuiRequestContext context) throws Exception {
    getChild(UIGrid.class).setRendered(true);
    context.getWriter().append("<span class=\"").append(getId()).append("\" id=\"").append(getId()).append("\">");
    renderChildren(context) ;
    context.getWriter().append("</span>");
  }
  
  public void updateGroupGrid(AddressBook group) throws Exception {
    ContactUtils.updateGroupGrid((UIContainer)this, group);
  }
  
  static public class EditActionListener extends EventListener<UIPublicAddressPermission> {
    public void execute(Event<UIPublicAddressPermission> event) throws Exception {
      UIPublicAddressPermission addEdit = event.getSource();
      String reciever = event.getRequestContext().getRequestParameter(OBJECTID);

      UISelectPermissionsForm shareForm = addEdit.getChild(UISelectPermissionsForm.class);
      UIFormStringInput uiStringInput = shareForm.getUIStringInput(ContactUtils.FIELD_USER) ;
      uiStringInput.setValue(reciever) ;
      AddressBook group = ContactUtils.getContactService()
        .getPublicAddressBook(ContactUtils.getCurrentUser(), addEdit.groupId_) ;
      shareForm.setGroup(group) ;
      if (group.getViewPermissionGroups() != null && Arrays.asList(group.getViewPermissionGroups()).contains(reciever)) {
        shareForm.getUIStringInput(ContactUtils.FIELD_GROUP).setValue(reciever) ;
        shareForm.getUIStringInput(ContactUtils.FIELD_USER).setValue(null) ;
        shareForm.getUIFormCheckBoxInput(ContactUtils.FIELD_EDIT_PERMISSION).setChecked(
            (group.getEditPermissionGroups() != null) && Arrays.asList(group.getEditPermissionGroups()).contains(reciever)) ;
      } else {
        shareForm.getUIStringInput(ContactUtils.FIELD_USER).setValue(reciever) ;
        shareForm.getUIStringInput(ContactUtils.FIELD_GROUP).setValue(null) ;
        shareForm.getUIFormCheckBoxInput(ContactUtils.FIELD_EDIT_PERMISSION).setChecked((group.getEditPermissionUsers()
            != null) && Arrays.asList(group.getEditPermissionUsers()).contains(reciever)) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(shareForm) ;
    }
  }
  
  static public class DeleteActionListener extends EventListener<UIPublicAddressPermission> {
    public void execute(Event<UIPublicAddressPermission> event) throws Exception {
      UIPublicAddressPermission uiForm = event.getSource();
      String remover = event.getRequestContext().getRequestParameter(OBJECTID);   
      ContactService contactService = ContactUtils.getContactService();
      String username = ContactUtils.getCurrentUser() ;
      AddressBook group = contactService.getPublicAddressBook(username, uiForm.groupId_) ;
      if (group.getViewPermissionGroups() != null && Arrays.asList(group.getViewPermissionGroups()).contains(remover)) {
        List<String> newPerms = new ArrayList<String>() ;
        newPerms.addAll(Arrays.asList(group.getViewPermissionGroups())) ;
        newPerms.remove(remover) ;
        group.setViewPermissionGroups(newPerms.toArray(new String[newPerms.size()])) ;
        if(group.getEditPermissionGroups() != null) {
          newPerms.clear() ;
          newPerms.addAll(Arrays.asList(group.getEditPermissionGroups())) ;
          newPerms.remove(remover) ;
          group.setEditPermissionGroups(newPerms.toArray(new String[newPerms.size()])) ;
        }
      } else {
        if(group.getViewPermissionUsers() != null) {
          List<String> newPerms = new ArrayList<String>() ;
          newPerms.addAll(Arrays.asList(group.getViewPermissionUsers())) ;
          newPerms.remove(remover) ;
          group.setViewPermissionUsers(newPerms.toArray(new String[newPerms.size()])) ;
        }
        if(group.getEditPermissionUsers() != null) {
          List<String> newPerms = new ArrayList<String>() ;
          newPerms.addAll(Arrays.asList(group.getEditPermissionUsers())) ;
          newPerms.remove(remover) ;
          group.setEditPermissionUsers(newPerms.toArray(new String[newPerms.size()])) ;
        }
      }
      contactService.savePublicAddressBook(group, false) ;
      uiForm.updateGroupGrid(group);
      event.getRequestContext().addUIComponentToUpdateByAjax(
          uiForm.getAncestorOfType(UIContactPortlet.class).findFirstComponentOfType(UIAddressBooks.class)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getChild(UIGrid.class)) ;
    }
  }
  
}
