package org.alfresco.rest.tags;

import org.testng.annotations.BeforeClass;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestTagModel;
import org.alfresco.rest.model.RestTagModelsCollection;
import org.alfresco.utility.Utility;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser.ListUserWithRoles;
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;

public class TagsDataPrep extends RestTest
{

    protected static UserModel adminUserModel;
    protected static UserModel userModel;
    protected static ListUserWithRoles usersWithRoles;
    protected static SiteModel siteModel;
    protected static FileModel document;
    protected static FolderModel folder, folder2;
    protected static RestTagModelsCollection folder2tags;
    protected static String documentTagValue, documentTagValue2, folderTagValue;
    protected static RestTagModel documentTag, documentTag2, folderTag, orphanTag, returnedModel;
    protected static RestTagModelsCollection returnedCollection;

    @BeforeClass
    public void init() throws Exception
    {
        // Create users
        adminUserModel = dataUser.getAdminUser();
        userModel = dataUser.createRandomTestUser();
        // Create public site
        siteModel = dataSite.usingUser(adminUserModel).createPublicRandomSite();
        usersWithRoles = dataUser.usingAdmin().addUsersWithRolesToSite(siteModel, UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer, UserRole.SiteContributor);
        document = dataContent.usingUser(adminUserModel).usingSite(siteModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        folder = dataContent.usingUser(adminUserModel).usingSite(siteModel).createFolder();
        folder2 = dataContent.usingUser(adminUserModel).usingSite(siteModel).createFolder();

        documentTagValue = RandomData.getRandomName("tag").toLowerCase();
        documentTagValue2 = RandomData.getRandomName("tag").toLowerCase();
        folderTagValue = RandomData.getRandomName("tag").toLowerCase();

        restClient.authenticateUser(adminUserModel);
        documentTag = restClient.withCoreAPI().usingResource(document).addTag(documentTagValue);
        documentTag2 = restClient.withCoreAPI().usingResource(document).addTag(documentTagValue2);
        folderTag = restClient.withCoreAPI().usingResource(folder).addTag(folderTagValue);
        orphanTag = restClient.withCoreAPI().createSingleTag(RestTagModel.builder().tag(RandomData.getRandomName("orphan-tag").toLowerCase()).create());
        folder2tags = restClient.withCoreAPI().usingResource(folder2).addTags(folderTagValue, documentTagValue);

        // Allow indexing to complete.
        Utility.sleep(500, 60000, () -> {
            returnedCollection = restClient.withParams("maxItems=10000", "where=(tag MATCHES ('*tag*'))")
                    .withCoreAPI().getTags();
            returnedCollection.assertThat().entriesListContains("tag", documentTagValue)
                    .and().entriesListContains("tag", documentTagValue2)
                    .and().entriesListContains("tag", folderTagValue)
                    .and().entriesListContains("tag", orphanTag.getTag());
        });
    }

    protected RestTagModel createTagForDocument(FileModel document)
    {
        String documentTagValue = RandomData.getRandomName("tag");
        return restClient.withCoreAPI().usingResource(document).addTag(documentTagValue);
    }

    protected RestTagModel createTagModelWithId(final String id)
    {
        return createTagModelWithIdAndName(id, RandomData.getRandomName("tag"));
    }

    protected RestTagModel createTagModelWithIdAndName(final String id, final String tag)
    {
        return RestTagModel.builder()
                .id(id)
                .tag(tag)
                .create();
    }

    protected RestTagModel createTagModelWithName(final String tagName)
    {
        return RestTagModel.builder().tag(tagName).create();
    }
}
