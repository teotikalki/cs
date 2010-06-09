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
package org.exoplatform.mail.service;


/**
 * Created by The eXo Platform SAS
 * Author : Nam Phung
 *          phunghainam@gmail.com
 * Apr 1, 2008  
 */
public class CheckingInfo {
  public static final int START_CHECKMAIL_STATUS = 101;
  public static final int NO_UPDATE_STATUS = 201;
  public static final int DOWNLOADING_MAIL_STATUS = 150;
  public static final int FINISHED_CHECKMAIL_STATUS = 200;
  public static final int REQUEST_STOP_STATUS = 202;
  public static final int EXECUTING_FILTER = 203;
  public static final int CONNECTION_FAILURE = 102 ;
  public static final int RETRY_PASSWORD = 103 ;
  public static final int COMMON_ERROR = 104 ;
  
  public static final int START_SYNC_FOLDER = 301;
  public static final int FINISH_SYNC_FOLDER = 302;
  public static final int FINISHED_SYNC_FOLDER = 303;
  
  private int totalMsg_ = 0 ;
  private int fetching_ = 0  ;
  private int statusCode_ ;
  private String statusMsg_;
  private String fetchingToFolders_;
  private boolean hasChanged_ ;
  private boolean isRequestStop_ = false;
  private String requestingForFolder_;
  private String msgId_ ;
  private int syncFolderStatus_ =300;
  private StatusInfo status_ = new StatusInfo();
  
  
  public int getTotalMsg() {  return totalMsg_ ; } ;
  public void setTotalMsg(int totalMsg) { 
    totalMsg_ = totalMsg ; 
    hasChanged_ = true ;
  }
  
  public int getFetching() { return fetching_ ; }
  public void setFetching(int in) { 
    fetching_ = in ; 
    hasChanged_ = true ;
  }

  public int getSyncFolderStatus() {  return syncFolderStatus_ ; } ;
  public void setSyncFolderStatus(int syncFolderStatus) { 
    syncFolderStatus_ = syncFolderStatus ; 
    hasChanged_ = true ;
  }
  
  public String getFetchingToFolders() { return fetchingToFolders_; } 
  public void setFetchingToFolders(String fetchingToFolders) {
    fetchingToFolders_ = fetchingToFolders;
  }
  
  public String getStatusMsg() { return statusMsg_ ; }
  
  public void setStatusMsg(String statusMsg) {
    if (statusMsg_ != null && statusMsg_.equals(statusMsg)) {
      return;
    }
    statusMsg_ = statusMsg;
    status_.setStatusMsg(statusMsg_);
    hasChanged_ = true;

  }
  
  public int getStatusCode() { return statusCode_ ; }

  public void setStatusCode(int code) {
    synchronized (this) {
      if (statusCode_ != code) {
        status_.setPreviousStatus(statusCode_);
        statusCode_ = code ; 
        status_.setStatus(statusCode_);
        hasChanged_ = true ;
      }
  
    }
  }
  
  public boolean hasChanged() { return hasChanged_ ; }
  public void setHasChanged(boolean b) { hasChanged_ = b ; }
  /**
   * this function is involved to ask stopping request of user. if returned value is true, 
   * the checking mail job will try to stop.
   * @return
   */
  public boolean isRequestStop() { return isRequestStop_ ; }

  public void setRequestStop(boolean b) {
    synchronized (this) {
      isRequestStop_ = b;
    }
    if (statusCode_ != CheckingInfo.REQUEST_STOP_STATUS) {
      status_.setPreviousStatus(statusCode_);
      statusCode_ = CheckingInfo.REQUEST_STOP_STATUS; 
      status_.setStatus(statusCode_);
      hasChanged_ = true ;
    }
  }
  
  public String getMsgId() { return msgId_ ; }
  public void setMsgId(String msgId) { 
    msgId_ = msgId; 
  }
  
  public String getRequestingForFolder_() { return requestingForFolder_; }
  public void setRequestingForFolder_(String str) { requestingForFolder_ = str ;}
  
  public StatusInfo getStatus() {
    return status_;
  } 
  
  
}
