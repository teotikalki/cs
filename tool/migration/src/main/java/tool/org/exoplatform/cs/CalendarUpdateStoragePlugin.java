/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package tool.org.exoplatform.cs;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.RepositoryService;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Mar 24, 2009  
 */
public class CalendarUpdateStoragePlugin extends UpdateStorageEventListener {
    //private RepositoryService repositorySerivce_;
    private CsObjectParam csObj_ ;
    
    public CalendarUpdateStoragePlugin(InitParams params, RepositoryService repositorySerivce) throws Exception {
      //repositorySerivce_ = repositorySerivce ;
      csObj_ = (CsObjectParam)params.getObjectParam("cs.calendar.update.object").getObject();
    }
    
    public CsObjectParam getCsObjectParam() {
      return csObj_;
    }
    
    public void preUpdate() {
      System.out.println("======>>> preUpdate");
    }
    
    public void postUpdate() {
      System.out.println("======>>> postUpdate");
    }
  }

