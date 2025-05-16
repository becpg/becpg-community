package fr.becpg.test.project;

import java.util.List;
import java.util.stream.Collectors;

import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.URLEncoder;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.jscript.ProjectScriptHelper;
import fr.becpg.test.RepoBaseTestCase;

public class ProjectScriptHelperIT extends RepoBaseTestCase {

	@Autowired
	ServiceRegistry serviceRegistry;

	@Autowired
	ProjectScriptHelper projectScriptHelper;

	@Test
	public void testCreateDeliverableUrl() {

		ProjectData ret = (ProjectData) inWriteTx(() -> {
			ProjectData projectData = ProjectData.build().withName("Deliverable test")
					.withTaskList(List.of(TaskListDataItem.build().withTaskName("task1"), TaskListDataItem.build().withTaskName("task2")));

			projectData.getAspects().add(BeCPGModel.ASPECT_ENTITY_TPL);

			projectData.setEntities(List.of(alfrescoRepository.create(getTestFolderNodeRef(), RawMaterialData.build().withName("MP1")).getNodeRef(),
					alfrescoRepository.create(getTestFolderNodeRef(), RawMaterialData.build().withName("MP2")).getNodeRef()));

			alfrescoRepository.create(getTestFolderNodeRef(), projectData);

			// {nodeRef} --> replace with project nodeRef
			// {nodeRef|propName} --> replace with project property
			// {nodeRef|xpath:./path} --> replace with nodeRef found in relative project path
			// {assocName} --> replace with association nodeRef
			// {assocName|propName} --> replace with association property
			// {assocName|@type} --> replace with association property
			// {assocName|xpath:./path} --> replace with nodeRef found in relative assoc path

			projectData.withDeliverableList(List.of(DeliverableListDataItem.build().withTasks(List.of(projectData.getTaskList().get(0).getNodeRef()))

					.withUrl(
							"https://entity?nodeRef={nodeRef}&code={nodeRef|bcpg:code}&state={nodeRef|pjt:projectState?format(.%s)}&entities={pjt:projectEntity}")));

			return alfrescoRepository.save(projectData);

		});

		inReadTx(() -> {
			
			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(ret.getNodeRef());

			String url = projectScriptHelper.getDeliverableUrl(new ScriptNode(ret.getDeliverableList().get(0).getNodeRef(), serviceRegistry));

			String url2 = "https://entity?nodeRef=" + URLEncoder.encodeUriComponent(ret.getNodeRef().toString()) + "&code=" +projectData.getCode()
					+ "&state=." + projectData.getState().toString() + "&entities="
					+ URLEncoder.encodeUriComponent(projectData.getEntities().stream().map(NodeRef::toString).collect(Collectors.joining(",")));

			
			Assert.assertEquals(url, url2);

			return null;
		});

	}

}
