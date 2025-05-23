/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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

package org.alfresco.repo.event2;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.Test;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.event.v1.model.ChildAssociationResource;
import org.alfresco.repo.event.v1.model.EventData;
import org.alfresco.repo.event.v1.model.EventType;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;

/**
 * @author Adina Ababei
 * @author Iulian Aftene
 */
public class ChildAssociationRepoEventIT extends AbstractContextAwareRepoEvent
{
    @Test
    public void testAddChildAssociation()
    {
        String assocLocalName = GUID.generate();
        final NodeRef parentNodeRef = createNode(ContentModel.TYPE_FOLDER);
        final NodeRef childNodeRef = createNode(ContentModel.TYPE_CONTENT);

        checkNumOfEvents(2);

        RepoEvent<EventData<NodeResource>> resultRepoEvent = getRepoEventWithoutWait(1);
        assertEquals("Wrong repo event type.", EventType.NODE_CREATED.getType(),
                resultRepoEvent.getType());

        resultRepoEvent = getRepoEventWithoutWait(2);
        assertEquals("Wrong repo event type.", EventType.NODE_CREATED.getType(),
                resultRepoEvent.getType());

        retryingTransactionHelper.doInTransaction(() -> nodeService.addChild(
                parentNodeRef,
                childNodeRef,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(TEST_NAMESPACE, assocLocalName)));

        List<ChildAssociationRef> childAssociationRefs = retryingTransactionHelper.doInTransaction(() -> nodeService.getChildAssocs(parentNodeRef));

        assertEquals(1, childAssociationRefs.size());
        assertFalse(childAssociationRefs.get(0).isPrimary());

        checkNumOfEvents(4);

        // node event
        final RepoEvent<EventData<NodeResource>> nodeRepoEvent = getRepoEventWithoutWait(3);
        assertEquals("Wrong repo event type.", EventType.NODE_UPDATED.getType(), nodeRepoEvent.getType());
        assertNotNull("Repo event ID is not available.", nodeRepoEvent.getId());
        assertNotNull("Source is not available", nodeRepoEvent.getSource());
        assertEquals("Repo event source is not available.",
                "/" + descriptorService.getCurrentRepositoryDescriptor().getId(),
                nodeRepoEvent.getSource().toString());
        assertNotNull("Repo event creation time is not available.", nodeRepoEvent.getTime());
        assertEquals("Invalid repo event datacontenttype", "application/json",
                nodeRepoEvent.getDatacontenttype());
        assertNotNull(nodeRepoEvent.getDataschema());
        assertEquals(EventJSONSchema.NODE_UPDATED_V1.getSchema(), nodeRepoEvent.getDataschema());

        final EventData<NodeResource> nodeResourceEventData = getEventData(nodeRepoEvent);
        assertNotNull("Event data group ID is not available. ", nodeResourceEventData.getEventGroupId());
        assertNotNull("resourceBefore property is not available", nodeResourceEventData.getResourceBefore());

        final NodeResource nodeResource = getNodeResource(nodeRepoEvent);
        final NodeResource nodeResourceBefore = getNodeResourceBefore(nodeRepoEvent);
        assertNotSame("Secondary parents actual and earlier state should differ", nodeResource.getSecondaryParents(), nodeResourceBefore.getSecondaryParents());

        // child association event
        final RepoEvent<EventData<ChildAssociationResource>> childAssocRepoEvent = getFilteredEvent(EventType.CHILD_ASSOC_CREATED, 0);
        assertEquals("Wrong repo event type.", EventType.CHILD_ASSOC_CREATED.getType(), childAssocRepoEvent.getType());
        assertNotNull("Repo event ID is not available.", childAssocRepoEvent.getId());
        assertNotNull("Source is not available", childAssocRepoEvent.getSource());
        assertEquals("Repo event source is not available.",
                "/" + descriptorService.getCurrentRepositoryDescriptor().getId(),
                childAssocRepoEvent.getSource().toString());
        assertNotNull("Repo event creation time is not available.", childAssocRepoEvent.getTime());
        assertEquals("Invalid repo event datacontenttype", "application/json",
                childAssocRepoEvent.getDatacontenttype());
        assertNotNull(childAssocRepoEvent.getDataschema());
        assertEquals(EventJSONSchema.CHILD_ASSOC_CREATED_V1.getSchema(), childAssocRepoEvent.getDataschema());

        final EventData<ChildAssociationResource> childAssocResourceEventData = getEventData(childAssocRepoEvent);
        assertNotNull("Event data group ID is not available. ", childAssocResourceEventData.getEventGroupId());
        assertNull("resourceBefore property is not available", childAssocResourceEventData.getResourceBefore());

        final ChildAssociationResource childAssociationResource = getChildAssocResource(childAssocRepoEvent);
        assertEquals("Wrong parent", parentNodeRef.getId(), childAssociationResource.getParent().getId());
        assertEquals("Wrong child", childNodeRef.getId(), childAssociationResource.getChild().getId());
        assertEquals("Wrong assoc type", "cm:contains", childAssociationResource.getAssocType());
        assertEquals("Wrong assoc name", "ce:" + assocLocalName, childAssociationResource.getAssocQName());

        assertEquals("Node and child association events should have same eventGroupId", nodeResourceEventData.getEventGroupId(), childAssocResourceEventData.getEventGroupId());
        assertTrue("Wrong node's secondary parents", nodeResource.getSecondaryParents().contains(childAssociationResource.getParent().getId()));
    }

    @Test
    public void testRemoveChildAssociation()
    {
        final NodeRef parentNodeRef = createNode(ContentModel.TYPE_FOLDER);
        final NodeRef childNodeRef = createNode(ContentModel.TYPE_CONTENT);

        checkNumOfEvents(2);
        RepoEvent<EventData<NodeResource>> parentRepoEvent = getRepoEventWithoutWait(1);
        assertEquals("Wrong repo event type.", EventType.NODE_CREATED.getType(), parentRepoEvent.getType());

        RepoEvent<EventData<NodeResource>> childRepoEvent = getRepoEventWithoutWait(2);
        assertEquals("Wrong repo event type.", EventType.NODE_CREATED.getType(), childRepoEvent.getType());

        ChildAssociationRef childAssociationRef = retryingTransactionHelper.doInTransaction(() -> nodeService.addChild(
                parentNodeRef,
                childNodeRef,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(TEST_NAMESPACE, GUID.generate())));

        List<ChildAssociationRef> childAssociationRefs = retryingTransactionHelper.doInTransaction(() -> nodeService.getChildAssocs(parentNodeRef));

        assertEquals(1, childAssociationRefs.size());
        assertFalse(childAssociationRefs.get(0).isPrimary());

        checkNumOfEvents(4);

        retryingTransactionHelper.doInTransaction(() -> nodeService.removeChildAssociation(childAssociationRef));

        childAssociationRefs = retryingTransactionHelper.doInTransaction(() -> nodeService.getChildAssocs(parentNodeRef));

        assertEquals(0, childAssociationRefs.size());

        checkNumOfEvents(6);

        // node repo event
        final RepoEvent<EventData<NodeResource>> nodeRepoEvent = getRepoEventWithoutWait(5);
        assertEquals("Wrong repo event type.", EventType.NODE_UPDATED.getType(), nodeRepoEvent.getType());
        assertNotNull("Repo event ID is not available.", nodeRepoEvent.getId());
        assertNotNull("Source is not available", nodeRepoEvent.getSource());
        assertEquals("Repo event source is not available.",
                "/" + descriptorService.getCurrentRepositoryDescriptor().getId(),
                nodeRepoEvent.getSource().toString());
        assertNotNull("Repo event creation time is not available.", nodeRepoEvent.getTime());
        assertEquals("Invalid repo event datacontenttype", "application/json",
                nodeRepoEvent.getDatacontenttype());
        assertNotNull(nodeRepoEvent.getDataschema());
        assertEquals(EventJSONSchema.NODE_UPDATED_V1.getSchema(), nodeRepoEvent.getDataschema());

        final EventData<NodeResource> nodeResourceEventData = getEventData(nodeRepoEvent);
        assertNotNull("Event data group ID is not available. ", nodeResourceEventData.getEventGroupId());
        assertNotNull("resourceBefore property is not available", nodeResourceEventData.getResourceBefore());

        final NodeResource nodeResource = getNodeResource(nodeRepoEvent);
        final NodeResource nodeResourceBefore = getNodeResourceBefore(nodeRepoEvent);
        assertNotSame("Secondary parents actual and earlier state should differ", nodeResource.getSecondaryParents(), nodeResourceBefore.getSecondaryParents());

        // child association repo event
        final RepoEvent<EventData<ChildAssociationResource>> childAssocRepoEvent = getFilteredEvent(EventType.CHILD_ASSOC_DELETED, 0);
        assertEquals("Wrong repo event type.", EventType.CHILD_ASSOC_DELETED.getType(), childAssocRepoEvent.getType());
        assertNotNull("Repo event ID is not available. ", childAssocRepoEvent.getId());
        assertNotNull("Source is not available", childAssocRepoEvent.getSource());
        assertEquals("Repo event source is not available. ",
                "/" + descriptorService.getCurrentRepositoryDescriptor().getId(),
                childAssocRepoEvent.getSource().toString());
        assertNotNull("Repo event creation time is not available. ", childAssocRepoEvent.getTime());
        assertEquals("Repo event datacontenttype", "application/json", childAssocRepoEvent.getDatacontenttype());
        assertNotNull(childAssocRepoEvent.getDataschema());
        assertEquals(EventJSONSchema.CHILD_ASSOC_DELETED_V1.getSchema(), childAssocRepoEvent.getDataschema());

        final EventData<ChildAssociationResource> childAssocResourceEventData = getEventData(childAssocRepoEvent);
        assertNotNull("Event data group ID is not available. ", childAssocResourceEventData.getEventGroupId());
        assertNull("resourceBefore property is not available", childAssocResourceEventData.getResourceBefore());

        final ChildAssociationResource childAssociationResource = getChildAssocResource(childAssocRepoEvent);
        assertEquals("Wrong parent", parentNodeRef.getId(), childAssociationResource.getParent().getId());
        assertEquals("Wrong child", childNodeRef.getId(), childAssociationResource.getChild().getId());
        assertEquals("Wrong assoc type", "cm:contains", childAssociationResource.getAssocType());

        assertEquals("Node and child association events should have same eventGroupId", nodeResourceEventData.getEventGroupId(), childAssocResourceEventData.getEventGroupId());
        assertTrue("Wrong node's secondary parents", nodeResourceBefore.getSecondaryParents().contains(childAssociationResource.getParent().getId()));
    }

    @Test
    public void testOneChildListOfParentsAssociations()
    {
        final NodeRef parent1NodeRef = createNode(ContentModel.TYPE_FOLDER);
        final NodeRef parent2NodeRef = createNode(ContentModel.TYPE_FOLDER);
        final NodeRef parent3NodeRef = createNode(ContentModel.TYPE_FOLDER);
        final NodeRef childNodeRef = createNode(ContentModel.TYPE_CONTENT);

        List<NodeRef> parents = Arrays.asList(parent1NodeRef, parent2NodeRef, parent3NodeRef);

        checkNumOfEvents(4);

        IntStream.of(1, 2, 3, 4).forEach(i -> {
            RepoEvent<EventData<NodeResource>> resultRepoEvent = getRepoEventWithoutWait(i);
            assertEquals("Wrong repo event type.", EventType.NODE_CREATED.getType(), resultRepoEvent.getType());
        });

        retryingTransactionHelper.doInTransaction(() -> nodeService.addChild(
                parents,
                childNodeRef,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(TEST_NAMESPACE, GUID.generate())));

        retryingTransactionHelper.doInTransaction(() -> {
            List<ChildAssociationRef> childAssocParent1 = nodeService.getChildAssocs(
                    parent1NodeRef);
            List<ChildAssociationRef> childAssocParent2 = nodeService.getChildAssocs(
                    parent2NodeRef);
            List<ChildAssociationRef> childAssocParent3 = nodeService.getChildAssocs(
                    parent3NodeRef);

            assertEquals(1, childAssocParent1.size());
            assertEquals(1, childAssocParent2.size());
            assertEquals(1, childAssocParent3.size());
            return null;
        });

        checkNumOfEvents(8);

        // 1 node.Updated events should be created
        List<RepoEvent<EventData<NodeResource>>> nodeUpdateEvent = getFilteredEvents(EventType.NODE_UPDATED);
        assertEquals("Wrong association events number", 1, nodeUpdateEvent.size());

        // 3 assoc.child.Created events should be created
        List<RepoEvent<EventData<ChildAssociationResource>>> childAssocEvents = getFilteredEvents(EventType.CHILD_ASSOC_CREATED);
        assertEquals("Wrong association events number", 3, childAssocEvents.size());
    }

    @Test
    public void testOneChildMultipleParentsSameTransaction()
    {
        final NodeRef parent1NodeRef = createNode(ContentModel.TYPE_FOLDER);
        final NodeRef parent2NodeRef = createNode(ContentModel.TYPE_FOLDER);
        final NodeRef parent3NodeRef = createNode(ContentModel.TYPE_FOLDER);
        final NodeRef childNodeRef = createNode(ContentModel.TYPE_CONTENT);

        List<NodeRef> parents = Arrays.asList(parent1NodeRef, parent2NodeRef, parent3NodeRef);
        checkNumOfEvents(4);

        IntStream.of(1, 2, 3, 4).forEach(i -> {
            RepoEvent<EventData<NodeResource>> resultRepoEvent = getRepoEventWithoutWait(i);
            assertEquals("Wrong repo event type.", EventType.NODE_CREATED.getType(), resultRepoEvent.getType());
        });

        retryingTransactionHelper.doInTransaction(() -> {
            for (NodeRef parent : parents)
            {
                nodeService.addChild(parent,
                        childNodeRef,
                        ContentModel.ASSOC_CONTAINS,
                        QName.createQName(TEST_NAMESPACE, GUID.generate()));
            }
            return null;
        });

        retryingTransactionHelper.doInTransaction(() -> {
            List<ChildAssociationRef> childAssocParent1 = nodeService.getChildAssocs(
                    parent1NodeRef);
            List<ChildAssociationRef> childAssocParent2 = nodeService.getChildAssocs(
                    parent2NodeRef);
            List<ChildAssociationRef> childAssocParent3 = nodeService.getChildAssocs(
                    parent3NodeRef);

            assertEquals(1, childAssocParent1.size());
            assertEquals(1, childAssocParent2.size());
            assertEquals(1, childAssocParent3.size());
            return null;
        });

        checkNumOfEvents(8);

        RepoEvent<EventData<NodeResource>> resultRepoEvent = getRepoEventWithoutWait(5);
        assertEquals("Wrong repo event type.", EventType.NODE_UPDATED.getType(), resultRepoEvent.getType());

        // 3 assoc.child.Created events should be created
        List<RepoEvent<EventData<ChildAssociationResource>>> childAssocEvents = getFilteredEvents(EventType.CHILD_ASSOC_CREATED);
        assertEquals("Wrong association events number", 3, childAssocEvents.size());

        // All events in the transaction should have the same eventGroupId
        String assocEventGroupID1 = getEventData(childAssocEvents.get(0)).getEventGroupId();
        String assocEventGroupID2 = getEventData(childAssocEvents.get(1)).getEventGroupId();
        String assocEventGroupID3 = getEventData(childAssocEvents.get(2)).getEventGroupId();

        assertEquals(assocEventGroupID1, assocEventGroupID2);
        assertEquals(assocEventGroupID2, assocEventGroupID3);
    }

    @Test
    public void testOneChildMultipleParentsDifferentTransaction()
    {
        final NodeRef parent1NodeRef = createNode(ContentModel.TYPE_FOLDER);
        final NodeRef parent2NodeRef = createNode(ContentModel.TYPE_FOLDER);
        final NodeRef parent3NodeRef = createNode(ContentModel.TYPE_FOLDER);
        final NodeRef childNodeRef = createNode(ContentModel.TYPE_CONTENT);

        List<NodeRef> parents = Arrays.asList(parent1NodeRef, parent2NodeRef, parent3NodeRef);

        checkNumOfEvents(4);

        IntStream.of(1, 2, 3, 4).forEach(i -> {
            RepoEvent<EventData<NodeResource>> resultRepoEvent = getRepoEventWithoutWait(i);
            assertEquals("Wrong repo event type.", EventType.NODE_CREATED.getType(), resultRepoEvent.getType());
        });

        for (NodeRef parent : parents)
        {
            retryingTransactionHelper.doInTransaction(() -> nodeService.addChild(
                    parent,
                    childNodeRef,
                    ContentModel.ASSOC_CONTAINS,
                    QName.createQName(TEST_NAMESPACE, GUID.generate())));
        }

        retryingTransactionHelper.doInTransaction(() -> {
            List<ChildAssociationRef> childAssocParent1 = nodeService.getChildAssocs(
                    parent1NodeRef);
            List<ChildAssociationRef> childAssocParent2 = nodeService.getChildAssocs(
                    parent2NodeRef);
            List<ChildAssociationRef> childAssocParent3 = nodeService.getChildAssocs(
                    parent3NodeRef);

            assertEquals(1, childAssocParent1.size());
            assertEquals(1, childAssocParent2.size());
            assertEquals(1, childAssocParent3.size());
            return null;
        });

        checkNumOfEvents(10);

        // 3 node.Updated events should be created
        List<RepoEvent<EventData<ChildAssociationResource>>> nodeUpdateEvents = getFilteredEvents(EventType.NODE_UPDATED);
        assertEquals("Wrong node update events number", 3, nodeUpdateEvents.size());

        // 3 assoc.child.Created events should be created
        List<RepoEvent<EventData<ChildAssociationResource>>> childAssocEvents = getFilteredEvents(EventType.CHILD_ASSOC_CREATED);
        assertEquals("Wrong association events number", 3, childAssocEvents.size());

        assertEquals(parent1NodeRef.getId(), getChildAssocResource(childAssocEvents.get(0)).getParent().getId());
        assertEquals(childNodeRef.getId(), getChildAssocResource(childAssocEvents.get(0)).getChild().getId());
        assertEquals("cm:contains", getChildAssocResource(childAssocEvents.get(0)).getAssocType());

        assertEquals(parent2NodeRef.getId(), getChildAssocResource(childAssocEvents.get(1)).getParent().getId());
        assertEquals(childNodeRef.getId(), getChildAssocResource(childAssocEvents.get(1)).getChild().getId());
        assertEquals("cm:contains", getChildAssocResource(childAssocEvents.get(1)).getAssocType());

        assertEquals(parent3NodeRef.getId(), getChildAssocResource(childAssocEvents.get(2)).getParent().getId());
        assertEquals(childNodeRef.getId(), getChildAssocResource(childAssocEvents.get(2)).getChild().getId());
        assertEquals("cm:contains", getChildAssocResource(childAssocEvents.get(2)).getAssocType());
    }

    @Test
    public void testOneParentMultipleChildrenSameTransaction()
    {
        final NodeRef parentNodeRef = createNode(ContentModel.TYPE_FOLDER);
        final NodeRef child1NodeRef = createNode(ContentModel.TYPE_CONTENT);
        final NodeRef child2NodeRef = createNode(ContentModel.TYPE_CONTENT);
        final NodeRef child3NodeRef = createNode(ContentModel.TYPE_CONTENT);

        List<NodeRef> children = Arrays.asList(child1NodeRef, child2NodeRef, child3NodeRef);

        checkNumOfEvents(4);

        IntStream.of(1, 2, 3, 4).forEach(i -> {
            RepoEvent<EventData<NodeResource>> resultRepoEvent = getRepoEventWithoutWait(i);
            assertEquals("Wrong repo event type.", EventType.NODE_CREATED.getType(), resultRepoEvent.getType());
        });

        retryingTransactionHelper.doInTransaction(() -> {
            for (NodeRef child : children)
            {
                nodeService.addChild(parentNodeRef, child, ContentModel.ASSOC_CONTAINS,
                        QName.createQName(TEST_NAMESPACE, GUID.generate()));
            }
            return null;
        });

        retryingTransactionHelper.doInTransaction(() -> {
            List<ChildAssociationRef> childAssocParent = nodeService.getChildAssocs(parentNodeRef);

            assertEquals(3, childAssocParent.size());
            return null;
        });

        checkNumOfEvents(10);

        // 3 node.Updated events should be created
        List<RepoEvent<EventData<ChildAssociationResource>>> nodeUpdateEvents = getFilteredEvents(EventType.NODE_UPDATED);
        assertEquals("Wrong node update events number", 3, nodeUpdateEvents.size());

        // 3 assoc.child.Created events should be created
        List<RepoEvent<EventData<ChildAssociationResource>>> childAssocEvents = getFilteredEvents(EventType.CHILD_ASSOC_CREATED);
        assertEquals("Wrong association events number", 3, childAssocEvents.size());
    }

    @Test
    public void testOneParentMultipleChildrenDifferentTransaction()
    {
        final NodeRef parentNodeRef = createNode(ContentModel.TYPE_FOLDER);
        final NodeRef child1NodeRef = createNode(ContentModel.TYPE_CONTENT);
        final NodeRef child2NodeRef = createNode(ContentModel.TYPE_CONTENT);
        final NodeRef child3NodeRef = createNode(ContentModel.TYPE_CONTENT);

        List<NodeRef> children = Arrays.asList(child1NodeRef, child2NodeRef, child3NodeRef);

        checkNumOfEvents(4);

        IntStream.of(1, 2, 3, 4).forEach(i -> {
            RepoEvent<EventData<NodeResource>> resultRepoEvent = getRepoEventWithoutWait(i);
            assertEquals("Wrong repo event type.", EventType.NODE_CREATED.getType(), resultRepoEvent.getType());
        });

        for (NodeRef child : children)
        {
            retryingTransactionHelper.doInTransaction(() -> nodeService.addChild(parentNodeRef, child, ContentModel.ASSOC_CONTAINS,
                    QName.createQName(TEST_NAMESPACE, GUID.generate())));
        }

        retryingTransactionHelper.doInTransaction(() -> {
            List<ChildAssociationRef> childAssocParent = nodeService.getChildAssocs(parentNodeRef);

            assertEquals(3, childAssocParent.size());
            return null;
        });

        checkNumOfEvents(10);

        // 3 node.Updated events should be created
        List<RepoEvent<EventData<ChildAssociationResource>>> nodeUpdateEvents = getFilteredEvents(EventType.NODE_UPDATED);
        assertEquals("Wrong node update events number", 3, nodeUpdateEvents.size());

        // 3 assoc.child.Created events should be created
        List<RepoEvent<EventData<ChildAssociationResource>>> childAssocEvents = getFilteredEvents(EventType.CHILD_ASSOC_CREATED);
        assertEquals("Wrong association events number", 3, childAssocEvents.size());

        assertEquals(parentNodeRef.getId(), getChildAssocResource(childAssocEvents.get(0)).getParent().getId());
        assertEquals(child1NodeRef.getId(), getChildAssocResource(childAssocEvents.get(0)).getChild().getId());
        assertEquals("cm:contains", getChildAssocResource(childAssocEvents.get(0)).getAssocType());

        assertEquals(parentNodeRef.getId(), getChildAssocResource(childAssocEvents.get(1)).getParent().getId());
        assertEquals(child2NodeRef.getId(), getChildAssocResource(childAssocEvents.get(1)).getChild().getId());
        assertEquals("cm:contains", getChildAssocResource(childAssocEvents.get(1)).getAssocType());

        assertEquals(parentNodeRef.getId(), getChildAssocResource(childAssocEvents.get(2)).getParent().getId());
        assertEquals(child3NodeRef.getId(), getChildAssocResource(childAssocEvents.get(2)).getChild().getId());
        assertEquals("cm:contains", getChildAssocResource(childAssocEvents.get(2)).getAssocType());
    }

    @Test
    public void testDeleteAssociationsOneChildMultipleParentsSameTransaction()
    {
        final NodeRef parent1NodeRef = createNode(ContentModel.TYPE_FOLDER);
        final NodeRef parent2NodeRef = createNode(ContentModel.TYPE_FOLDER);
        final NodeRef parent3NodeRef = createNode(ContentModel.TYPE_FOLDER);
        final NodeRef childNodeRef = createNode(ContentModel.TYPE_CONTENT);

        List<NodeRef> parents = Arrays.asList(parent1NodeRef, parent2NodeRef, parent3NodeRef);

        checkNumOfEvents(4);

        IntStream.of(1, 2, 3, 4).forEach(i -> {
            RepoEvent<EventData<NodeResource>> resultRepoEvent = getRepoEventWithoutWait(i);
            assertEquals("Wrong repo event type.", EventType.NODE_CREATED.getType(), resultRepoEvent.getType());
        });

        retryingTransactionHelper.doInTransaction(() -> nodeService.addChild(parents, childNodeRef, ContentModel.ASSOC_CONTAINS,
                QName.createQName(TEST_NAMESPACE, GUID.generate())));

        List<ChildAssociationRef> listChildAssociationRefs = retryingTransactionHelper.doInTransaction(() -> {
            List<ChildAssociationRef> childAssocParent1 = nodeService.getChildAssocs(
                    parent1NodeRef);
            List<ChildAssociationRef> childAssocParent2 = nodeService.getChildAssocs(
                    parent2NodeRef);
            List<ChildAssociationRef> childAssocParent3 = nodeService.getChildAssocs(
                    parent3NodeRef);

            assertEquals(1, childAssocParent1.size());
            assertEquals(1, childAssocParent2.size());
            assertEquals(1, childAssocParent3.size());

            return Arrays.asList(childAssocParent1.get(0), childAssocParent2.get(0), childAssocParent3.get(0));
        });

        retryingTransactionHelper.doInTransaction(() -> {
            for (ChildAssociationRef childAssociationRef : listChildAssociationRefs)
            {
                nodeService.removeChildAssociation(childAssociationRef);
            }
            return null;
        });

        checkNumOfEvents(12);

        // 2 node.Updated events should be created
        List<RepoEvent<EventData<ChildAssociationResource>>> nodeUpdateEvents = getFilteredEvents(EventType.NODE_UPDATED);
        assertEquals("Wrong node update events number", 2, nodeUpdateEvents.size());

        // 3 assoc.child.Deleted events should be created
        List<RepoEvent<EventData<ChildAssociationResource>>> childAssocEvents = getFilteredEvents(EventType.CHILD_ASSOC_DELETED);
        assertEquals("Wrong association events number", 3, childAssocEvents.size());
    }

    @Test
    public void testDeleteAssociationMultipleParentOneChildrenDifferentTransactions()
    {
        final NodeRef parent1NodeRef = createNode(ContentModel.TYPE_FOLDER);
        final NodeRef parent2NodeRef = createNode(ContentModel.TYPE_FOLDER);
        final NodeRef parent3NodeRef = createNode(ContentModel.TYPE_FOLDER);
        final NodeRef childNodeRef = createNode(ContentModel.TYPE_CONTENT);

        List<NodeRef> parents = Arrays.asList(parent1NodeRef, parent2NodeRef, parent3NodeRef);

        checkNumOfEvents(4);

        IntStream.of(1, 2, 3, 4).forEach(i -> {
            RepoEvent<EventData<NodeResource>> resultRepoEvent = getRepoEventWithoutWait(i);
            assertEquals("Wrong repo event type.", EventType.NODE_CREATED.getType(), resultRepoEvent.getType());
        });

        retryingTransactionHelper.doInTransaction(() -> nodeService.addChild(parents, childNodeRef, ContentModel.ASSOC_CONTAINS,
                QName.createQName(TEST_NAMESPACE, GUID.generate())));

        List<ChildAssociationRef> listChildAssociationRefs = retryingTransactionHelper.doInTransaction(() -> {
            List<ChildAssociationRef> childAssocParent1 = nodeService.getChildAssocs(
                    parent1NodeRef);
            List<ChildAssociationRef> childAssocParent2 = nodeService.getChildAssocs(
                    parent2NodeRef);
            List<ChildAssociationRef> childAssocParent3 = nodeService.getChildAssocs(
                    parent3NodeRef);

            assertEquals(1, childAssocParent1.size());
            assertEquals(1, childAssocParent2.size());
            assertEquals(1, childAssocParent3.size());

            return Arrays.asList(childAssocParent1.get(0), childAssocParent2.get(0), childAssocParent3.get(0));
        });

        for (ChildAssociationRef childAssociationRef : listChildAssociationRefs)
        {
            retryingTransactionHelper.doInTransaction(() -> nodeService.removeChildAssociation(childAssociationRef));
        }

        checkNumOfEvents(14);

        // 3 assoc.child.Deleted events should be created
        List<RepoEvent<EventData<ChildAssociationResource>>> childAssocEvents = getFilteredEvents(EventType.CHILD_ASSOC_DELETED);
        assertEquals("Wrong association events number", 3, childAssocEvents.size());

        assertEquals(parent1NodeRef.getId(), getChildAssocResource(childAssocEvents.get(0)).getParent().getId());
        assertEquals(childNodeRef.getId(), getChildAssocResource(childAssocEvents.get(0)).getChild().getId());
        assertEquals("cm:contains", getChildAssocResource(childAssocEvents.get(0)).getAssocType());

        assertEquals(parent2NodeRef.getId(), getChildAssocResource(childAssocEvents.get(1)).getParent().getId());
        assertEquals(childNodeRef.getId(), getChildAssocResource(childAssocEvents.get(1)).getChild().getId());
        assertEquals("cm:contains", getChildAssocResource(childAssocEvents.get(1)).getAssocType());

        assertEquals(parent3NodeRef.getId(), getChildAssocResource(childAssocEvents.get(2)).getParent().getId());
        assertEquals(childNodeRef.getId(), getChildAssocResource(childAssocEvents.get(2)).getChild().getId());
        assertEquals("cm:contains", getChildAssocResource(childAssocEvents.get(2)).getAssocType());
    }

    @Test
    public void testDeleteParentWithMultipleChildAssociations()
    {
        final NodeRef parentNodeRef = createNode(ContentModel.TYPE_FOLDER);
        final NodeRef child1NodeRef = createNode(ContentModel.TYPE_CONTENT);
        final NodeRef child2NodeRef = createNode(ContentModel.TYPE_CONTENT);
        final NodeRef child3NodeRef = createNode(ContentModel.TYPE_CONTENT);

        List<NodeRef> children = Arrays.asList(child1NodeRef, child2NodeRef, child3NodeRef);

        checkNumOfEvents(4);

        IntStream.of(1, 2, 3, 4).forEach(i -> {
            RepoEvent<EventData<NodeResource>> resultRepoEvent = getRepoEventWithoutWait(i);
            assertEquals("Wrong repo event type.", EventType.NODE_CREATED.getType(), resultRepoEvent.getType());
        });

        retryingTransactionHelper.doInTransaction(() -> {
            for (NodeRef child : children)
            {
                nodeService.addChild(parentNodeRef, child, ContentModel.ASSOC_CONTAINS,
                        QName.createQName(TEST_NAMESPACE, GUID.generate()));
            }

            return null;
        });

        retryingTransactionHelper.doInTransaction(() -> {
            List<ChildAssociationRef> childAssocParent = nodeService.getChildAssocs(parentNodeRef);

            assertEquals(3, childAssocParent.size());
            return null;
        });

        deleteNode(parentNodeRef);

        checkNumOfEvents(17);

        // 6 node.Updated events should be created
        List<RepoEvent<EventData<ChildAssociationResource>>> nodeUpdateEvents = getFilteredEvents(EventType.NODE_UPDATED);
        assertEquals("Wrong node update events number", 6, nodeUpdateEvents.size());

        // 3 assoc.child.Deleted events should be created
        List<RepoEvent<EventData<ChildAssociationResource>>> childAssocEvents = getFilteredEvents(EventType.CHILD_ASSOC_DELETED);
        assertEquals("Wrong association events number", 3, childAssocEvents.size());
    }

    @Test
    public void testDeleteChildWithMultipleParentAssociations()
    {
        final NodeRef parent1NodeRef = createNode(ContentModel.TYPE_FOLDER);
        final NodeRef parent2NodeRef = createNode(ContentModel.TYPE_FOLDER);
        final NodeRef parent3NodeRef = createNode(ContentModel.TYPE_FOLDER);
        final NodeRef childNodeRef = createNode(ContentModel.TYPE_CONTENT);

        List<NodeRef> parents = Arrays.asList(parent1NodeRef, parent2NodeRef, parent3NodeRef);

        checkNumOfEvents(4);

        IntStream.of(1, 2, 3, 4).forEach(i -> {
            RepoEvent<EventData<NodeResource>> resultRepoEvent = getRepoEventWithoutWait(i);
            assertEquals("Wrong repo event type.", EventType.NODE_CREATED.getType(), resultRepoEvent.getType());
        });

        retryingTransactionHelper.doInTransaction(() -> nodeService.addChild(parents, childNodeRef, ContentModel.ASSOC_CONTAINS,
                QName.createQName(TEST_NAMESPACE, GUID.generate())));

        retryingTransactionHelper.doInTransaction(() -> {
            List<ChildAssociationRef> childAssocParent1 = nodeService.getChildAssocs(
                    parent1NodeRef);
            List<ChildAssociationRef> childAssocParent2 = nodeService.getChildAssocs(
                    parent2NodeRef);
            List<ChildAssociationRef> childAssocParent3 = nodeService.getChildAssocs(
                    parent3NodeRef);

            assertEquals(1, childAssocParent1.size());
            assertEquals(1, childAssocParent2.size());
            assertEquals(1, childAssocParent3.size());
            return null;
        });

        deleteNode(childNodeRef);

        checkNumOfEvents(12);

        // 2 node.Updated events should be created
        List<RepoEvent<EventData<ChildAssociationResource>>> nodeUpdateEvents = getFilteredEvents(EventType.NODE_UPDATED);
        assertEquals("Wrong node update events number", 2, nodeUpdateEvents.size());

        // 3 assoc.child.Deleted events should be created
        List<RepoEvent<EventData<ChildAssociationResource>>> childAssocEvents = getFilteredEvents(EventType.CHILD_ASSOC_DELETED);
        assertEquals("Wrong association events number", 3, childAssocEvents.size());
    }

    @Test
    public void testUpdateNodeAddChildAssociationNodeEventsFirst()
    {
        final NodeRef parentNodeRef = createNode(ContentModel.TYPE_CONTENT);
        final NodeRef childNodeRef = createNode(ContentModel.TYPE_CONTENT);

        checkNumOfEvents(2);

        IntStream.of(1, 2).forEach(i -> {
            RepoEvent<EventData<NodeResource>> resultRepoEvent = getRepoEventWithoutWait(i);
            assertEquals("Wrong repo event type.", EventType.NODE_CREATED.getType(), resultRepoEvent.getType());
        });

        retryingTransactionHelper.doInTransaction(() -> {
            nodeService.setType(parentNodeRef, ContentModel.TYPE_FOLDER);

            return nodeService.addChild(
                    parentNodeRef,
                    childNodeRef,
                    ContentModel.ASSOC_CONTAINS,
                    QName.createQName(TEST_NAMESPACE, GUID.generate()));
        });

        List<ChildAssociationRef> childAssociationRefs = retryingTransactionHelper.doInTransaction(() -> nodeService.getChildAssocs(parentNodeRef));

        assertEquals(1, childAssociationRefs.size());
        assertFalse(childAssociationRefs.get(0).isPrimary());

        checkNumOfEvents(5);

        // Check the node events occur before the child association event
        List<RepoEvent<?>> repoEvents = getRepoEventsContainer().getEvents();
        assertEquals(EventType.NODE_CREATED.getType(), repoEvents.get(0).getType());
        assertEquals(EventType.NODE_CREATED.getType(), repoEvents.get(1).getType());
        assertEquals(EventType.NODE_UPDATED.getType(), repoEvents.get(2).getType());
        assertEquals(EventType.NODE_UPDATED.getType(), repoEvents.get(3).getType());
        assertEquals(EventType.CHILD_ASSOC_CREATED.getType(), repoEvents.get(4).getType());
    }
}
