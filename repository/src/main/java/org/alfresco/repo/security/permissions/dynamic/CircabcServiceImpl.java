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

import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

public class CircabcServiceImpl implements CircabcService {

  private NodeService nodeService;
  private MultilingualContentService multilingualContentService;

  public void setMultilingualContentService(
    MultilingualContentService multilingualContentService
  ) {
    this.multilingualContentService = multilingualContentService;
  }

  private CircabcDAO circabcDAO;

  public void setCircabcDAO(CircabcDAO circabcDAO) {
    this.circabcDAO = circabcDAO;
  }

  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  @Override
  public NodeRef findGroup(NodeRef nodeRef) {
    return AuthenticationUtil.runAs(
      new RunAsWork<NodeRef>() {
        public NodeRef doWork() throws Exception {
          NodeRef result = null;
          NodeRef tempNodeRef = nodeRef;
          for (;;) {
            if (tempNodeRef == null) {
              break;
            }

            if (
              nodeService
                .getType(tempNodeRef)
                .equals(ContentModel.TYPE_MULTILINGUAL_CONTAINER)
            ) {
              tempNodeRef =
                multilingualContentService.getPivotTranslation(tempNodeRef);
            }

            if (
              nodeService.hasAspect(tempNodeRef, CircabcModel.ASPECT_IGROOT)
            ) {
              result = tempNodeRef;
              break;
            } else {
              tempNodeRef =
                nodeService.getPrimaryParent(tempNodeRef).getParentRef();
            }
          }
          return result;
        }
      },
      AuthenticationUtil.getSystemUserName()
    );
  }

  @Override
  public boolean isGroupMember(NodeRef group, String userName) {
    List<CircabcPermission> permissions = circabcDAO.getGroupPerrmission(
      group.toString(),
      userName
    );

    if (permissions != null && !permissions.isEmpty()) {
      return true;
    }
    return false;
  }

  @Override
  public boolean isAdmin(
    NodeRef group,
    String userName,
    CircabcServiceType serviceType
  ) {
    List<CircabcPermission> permissions = circabcDAO.getGroupPerrmission(
      group.toString(),
      userName
    );

    for (CircabcPermission permission : permissions) {
      if (
        serviceType == CircabcServiceType.LIBRARY &&
        permission.getLibraryPermission().equals("LibAdmin")
      ) {
        return true;
      }
      if (
        serviceType == CircabcServiceType.NEWSGROUP &&
        permission.getNewsGroupPermission().equals("NwsAdmin")
      ) {
        return true;
      }
      if (
        serviceType == CircabcServiceType.INFORMATION &&
        permission.getInformationPermission().equals("InfAdmin")
      ) {
        return true;
      }
    }

    return false;
  }

  @Override
  public boolean isCircabcNode(NodeRef nodeRef) {
    return AuthenticationUtil.runAs(
      new RunAsWork<Boolean>() {
        public Boolean doWork() throws Exception {
          return nodeService.hasAspect(
            nodeRef,
            CircabcModel.ASPECT_CIRCABC_MANAGEMENT
          );
        }
      },
      AuthenticationUtil.getSystemUserName()
    );
  }

  @Override
  public CircabcServiceType findServiceType(NodeRef nodeRef) {
    return AuthenticationUtil.runAs(
      new RunAsWork<CircabcServiceType>() {
        public CircabcServiceType doWork() throws Exception {
          CircabcServiceType result = CircabcServiceType.UNKNOWN;

          if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)) {
            result = CircabcServiceType.LIBRARY;
          } else if (
            nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_NEWSGROUP)
          ) {
            result = CircabcServiceType.NEWSGROUP;
          } else if (
            nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_INFORMATION)
          ) {
            result = CircabcServiceType.INFORMATION;
          }
          return result;
        }
      },
      AuthenticationUtil.getSystemUserName()
    );
  }
}
