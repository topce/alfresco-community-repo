/*
 * #%L
 * Alfresco Remote API
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
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rest.api.model;

import java.util.Objects;

public class Category
{
    private String id;
    private String name;
    private String parentId;
    private boolean hasChildren;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getParentId()
    {
        return parentId;
    }

    public void setParentId(String parentId)
    {
        this.parentId = parentId;
    }

    public boolean getHasChildren()
    {
        return hasChildren;
    }

    public void setHasChildren(boolean hasChildren)
    {
        this.hasChildren = hasChildren;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return hasChildren == category.hasChildren && Objects.equals(id, category.id) && name.equals(category.name) &&
                Objects.equals(parentId, category.parentId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, name, parentId, hasChildren);
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static class Builder
    {
        private String id;
        private String name;
        private String parentId;
        private boolean hasChildren;

        public Builder id(String id)
        {
            this.id = id;
            return this;
        }

        public Builder name(String name)
        {
            this.name = name;
            return this;
        }

        public Builder parentId(String parentId)
        {
            this.parentId = parentId;
            return this;
        }

        public Builder hasChildren(boolean hasChildren)
        {
            this.hasChildren = hasChildren;
            return this;
        }

        public Category create()
        {
            final Category category = new Category();
            category.setId(id);
            category.setName(name);
            category.setParentId(parentId);
            category.setHasChildren(hasChildren);
            return category;
        }
    }
}