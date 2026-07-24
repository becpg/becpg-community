package fr.becpg.test.notification;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.notification.NotificationRuleService;
import fr.becpg.repo.notification.data.NotificationRuleListDataItem;
import fr.becpg.repo.notification.data.RecurringTimeType;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.search.data.DateFilterType;
import fr.becpg.repo.search.data.VersionFilterType;
import fr.becpg.test.PLMBaseTestCase;

public class NotificationRuleServiceIT extends PLMBaseTestCase {

	@Autowired
	private NotificationRuleService notificationRuleService;

	@Autowired
	private AlfrescoRepository<NotificationRuleListDataItem> notificationRepository;

	@Test
	public void testFailingNotificationDoesNotBlockValidNotification() {
		// Clean up existing notification rules to isolate our test
		inWriteTx(() -> {
			List<NodeRef> existing = BeCPGQueryBuilder.createQuery().ofType(BeCPGModel.TYPE_NOTIFICATIONRULELIST).inDB().list();
			for (NodeRef nr : existing) {
				nodeService.addAspect(nr, ContentModel.ASPECT_TEMPORARY, null);
				nodeService.deleteNode(nr);
			}
			return null;
		});

		final NodeRef adminPerson = personService.getPerson(AuthenticationUtil.getAdminUserName());

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -2);
		Date pastDate = cal.getTime();

		final NodeRef failingNotifRef = inWriteTx(() -> {
			NotificationRuleListDataItem failingNotif = new NotificationRuleListDataItem();
			failingNotif.setSubject("Failing Notification QName");
			failingNotif.setNodeType("bcpg:finishedProduct");
			failingNotif.setDateField("");
			failingNotif.setFrequency(1);
			failingNotif.setRecurringTime(RecurringTimeType.Day);
			failingNotif.setFrequencyStartDate(pastDate);
			failingNotif.setAuthorities(List.of(adminPerson));
			failingNotif.setDisabled(false);
			failingNotif.setTimeType(DateFilterType.Before);
			failingNotif.setVersionFilterType(VersionFilterType.NONE);
			failingNotif.setDays(1);
			return notificationRepository.create(getTestFolderNodeRef(), failingNotif).getNodeRef();
		});

		final NodeRef validNotifRef = inWriteTx(() -> {
			NotificationRuleListDataItem validNotif = new NotificationRuleListDataItem();
			validNotif.setSubject("Valid Notification");
			validNotif.setNodeType("bcpg:finishedProduct");
			validNotif.setDateField("cm:created");
			validNotif.setFrequency(1);
			validNotif.setRecurringTime(RecurringTimeType.Day);
			validNotif.setFrequencyStartDate(pastDate);
			validNotif.setAuthorities(List.of(adminPerson));
			validNotif.setDisabled(false);
			validNotif.setTimeType(DateFilterType.Before);
			validNotif.setVersionFilterType(VersionFilterType.NONE);
			validNotif.setDays(1);
			return notificationRepository.create(getTestFolderNodeRef(), validNotif).getNodeRef();
		});

		inWriteTx(() -> {
			notificationRuleService.sendNotifications();
			return null;
		});

		inReadTx(() -> {
			NotificationRuleListDataItem failingNotif = notificationRepository.findOne(failingNotifRef);
			NotificationRuleListDataItem validNotif = notificationRepository.findOne(validNotifRef);

			Assert.assertNotNull("Failing notification must exist", failingNotif);
			Assert.assertNotNull("Valid notification must exist", validNotif);

			Assert.assertNotNull("Failing notification error log should not be null", failingNotif.getErrorLog());
			Assert.assertTrue("Failing notification error log should contain QName exception message",
					failingNotif.getErrorLog().contains("QName"));

			Assert.assertNull("Valid notification error log should be null", validNotif.getErrorLog());
			Assert.assertNotNull("Valid notification frequencyStartDate should be set", validNotif.getFrequencyStartDate());
			
			Assert.assertTrue("Valid notification frequencyStartDate should be updated to a newer date",
					validNotif.getFrequencyStartDate().after(pastDate));

			return null;
		});
	}
}