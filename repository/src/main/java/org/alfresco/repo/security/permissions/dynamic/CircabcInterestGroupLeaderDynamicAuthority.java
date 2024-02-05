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
 import org.alfresco.repo.security.permissions.DynamicAuthority;
 import org.alfresco.repo.security.permissions.PermissionReference;
 import org.alfresco.service.cmr.repository.NodeRef;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.InitializingBean;
 
 public class CircabcInterestGroupLeaderDynamicAuthority
   implements DynamicAuthority, InitializingBean {
 
   /** Logger for this class. */
   private static final Logger LOGGER = LoggerFactory.getLogger(
     CircabcInterestGroupLeaderDynamicAuthority.class
   );
 
   private CircabcService circabcService;
 
   public void setCircabcService(CircabcService circabcService) {
     this.circabcService = circabcService;
   }
 
   @Override
   public void afterPropertiesSet() throws Exception {
     if (circabcService == null) {
       throw new IllegalArgumentException("There must be a circabc service");
     }
   }
 
   @Override
   public boolean hasAuthority(NodeRef nodeRef, String userName) {
     LOGGER.info(
       "Checking if user {} has authority for node {}",
       userName,
       nodeRef
     );
     if (!circabcService.isCircabcNode(nodeRef)) {
       LOGGER.info("Node {} is not a circabc node", nodeRef);
       return false;
     }
     CircabcServiceType serviceType = circabcService.findServiceType(nodeRef);
     if (serviceType == CircabcServiceType.UNKNOWN) {
       LOGGER.info("Node {} has unknown service type", nodeRef);
       return false;
     }
     NodeRef group = circabcService.findGroup(nodeRef);
     if (group == null) {
       LOGGER.info("Node {} has no group", nodeRef);
       return false;
     }
     LOGGER.info(
       "Checking if user{} is admin for group {} and service type {} ",
       userName,
       group,
       serviceType
     );
 
     return circabcService.isAdmin(group, userName, serviceType);
   }
 
   @Override
   public String getAuthority() {
     return "ROLE_CIRCABC_LEADER";
   }
 
   @Override
   public Set<PermissionReference> requiredFor() {
     return null;
   }
 }
 