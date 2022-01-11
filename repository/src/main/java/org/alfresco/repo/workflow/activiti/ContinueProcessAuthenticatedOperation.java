/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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

package org.alfresco.repo.workflow.activiti;

import org.activiti.engine.impl.TaskQueryImpl;
import org.activiti.engine.impl.agenda.ContinueProcessOperation;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.task.Task;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.workflow.WorkflowConstants;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

/**
 * @author Damian Ujma
 * @since 7.2
 */
public class ContinueProcessAuthenticatedOperation extends ContinueProcessOperation
{

    private NodeService unprotectedNodeService;

    public ContinueProcessAuthenticatedOperation(CommandContext commandContext, ExecutionEntity execution, NodeService unprotectedNodeService)
    {
        super(commandContext, execution);
        this.unprotectedNodeService = unprotectedNodeService;
        if (unprotectedNodeService == null)
        {
            throw new IllegalArgumentException("NodeService is required");
        }
    }

    @Override public void run()
    {
        if (execution.hasVariable(AuthenticatedTimerJobHandler.AUTHENTICATION_FLAG) && (Boolean) execution.getVariable(
                    AuthenticatedTimerJobHandler.AUTHENTICATION_FLAG))
        {
            execution.removeVariable(AuthenticatedTimerJobHandler.AUTHENTICATION_FLAG);
            String userName = null;
            String tenantToRunIn = (String) execution.getVariable(ActivitiConstants.VAR_TENANT_DOMAIN);
            if (tenantToRunIn != null && tenantToRunIn.trim().length() == 0)
            {
                tenantToRunIn = null;
            }

            final ActivitiScriptNode initiatorNode = (ActivitiScriptNode) execution.getVariable(WorkflowConstants.PROP_INITIATOR);

            // Extracting the properties from the initiatornode should be done in correct tennant or as administrator, since we don't
            // know who started the workflow yet (We can't access node-properties when no valid authentication context is set up).
            if (tenantToRunIn != null)
            {
                userName = TenantUtil.runAsTenant(new TenantUtil.TenantRunAsWork<String>()
                {
                    @Override public String doWork() throws Exception
                    {
                        return getInitiator(initiatorNode);
                    }
                }, tenantToRunIn);
            }
            else
            {
                // No tenant on worklfow, run as admin in default tenant
                userName = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<String>()
                {
                    @SuppressWarnings("synthetic-access") public String doWork() throws Exception
                    {
                        return getInitiator(initiatorNode);
                    }
                }, AuthenticationUtil.getSystemUserName());
            }

            // Fall back to task assignee, if no initiator is found
            if (userName == null)
            {
                // Only try getting active task, if execution timer is waiting on is a userTask
                Task task = new TaskQueryImpl(commandContext).processInstanceId(execution.getProcessInstanceId()).executeSingleResult(commandContext);

                if (task != null && task.getAssignee() != null)
                {
                    userName = task.getAssignee();
                }
            }

            // When no task assignee is set, nor the initiator, use system user to run job
            if (userName == null)
            {
                userName = AuthenticationUtil.getSystemUserName();
                tenantToRunIn = null;
            }

            if (tenantToRunIn != null)
            {
                TenantUtil.runAsUserTenant(new TenantUtil.TenantRunAsWork<Void>()
                {
                    @Override public Void doWork() throws Exception
                    {
                        superRun();
                        return null;
                    }
                }, userName, tenantToRunIn);
            }
            else
            {
                // Execute the timer without tenant
                AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>()
                {
                    @SuppressWarnings("synthetic-access") public Void doWork() throws Exception
                    {
                        superRun();
                        return null;
                    }
                }, userName);
            }
        }
        else
        {
            superRun();
        }

    }

    protected String getInitiator(ActivitiScriptNode initiatorNode)
    {
        if (initiatorNode != null)
        {
            NodeRef ref = initiatorNode.getNodeRef();
            if (unprotectedNodeService.exists(ref))
            {
                return (String) unprotectedNodeService.getProperty(ref, ContentModel.PROP_USERNAME);
            }
        }
        return null;
    }

    private void superRun()
    {
        super.run();
    }
}
