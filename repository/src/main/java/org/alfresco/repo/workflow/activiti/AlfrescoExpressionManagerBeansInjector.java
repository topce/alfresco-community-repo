/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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

import org.activiti.engine.delegate.TaskListener;

/**
 * This injector depends on {@link AlfrescoUserTaskBpmnParseHandler}
 * This will add initialized beans to a {@link org.activiti.engine.impl.el.ExpressionManager}
 *
 * @author Damian Ujma
 * @since 7.2
 */
public class AlfrescoExpressionManagerBeansInjector
{
    private AlfrescoProcessEngineConfiguration alfrescoProcessEngineConfiguration;
    private TaskListener completeTaskListener;
    private TaskListener createTaskListener;
    private TaskListener taskNotificationListener;

    public void init()
    {
        if (alfrescoProcessEngineConfiguration != null && alfrescoProcessEngineConfiguration.getExpressionManager() != null
                    && alfrescoProcessEngineConfiguration.getExpressionManager().getBeans() != null)
        {
            alfrescoProcessEngineConfiguration.getExpressionManager().getBeans().put("completeTaskListener", completeTaskListener);
            alfrescoProcessEngineConfiguration.getExpressionManager().getBeans().put("createTaskListener", createTaskListener);
            alfrescoProcessEngineConfiguration.getExpressionManager().getBeans().put("taskNotificationListener", taskNotificationListener);
        }
    }

    public void setAlfrescoProcessEngineConfiguration(AlfrescoProcessEngineConfiguration alfrescoProcessEngineConfiguration)
    {
        this.alfrescoProcessEngineConfiguration = alfrescoProcessEngineConfiguration;
    }

    public void setCompleteTaskListener(TaskListener completeTaskListener)
    {
        this.completeTaskListener = completeTaskListener;
    }

    public void setCreateTaskListener(TaskListener createTaskListener)
    {
        this.createTaskListener = createTaskListener;
    }

    public void setTaskNotificationListener(TaskListener taskNotificationListener)
    {
        this.taskNotificationListener = taskNotificationListener;
    }
}
