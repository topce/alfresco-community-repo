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
package org.alfresco.repo.action.executer;

import java.util.List;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.OwnableService;

/**
 * Take Ownership action executor
 * 
 * @author Neil Mc Erlean
 * @since 5.0
 */
public class TakeOwnershipActionExecuter extends ActionExecuterAbstractBase
{
    public static final String NAME = "take-ownership";

    private NodeService nodeService;
    private OwnableService ownableService;

    public void setNodeService(NodeService service)
    {
        this.nodeService = service;
    }

    public void setOwnableService(OwnableService service)
    {
        this.ownableService = service;
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        // Intentionally empty. There are no parameters for this action.
    }

    public void executeImpl(Action ruleAction, NodeRef actionedUponNodeRef)
    {
        if (nodeService.exists(actionedUponNodeRef) && isApplicableType(actionedUponNodeRef))
        {
            ownableService.takeOwnership(actionedUponNodeRef);
        }
    }
}
