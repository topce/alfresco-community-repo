/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

 package org.alfresco.repo.security.permissions.dynamic;

 import java.util.Set;
 import org.alfresco.repo.security.authentication.AuthenticationUtil;
 import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
 import org.alfresco.repo.security.permissions.DynamicAuthority;
 import org.alfresco.repo.security.permissions.PermissionReference;
 import org.alfresco.service.cmr.repository.NodeRef;
 import org.alfresco.service.cmr.security.OwnableService;
 import org.alfresco.service.cmr.security.PermissionService;
 import org.alfresco.util.EqualsHelper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.InitializingBean;
 
 public class CircabcOwnerDynamicAuthority
   implements DynamicAuthority, InitializingBean {
 
   private static final Logger LOGGER = LoggerFactory.getLogger(
     CircabcOwnerDynamicAuthority.class
   );
 
   private OwnableService ownableService;
   private CircabcService circabcService;
 
   public void setCircabcService(CircabcService circabcService) {
     this.circabcService = circabcService;
   }
 
   public void setOwnableService(OwnableService ownableService) {
     this.ownableService = ownableService;
   }
 
   @Override
   public void afterPropertiesSet() throws Exception {
     if (ownableService == null) {
       throw new IllegalArgumentException("There must be an ownable service");
     }
     if (circabcService == null) {
       throw new IllegalArgumentException("There must be a circabc service");
     }
   }
 
   /**
    * Checks if the user has authority for a given node.
    *
    * @param  nodeRef    the reference to the node
    * @param  userName   the name of the user
    * @return            true if the user is owner of the node and still member of interest group, false otherwise
    */
   @Override
   public boolean hasAuthority(NodeRef nodeRef, String userName) {
     LOGGER.info(
       "Checking if user {} has authority for node {}",
       userName,
       nodeRef
     );
     if (!circabcService.isCircabcNode(nodeRef)) {
       LOGGER.info("Node {} is not a circabc node", nodeRef);
       return isOwner(nodeRef, userName);
     }
     NodeRef group = circabcService.findGroup(nodeRef);
     if (group == null) {
       LOGGER.info("Node {} has no group", nodeRef);
       return isOwner(nodeRef, userName);
     } else {
       if (circabcService.isGroupMember(group, userName)) {
         LOGGER.info("User {} is a member of group {}", userName, group);
         return isOwner(nodeRef, userName);
       } else {
         LOGGER.info("User {} is not a member of group {}", userName, group);
         return false;
       }
     }
   }
 
   private boolean isOwner(NodeRef nodeRef, String userName) {
     return AuthenticationUtil.runAs(
       new RunAsWork<Boolean>() {
         public Boolean doWork() throws Exception {
           return EqualsHelper.nullSafeEquals(
             ownableService.getOwner(nodeRef),
             userName
           );
         }
       },
       AuthenticationUtil.getSystemUserName()
     );
   }
 
   @Override
   public String getAuthority() {
     return PermissionService.OWNER_AUTHORITY;
   }
 
   @Override
   public Set<PermissionReference> requiredFor() {
     return null;
   }
 }
 