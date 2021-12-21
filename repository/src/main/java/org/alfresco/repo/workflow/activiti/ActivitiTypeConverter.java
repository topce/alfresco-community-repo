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

package org.alfresco.repo.workflow.activiti;

import static org.alfresco.repo.workflow.activiti.ActivitiConstants.DEFAULT_TRANSITION_DESCRIPTION;
import static org.alfresco.repo.workflow.activiti.ActivitiConstants.DEFAULT_TRANSITION_NAME;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Optional;

import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntityImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.workflow.WorkflowObjectFactory;
import org.alfresco.repo.workflow.activiti.properties.ActivitiPropertyConverter;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowDeployment;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowNode;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.cmr.workflow.WorkflowTransition;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.collections.Function;

/**
 * @author Nick Smith
 * @author Frederik Heremans
 * @since 3.4.e
 */
public class ActivitiTypeConverter
{
    private static final String TRANSITION_SUFFIX= ".transition";
    private static final String DEFAULT_TRANSITION_KEY= "bpm_businessprocessmodel.transition";
    
    private final RuntimeService runtimeService;
    private final RepositoryService repositoryService;
    private final HistoryService historyService;
    private final ActivitiPropertyConverter propertyConverter;
    private final WorkflowObjectFactory factory;

    private final ActivitiUtil activitiUtil;
    
    public ActivitiTypeConverter(ProcessEngine processEngine, 
                WorkflowObjectFactory factory,
                ActivitiPropertyConverter propertyConverter, boolean deployWorkflowsInTenant)
    {
        this.runtimeService = processEngine.getRuntimeService();
        this.historyService = processEngine.getHistoryService();
        this.factory = factory;
        this.repositoryService = processEngine.getRepositoryService();
        this.propertyConverter =propertyConverter;
        this.activitiUtil = new ActivitiUtil(processEngine, deployWorkflowsInTenant);
    }
    
    public <F, T> List<T> filterByDomainAndConvert(List<F> values, Function<F, String> processKeyGetter)
    {
        List<F> filtered = factory.filterByDomain(values, processKeyGetter);
        return convert(filtered);
    }

    /**
     * filters HistoricProcessInstances by domain
     * including all instances for default domain
     * and excluding shared instances (THOR-206) for tenants 
     * 
     * @return List
     */
    public <F, T> List<T> doSpecialTenantFilterAndSafeConvert(List<F> values, Function<F, String> processKeyGetter)
    {
        List<F> filtered = factory.doSpecialTenantFilter(values, processKeyGetter);
        factory.setIgnoreTenantCheck(factory.isDefaultDomain());
        try
        {
            return convert(filtered, true);
        }
        finally
        {
            factory.setIgnoreTenantCheck(false);
        }
        
    }

    /**
     * Convert a {@link Deployment} into a {@link WorkflowDeployment}.
     * @param deployment Deployment
     * @return WorkflowDeployment
     */
    public WorkflowDeployment convert(Deployment deployment)
    {
        if(deployment == null)
            return null;
        
        ProcessDefinition processDef = activitiUtil.getProcessDefinitionForDeployment(deployment.getId());
        WorkflowDefinition wfDef = convert(processDef);
        return factory.createDeployment(wfDef);
    }

    /**
     * Convert a {@link ProcessDefinition} into a {@link WorkflowDefinition}.
     * @param definition ProcessDefinition
     * @return WorkflowDefinition
     */
    public WorkflowDefinition convert(ProcessDefinition definition)
    {
        if(definition==null)
            return null;
        
        String defId = definition.getId();
        String defName = definition.getKey();
        int version = definition.getVersion();
        String defaultTitle = definition.getName();

        if (activitiUtil.isMultiTenantWorkflowDeploymentEnabled() && !TenantUtil.isDefaultTenantName(defName)) {
            defName = defName.substring(defName.lastIndexOf(TenantService.SEPARATOR) + 1);
        }

        String startTaskName = null;
        Process process = repositoryService.getBpmnModel(defId)
                                           .getProcessById(defName);
        FlowElement startElement = process.getInitialFlowElement();
        if(startElement instanceof StartEvent)
        {
            StartEvent startEvent = (StartEvent) startElement;
            startTaskName = startEvent.getFormKey();
        }

        ProcessDefinition def = repositoryService.getProcessDefinition(defId);
        FlowElement startEvent = repositoryService
                    .getBpmnModel(def.getId())
                    .getProcessById(defName)
                    .getInitialFlowElement();
        WorkflowTaskDefinition startTask = getTaskDefinition(startEvent, startTaskName, defName, true);
        
        return factory.createDefinition(defId,
                    defName, version, defaultTitle,
                    null, startTask);
    }

    private FlowElement getStartFormData(final String definitionId, String processKey)
    {
        Process process = repositoryService.getBpmnModel(definitionId)
                                               .getProcessById(processKey);
        return process.getInitialFlowElement();
    }
    
    public WorkflowTaskDefinition getTaskDefinition(FlowElement activity, String taskFormKey, String processKey, boolean isStart)
    {
        WorkflowNode node = getNode(activity, processKey, true);
        String taskDefId = taskFormKey == null ? node.getName() : taskFormKey;
        return factory.createTaskDefinition(taskDefId, node, taskFormKey, isStart);
    }
    
    public WorkflowTaskDefinition getTaskDefinition(Task task)
    {
    	// Get the task-form used (retrieved from cached process-definition)
        String taskDefId = task.getFormKey();
        
        // Fetch node based on cached process-definition
        ProcessDefinitionEntityImpl procDef = (ProcessDefinitionEntityImpl) repositoryService.getProcessDefinition(task.getProcessDefinitionId());

        WorkflowNode node = convert(repositoryService.getBpmnModel(procDef.getId()).getFlowElement(task.getTaskDefinitionKey()), procDef.getId(), true);
        
        return factory.createTaskDefinition(taskDefId, node, taskDefId, false);
    }

    /**
     * Get the taskDefinition key based on the Activiti task definition id,
     * @param taskDefinitionKey String
     * @param processDefinitionId String
     * @return WorkflowTaskDefinition
     */
    public WorkflowTaskDefinition getTaskDefinition(String taskDefinitionKey, String processDefinitionId)
    {
    	 ProcessDefinitionEntity procDef = (ProcessDefinitionEntity) repositoryService.getProcessDefinition(processDefinitionId);
    	 Collection<FlowElement> userTasks = findUserTasks(repositoryService
                     .getBpmnModel(procDef.getId()).
                     getProcessById(procDef.getKey())
                     .getInitialFlowElement());

    	 for(FlowElement activity : userTasks)
    	 {
    		 if(activity instanceof UserTask)
    		 {
    			 String formKey = getFormKey((UserTask) activity);
    			 WorkflowNode node = convert(activity, procDef.getId());
    			 return factory.createTaskDefinition(formKey, node, formKey, false);
    		 }
    	 }
    	 
    	 return null;
    }

    private WorkflowTransition getDefaultTransition(String processDefKey, String nodeId)
    {
        String processKey = processDefKey + TRANSITION_SUFFIX;
        String nodeKey = processDefKey + ".node." + nodeId + TRANSITION_SUFFIX; 
        String transitionId = DEFAULT_TRANSITION_NAME;
        String title = DEFAULT_TRANSITION_NAME;
        String description =DEFAULT_TRANSITION_DESCRIPTION;
        return factory.createTransition(transitionId, title, description, true, nodeKey, processKey, DEFAULT_TRANSITION_KEY);
    }

    public WorkflowInstance convert(ProcessInstance instance)
    {
       return convertAndSetVariables(instance, (Map<String, Object>) null);
    }
    
    public WorkflowInstance convertAndSetVariables(ProcessInstance instance, Map<String, Object> collectedvariables)
    {
        if(instance == null)
            return null;
        
        HistoricProcessInstance historicInstance = historyService
        	.createHistoricProcessInstanceQuery()
        	.processInstanceId(instance.getId())
        	.singleResult();
        
       return convertToInstanceAndSetVariables(historicInstance, collectedvariables);
    }

    public WorkflowInstance convert(HistoricProcessInstance instance, Map<String, Object> collectedvariables)
    {
        if(instance == null)
            return null;
        
        HistoricProcessInstance historicInstance = historyService
        .createHistoricProcessInstanceQuery()
        .processInstanceId(instance.getId())
        .singleResult();
        
        return convertToInstanceAndSetVariables(historicInstance, collectedvariables);
    }

    public WorkflowPath convert(Execution execution)
    {
        String instanceId = execution.getProcessInstanceId();
        ProcessInstance instance = activitiUtil.getProcessInstance(instanceId);
        return convert(execution, instance);
    }

    public WorkflowPath convert(Execution execution, ProcessInstance instance)
    {
        if(execution == null)
            return null;
        
        boolean isActive = !execution.isEnded();
        
        // Convert workflow and collect variables
        Map<String, Object> workflowInstanceVariables = new HashMap<String, Object>();
        WorkflowInstance wfInstance = convertAndSetVariables(instance, workflowInstanceVariables);
        
        WorkflowNode node = null;
        // Get active node on execution
        List<String> nodeIds = runtimeService.getActiveActivityIds(execution.getId());

        if (nodeIds != null && nodeIds.size() >= 1)
        {
            ProcessDefinition procDef = repositoryService.getProcessDefinition(instance.getProcessDefinitionId());
            FlowElement activity = repositoryService.getBpmnModel(procDef.getId())
                        .getProcessById(procDef.getKey())
                        .getInitialFlowElement();
            node = getNode(activity, procDef.getKey(), false);
        }

        return factory.createPath(execution.getId(), wfInstance, node, isActive);
    }
    
    public WorkflowNode convert(FlowElement activity, boolean forceIsTaskNode)
    {
    	 String procDefId = activity.getId();
         String key = repositoryService.getProcessDefinition(procDefId).getKey();
         return getNode(activity, key, forceIsTaskNode);
    }

    public WorkflowNode convert(FlowElement activity, String procDefId, boolean forceIsTaskNode)
    {
        String key = repositoryService.getProcessDefinition(procDefId).getKey();
        return getNode(activity, key, forceIsTaskNode);
    }

    /**
     * @param activity PvmActivity
     * @param key String
     * @param forceIsTaskNode boolean
     * @return WorkflowNode
     */
    private WorkflowNode getNode(FlowElement activity, String key, boolean forceIsTaskNode)
    {
        String name = activity.getId();
        // TODO: How to these properties from FlowElement ?
         String defaultTitle = activity.getName();
         String defaultDescription = activity.getDocumentation();
         String type = activity.getName();
         boolean isTaskNode = forceIsTaskNode || ActivitiConstants.USER_TASK_NODE_TYPE.equals(type);
         
         if(defaultTitle == null)
         {
         	defaultTitle = name;
         }
         if(defaultDescription == null)
         {
         	defaultDescription = name;
         }
        WorkflowTransition transition = getDefaultTransition(key, name);
        return factory.createNode(name, key, defaultTitle, defaultDescription, type, isTaskNode, transition);
    }
    
    public WorkflowNode convert(FlowElement activity)
    {
       return convert(activity, false);
    }

    public WorkflowNode convert(FlowElement activity, String procDefId)
    {
        return convert(activity, procDefId, false);
    }
    public List<WorkflowPath> convertExecution(List<Execution> executions)
    {
        ArrayList<WorkflowPath> results = new ArrayList<WorkflowPath>(executions.size());
        for (Execution execution : executions)
        {
            results.add(convert(execution));
        }
        return results;
    }
    
    public <T> List<T> convert(List<?> inputs)
    {
        return convert(inputs, false);
    }
    
    @SuppressWarnings("unchecked")
    public <T> List<T> convert(List<?> inputs, boolean ignoreNotDeployed)
    {
        ArrayList<T> results = new ArrayList<T>(inputs.size());
        for (Object in : inputs)
        {
            T out = null;
            try
            {
                out = (T) convert(in);
            }
            catch (NullPointerException npe)
            {
                if (!ignoreNotDeployed)
                {
                    throw npe;
                }
            }
            if(out != null)
            {
                results.add(out);
            }
        }
        return results;
    }
    
    /**
     * Converts an Activiti object to an Alresco Workflow object.
     * Determines the exact conversion method to use by checking the class of object.
     * @param obj The object to be converted.
     * @return the converted object.
     */
    private Object convert(Object obj)
    {
        if(obj == null)
            return null;
        
        if (obj instanceof Deployment)
        {
            return convert( (Deployment) obj);
        }
        if (obj instanceof ProcessDefinition)
        {
            return convert( (ProcessDefinition) obj);
        }
        if (obj instanceof ProcessInstance)
        {
            return convert( (ProcessInstance) obj);
        }
        if (obj instanceof Execution)
        {
            return convert( (Execution) obj);
        }
        if (obj instanceof Task)
        {
            return convert( (Task) obj);
        }
        if(obj instanceof HistoricTaskInstance) 
        {
            return convert((HistoricTaskInstance) obj);
        }
        if(obj instanceof HistoricProcessInstance) 
        {
            return convert((HistoricProcessInstance) obj);
        }
        throw new WorkflowException("Cannot convert object: " + obj + " of type: " + obj.getClass());
    }
    
    
    public WorkflowTask convert(Task task)
    {
       return convert(task, false);
    }
    
    /**
     * Converts the given task into a {@link WorkflowTask}, allows ignoring domain mismatch (ALF-12264)
     * @param task Task
     * @param ignoreDomainMismatch whether or not to ignore domain mismatch exception
     * @return the converter task. Returns null when the domain mismatched and ignoreDomainMismatch was true.
     */
    public WorkflowTask convert(Task task, boolean ignoreDomainMismatch)
    {
        if(task == null)
            return null;
        String id = task.getId();
        String defaultTitle = task.getName();
        String defaultDescription = task.getDescription();
        
        WorkflowTaskState state = WorkflowTaskState.IN_PROGRESS;
        WorkflowPath path = getWorkflowPath(task.getExecutionId(), ignoreDomainMismatch);
        
        if(path != null) 
        {
        	// Since the task is active, it's safe to use the active node on
            // the execution path
            WorkflowNode node = path.getNode();

            String taskDefId = task.getFormKey();
            WorkflowTaskDefinition taskDef = factory.createTaskDefinition(taskDefId, node, taskDefId, false);
            
            // All task-properties should be fetched, not only local
            Map<QName, Serializable> properties = propertyConverter.getTaskProperties(task);
            
            return factory.createTask(id,
                        taskDef, taskDef.getId(), defaultTitle, defaultDescription, state, path, properties);
        }
        else
        {
        	// Ignoring this task, domain mismatched and safely ignoring that
        	return null;
        }
    }
    
    public Map<QName, Serializable> getTaskProperties(Task task)
    {
    	return propertyConverter.getTaskProperties(task);
    }
    
    public Map<QName, Serializable> getTaskProperties(HistoricTaskInstance task)
    {
    	// Get the local task variables from the history
        Map<String, Object> variables = propertyConverter.getHistoricTaskVariables(task.getId());
       return propertyConverter.getTaskProperties(task, variables);
    }
    
    public WorkflowPath getWorkflowPath(String executionId, boolean ignoreDomainMismatch)
    {
    	 Execution execution = activitiUtil.getExecution(executionId);
         
         WorkflowPath path = null;
         try 
         {
         	path = convert(execution);
         } 
         catch(RuntimeException re) 
         {
         	if(!ignoreDomainMismatch)
         	{
         		throw re;
         	}
         }
         return path;
    }
    
    public WorkflowTask getVirtualStartTask(String processInstanceId, Boolean inProgress)
    {
        ProcessInstance processInstance = activitiUtil.getProcessInstance(processInstanceId);
        if(processInstance != null)
        {
            if(null == inProgress)
            {
                inProgress = isStartTaskActive(processInstanceId);
            }
            return getVirtualStartTask(processInstance, inProgress);
        }
        HistoricProcessInstance historicProcessInstance = activitiUtil.getHistoricProcessInstance(processInstanceId);
        return getVirtualStartTask(historicProcessInstance);
    }

    public boolean isStartTaskActive(String processInstanceId)
    {
        Object endDate = runtimeService.getVariable(processInstanceId, ActivitiConstants.PROP_START_TASK_END_DATE);
        return endDate == null;
    }

    private WorkflowTask getVirtualStartTask(ProcessInstance processInstance, boolean inProgress)
    {
        String processInstanceId = processInstance.getId();

        if (!activitiUtil.isMultiTenantWorkflowDeploymentEnabled() && !isCorrectTenantRuntime(processInstanceId))
        {
            return null;
        }
        
        String id = ActivitiConstants.START_TASK_PREFIX + processInstanceId;
        
        WorkflowTaskState state = null;
        if(inProgress)
        {
            state = WorkflowTaskState.IN_PROGRESS;
        }
        else
        {
            state = WorkflowTaskState.COMPLETED;
        }
        
        WorkflowPath path  = convert((Execution)processInstance);
        
        // Convert start-event to start-task Node
        String definitionId = processInstance.getProcessDefinitionId();
        ProcessDefinition procDef = repositoryService.getProcessDefinition(definitionId);

        WorkflowNode startNode = getNode(repositoryService.getBpmnModel(procDef.getId())
                    .getProcessById(procDef.getKey())
                    .getInitialFlowElement(), procDef.getKey(),  true);
        
        FlowElement startElement = getStartFormData(procDef.getId(), procDef.getKey());
        String taskDefId = null;
        if(startElement instanceof StartEvent)
        {
            StartEvent startEvent = (StartEvent) startElement;
            taskDefId = startEvent.getFormKey();
        }
        WorkflowTaskDefinition taskDef = factory.createTaskDefinition(taskDefId, startNode, taskDefId, true);
        
        // Add properties based on HistoricProcessInstance
        HistoricProcessInstance historicProcessInstance = historyService
            .createHistoricProcessInstanceQuery()
            .processInstanceId(processInstanceId)
            .singleResult();
        
        Map<QName, Serializable> properties = propertyConverter.getStartTaskProperties(historicProcessInstance, taskDefId, !inProgress);
        
        // TODO: Figure out what name/description should be used for the start-task, start event's name?
        String defaultTitle = null;
        String defaultDescription = null;
        
        return factory.createTask(id,
                    taskDef, taskDef.getId(), defaultTitle, defaultDescription, state, path, properties);
    }

    public boolean isCorrectTenantRuntime(String processInstanceId, boolean isRuntime)
    {
    	// Runtime domain only applicable in case tenant-aware deployment is turned off
    	if(!activitiUtil.isMultiTenantWorkflowDeploymentEnabled()) {
    		if (isRuntime)
    		{
    			return isCorrectTenantRuntime(processInstanceId); 
    		}
    		else
    		{
    			return isCorrectTenantHistoric(processInstanceId); 
    		}
    	}
    	return true;
    }
    
    public boolean isCorrectTenantRuntime(String processInstanceId)
    {
        return runtimeService.createProcessInstanceQuery()
            .processInstanceId(processInstanceId)
            .variableValueEquals(ActivitiConstants.VAR_TENANT_DOMAIN, TenantUtil.getCurrentDomain())
            .count()>0;
    }

    public boolean isCorrectTenantHistoric(String processInstanceId)
    {
        return historyService.createHistoricProcessInstanceQuery()
            .processInstanceId(processInstanceId)
            .variableValueEquals(ActivitiConstants.VAR_TENANT_DOMAIN, TenantUtil.getCurrentDomain())
            .count()>0;
    }
    
    private WorkflowTask getVirtualStartTask(HistoricProcessInstance historicProcessInstance)
    {
        if(historicProcessInstance == null)
        {
            return null;
        }
        String processInstanceId = historicProcessInstance.getId();

        if (!activitiUtil.isMultiTenantWorkflowDeploymentEnabled() && false == isCorrectTenantHistoric(processInstanceId))
        {
            return null;
        }
        
        String id = ActivitiConstants.START_TASK_PREFIX + processInstanceId;
        
        // Since the process instance is complete the Start Task must be complete!
        WorkflowTaskState state = WorkflowTaskState.COMPLETED;

        // We use the process-instance ID as execution-id. It's ended anyway
        WorkflowPath path  = buildCompletedPath(processInstanceId, processInstanceId);
        if(path == null)
        {
            return null;
        }
        
        // Convert start-event to start-task Node
        ProcessDefinition procDef = repositoryService.getProcessDefinition(historicProcessInstance.getProcessDefinitionId());
        WorkflowNode startNode = convert(repositoryService
                    .getBpmnModel(procDef.getId())
                    .getProcessById(procDef.getKey())
                    .getInitialFlowElement(), procDef.getId(), true);
        
        String taskDefId = activitiUtil.getStartTaskTypeName(historicProcessInstance.getProcessDefinitionId());
        WorkflowTaskDefinition taskDef = factory.createTaskDefinition(taskDefId, startNode, taskDefId, true);
        
        boolean completed = historicProcessInstance.getEndTime() != null;
        Map<QName, Serializable> properties = propertyConverter.getStartTaskProperties(historicProcessInstance, taskDefId, completed);
        
        // TODO: Figure out what name/description should be used for the start-task, start event's name?
        String defaultTitle = null;
        String defaultDescription = null;
        
        return factory.createTask(id,
                    taskDef, taskDef.getId(), defaultTitle, defaultDescription, state, path, properties);
    }
    
    public WorkflowTask convert(HistoricTaskInstance historicTaskInstance) 
    {
        if(historicTaskInstance == null) 
        {
            return null;
        }
       
        WorkflowPath path = getWorkflowPath(historicTaskInstance);
        if(path == null)
        {
            // When path is null, workflow is deleted or cancelled. Task should
            // not be used
            return null;
        }
        
        // Extract node from historic task
        WorkflowNode node = buildHistoricTaskWorkflowNode(historicTaskInstance);
        
        WorkflowTaskState state= WorkflowTaskState.COMPLETED;

        String taskId = historicTaskInstance.getId();
        
        // Get the local task variables from the history
        Map<String, Object> variables = propertyConverter.getHistoricTaskVariables(taskId);
        Map<QName, Serializable> historicTaskProperties = propertyConverter.getTaskProperties(historicTaskInstance, variables);
        
        // Get task definition from historic variable 
        String formKey = (String) variables.get(ActivitiConstants.PROP_TASK_FORM_KEY);
        WorkflowTaskDefinition taskDef = factory.createTaskDefinition(formKey, node, formKey, false);
        String title = historicTaskInstance.getName();
        String description = historicTaskInstance.getDescription();
        String taskName = taskDef.getId();

        return factory.createTask(taskId, taskDef, taskName, 
                    title, description, state, path, historicTaskProperties);
    }
    
    public WorkflowPath getWorkflowPath(HistoricTaskInstance historicTaskInstance)
    {
    	 WorkflowPath path = null;
         // Check to see if the instance is still running
         Execution execution = activitiUtil.getExecution(historicTaskInstance.getExecutionId());
         
         if(execution != null)
         {
             // Process execution still running
             path  = convert(execution);
         }
         else
         {
             // Process execution is historic
             path  = buildCompletedPath(historicTaskInstance.getExecutionId(), historicTaskInstance.getProcessInstanceId());
         }
         return path;
    }
    
    public String getFormKey(FlowElement act)
    {
        if(act instanceof UserTask)
        {
            UserTask actImpl = (UserTask) act;
            return actImpl.getFormKey();
        }
        return null;
    }

    private String getFormKey(UserTask userTask)
    {
        if(userTask != null){
            return userTask.getFormKey();
        }
         return null;
    }

    private WorkflowNode buildHistoricTaskWorkflowNode(HistoricTaskInstance historicTaskInstance) 
    {
    	ProcessDefinition procDef = repositoryService.getProcessDefinition(historicTaskInstance.getProcessDefinitionId());
        FlowElement flowElement = repositoryService.getBpmnModel(procDef.getId()).getFlowElement(historicTaskInstance.getTaskDefinitionKey());

		return convert(flowElement, procDef.getId());
	}

	public WorkflowPath buildCompletedPath(String executionId, String processInstanceId)
    {
        WorkflowInstance wfInstance = null;
        ProcessInstance processInstance = activitiUtil.getProcessInstance(processInstanceId);
        if(processInstance != null)
        {
            wfInstance = convert(processInstance);
        } 
        else
        {
            HistoricProcessInstance historicProcessInstance = activitiUtil.getHistoricProcessInstance(processInstanceId);
            if(historicProcessInstance!= null)
                wfInstance = convert(historicProcessInstance);
        }
        if(wfInstance == null)
        {
        	// When workflow is cancelled or deleted, WorkflowPath should not be returned
        	return null;
        }
        WorkflowNode node = null;
        return factory.createPath(executionId, wfInstance, node, false);
    }

    public WorkflowInstance convertToInstanceAndSetVariables(HistoricProcessInstance historicProcessInstance, Map<String, Object> collectedVariables)
    {
        String processInstanceId = historicProcessInstance.getId();
        String id = processInstanceId;
        ProcessDefinition procDef = activitiUtil.getProcessDefinition(historicProcessInstance.getProcessDefinitionId());
        WorkflowDefinition definition = convert(procDef);
        
        // Set process variables based on historic detail query
        Map<String, Object> variables = propertyConverter.getHistoricProcessVariables(processInstanceId);
        
        Date startDate = historicProcessInstance.getStartTime();
        Date endDate = historicProcessInstance.getEndTime();

        // Copy all variables to map, if not null
        if(collectedVariables != null)
        {
        	collectedVariables.putAll(variables);
        }
        boolean isActive = endDate == null;
        return factory.createInstance(id, definition, variables, isActive, startDate, endDate);
    }
    
    public String getWorkflowDefinitionName(String workflowDefinitionId)
    {
    	ProcessDefinition def = repositoryService.getProcessDefinition(workflowDefinitionId);
    	return ((ProcessDefinition) def).getKey();
    }
    
    public Collection<FlowElement> findUserTasks(FlowElement startEvent)
    {
        // Use a linked hashmap to get the task defs in the right order
        Map<String, FlowElement> userTasks = new LinkedHashMap<String, FlowElement>();
        Set<String> processedActivities = new HashSet<String>();
        
        // Start finding activities recursively
        findUserTasks(startEvent, userTasks, processedActivities);
        
        return userTasks.values();
    }

    private void findUserTasks(FlowElement currentActivity, Map<String, FlowElement> userTasks, Set<String> processedActivities)
    {
        // Only process activity if not already processed, to prevent endless loops
        if(!processedActivities.contains(currentActivity.getId()))
        {
            processedActivities.add(currentActivity.getId());
            if(isUserTask(currentActivity))
            {
                userTasks.put(currentActivity.getId(), currentActivity);
            }

            // Process outgoing transitions
            List<SequenceFlow> outgoingFlows = ((FlowNode) currentActivity).getOutgoingFlows();
            if(outgoingFlows != null)
            {
                for(SequenceFlow transition : outgoingFlows)
                {
                    if(transition.getTargetFlowElement() != null)
                    {
                        findUserTasks(transition.getTargetFlowElement(), userTasks, processedActivities);
                    }
                }
            }

            if (isSubProcess(currentActivity))
            {
                Optional<FlowElement> startEvent =
                            ((SubProcess) currentActivity)
                                        .getFlowElements()
                                        .stream()
                                        .filter(flowElement -> flowElement instanceof StartEvent)
                                        .findFirst();
                startEvent.ifPresent(flowElement -> findUserTasks(flowElement, userTasks, processedActivities));
            }
        }
    }

    private boolean isUserTask(FlowElement currentActivity)
    {
        return currentActivity instanceof UserTask;
    }

    private boolean isSubProcess(FlowElement currentActivity)
    {
        return currentActivity instanceof SubProcess;
    }

    public WorkflowInstance convert(HistoricProcessInstance historicProcessInstance)
    {
    	return convertToInstanceAndSetVariables(historicProcessInstance, null);
    }
    
    public WorkflowObjectFactory getWorkflowObjectFactory() {
		return factory;
	}

}
