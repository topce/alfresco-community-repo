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
package org.alfresco.rest.model;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RestRatingModel extends TestModel implements IRestModel<RestRatingModel>
{
    @JsonProperty(value = "entry")
    RestRatingModel model;

    @JsonProperty(required = true)
    private String id;
    
    private String ratedAt;
    private String myRating;
    private RestAggregateModel aggregate;

    @Override
    public RestRatingModel onModel()
    {
        return model;
    }
    
    public RestAggregateModel getAggregate()
    {
        return aggregate;
    }

    public void setAggregate(RestAggregateModel aggregate)
    {
        this.aggregate = aggregate;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getRatedAt()
    {
        return ratedAt;
    }

    public void setRatedAt(String ratedAt)
    {
        this.ratedAt = ratedAt;
    }

    public String getMyRating()
    {
        return myRating;
    }

    public void setMyRating(String myRating)
    {
        this.myRating = myRating;
    }

    @Override
    public ModelAssertion<RestRatingModel> assertThat() 
    {      
      return new ModelAssertion<>(this);
    }

    @Override
    public ModelAssertion<RestRatingModel> and() 
    {      
      return assertThat();
    }
}    