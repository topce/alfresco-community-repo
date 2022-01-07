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

import java.util.List;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.jobexecutor.AsyncContinuationJobHandler;
import org.activiti.engine.impl.jobexecutor.JobHandler;
import org.activiti.engine.impl.jobexecutor.TriggerTimerEventJobHandler;
import org.activiti.engine.impl.variable.SerializableType;
import org.activiti.engine.impl.variable.VariableType;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.autodeployment.AutoDeploymentStrategy;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.workflow.activiti.variable.CustomStringVariableType;
import org.alfresco.service.cmr.repository.NodeService;

/**
 * @author Nick Smith
 * @author Frederik Heremans
 * @since 3.4.e
 */
public class AlfrescoProcessEngineConfiguration extends SpringProcessEngineConfiguration
{
    private List<VariableType> customTypes;
    private NodeService unprotectedNodeService;

    public AlfrescoProcessEngineConfiguration()
    {
        // Make sure the synchornizationAdapter is run before the AlfrescoTransactionSupport (and also before the
        // myBatis synchonisation, which unbinds the neccesairy sqlSession used by the JobFailedListener)
        this.transactionSynchronizationAdapterOrder = AlfrescoTransactionSupport.SESSION_SYNCHRONIZATION_ORDER - 100;
        super.performanceSettings.setValidateExecutionRelationshipCountConfigOnBoot(false);
    }

    @Override
    protected void autoDeployResources(ProcessEngine processEngine)
    {
        if (this.deploymentResources != null && this.deploymentResources.length > 0)
        {
            final AutoDeploymentStrategy strategy = getAutoDeploymentStrategy(deploymentMode);
            strategy.deployResources(deploymentName, deploymentResources, processEngine.getRepositoryService());
        }
    }

    @Override
    public void initVariableTypes()
    {
        super.initVariableTypes();
        // Add custom types before SerializableType
        if (customTypes != null)
        {
            int serializableIndex = variableTypes.getTypeIndex(SerializableType.TYPE_NAME);
            for (VariableType type : customTypes)
            {
                variableTypes.addType(type, serializableIndex);
            }
        }

        // WOR-171: Replace string type by custom one to handle large text-values
        int stringIndex = variableTypes.getTypeIndex("string");
        variableTypes.removeType(variableTypes.getVariableType("string"));
        variableTypes.addType(new CustomStringVariableType(), stringIndex);
    }

    @Override
    public void initAsyncExecutor()
    {
        super.initAsyncExecutor();

        // Wrap timer-job handler to handle authentication
        JobHandler timerJobHandler = jobHandlers.get(TriggerTimerEventJobHandler.TYPE);
        JobHandler wrappingTimerJobHandler = new AuthenticatedTimerJobHandler(timerJobHandler, unprotectedNodeService);
        jobHandlers.put(TriggerTimerEventJobHandler.TYPE, wrappingTimerJobHandler);

        // Wrap async-job handler to handle authentication
        JobHandler asyncJobHandler = jobHandlers.get(AsyncContinuationJobHandler.TYPE);
        JobHandler wrappingAsyncJobHandler = new AuthenticatedAsyncJobHandler(asyncJobHandler);
        jobHandlers.put(AsyncContinuationJobHandler.TYPE, wrappingAsyncJobHandler);

    }

    public void setCustomTypes(List<VariableType> customTypes)
    {
        this.customTypes = customTypes;
    }

    public void setUnprotectedNodeService(NodeService unprotectedNodeService)
    {
        this.unprotectedNodeService = unprotectedNodeService;
    }

    @Override
    public void initDatabaseType()
    {
        databaseTypeMappings.setProperty("MariaDB", DATABASE_TYPE_MYSQL);
        super.initDatabaseType();
    }

    @Override
    public void initAgendaFactory()
    {
        if (this.engineAgendaFactory == null)
        {
            this.engineAgendaFactory = new AlfrescoActivitiEngineAgendaFactory();
        }
    }
}
