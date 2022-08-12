/*-
 * #%L
 * alfresco-tas-restapi
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
package org.alfresco.rest.search;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Generated by 'gethin' on '2017-03-17 13:23' from 'Alfresco Search REST API' swagger file 
 * Generated from 'Alfresco Search REST API' swagger file
 * Base Path {@linkplain /alfresco/api/-default-/public/search/versions/1}
 */
public class RestGenericMetricModel extends TestModel implements IRestModel<RestGenericMetricModel>
{
    @Override
    public ModelAssertion<RestGenericMetricModel> assertThat()
    {
        return new ModelAssertion<RestGenericMetricModel>(this);
    }

    @Override
    public ModelAssertion<RestGenericMetricModel> and()
    {
        return assertThat();
    }

    @JsonProperty(value = "entry")
    RestGenericMetricModel model;

    @Override
    public RestGenericMetricModel onModel()
    {
        return model;
    }

    /**
    The type of metric, e.g. count
    */	        

    private String type;	    
    /**
    The metric value, e.g.
    */	        

    private Object value;	    

    public String getType()
    {
        return this.type;
    }

    public void setType(String type)
    {
        this.type = type;
    }				

    public Object getValue()
    {
        return this.value;
    }

    public void setValue(Object value)
    {
        this.value = value;
    }				
}
 