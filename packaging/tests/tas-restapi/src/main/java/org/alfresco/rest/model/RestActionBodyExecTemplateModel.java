package org.alfresco.rest.model;

import java.util.List;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Generated by 'Kristian.Dimitrov@hyland.com' on '2022-07-14 13:22' from 'Alfresco Content Services REST API' swagger file 
 * Generated from 'Alfresco Content Services REST API' swagger file
 * Base Path {@linkplain /alfresco/api/-default-/public/alfresco/versions/1}
 */
public class RestActionBodyExecTemplateModel extends TestModel implements IRestModel<RestActionBodyExecTemplateModel>
{
    @Override
    public ModelAssertion<RestActionBodyExecTemplateModel> assertThat()
    {
        return new ModelAssertion<RestActionBodyExecTemplateModel>(this);
    }

    @Override
    public ModelAssertion<RestActionBodyExecTemplateModel> and()
    {
        return assertThat();
    }

    @JsonProperty(value = "entry")
    RestActionBodyExecTemplateModel model;

    @Override
    public RestActionBodyExecTemplateModel onModel()
    {
        return model;
    }


    @JsonProperty(required = true)    
    private String actionDefinitionId;	    

    private Object params;	    

    public String getActionDefinitionId()
    {
        return this.actionDefinitionId;
    }

    public void setActionDefinitionId(String actionDefinitionId)
    {
        this.actionDefinitionId = actionDefinitionId;
    }				

    public Object getParams()
    {
        return this.params;
    }

    public void setParams(Object params)
    {
        this.params = params;
    }				
}
 