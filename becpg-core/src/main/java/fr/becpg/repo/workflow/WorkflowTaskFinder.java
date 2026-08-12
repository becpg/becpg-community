package fr.becpg.repo.workflow;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * <p>« Cet utilisateur a-t-il une tâche en cours sur ces workflows ? » — posée à Activiti
 * dans le sens où il sait répondre.</p>
 *
 * <p>Deux endroits posaient cette question, chacun avec sa copie du même code lent :
 * {@code EntitySecurityWebScript.checkUserHasAssignedTask} et
 * {@code SupplierSecurityPlugin.hasMatchingTask}. Tous deux listaient <b>toutes</b> les tâches
 * de l'utilisateur, puis filtraient en mémoire sur
 * {@code task.getPath().getInstance().getId()} — un accès qui charge paresseusement le chemin
 * puis l'instance de workflow, <b>une fois par tâche</b>. Le coût suivait donc le nombre de
 * tâches du compte, alors que la question porte sur une ou deux instances connues d'avance.</p>
 *
 * <p>Mesuré à travers le portail fournisseur le 2026-08-12, sur un compte ayant accumulé des
 * tâches : 5 011 ms pour {@code becpg/security/entitylists/check}, dont 2 700 ms pour le seul
 * {@code computeAccessMode} — c'est-à-dire pour le plugin fournisseur.</p>
 *
 * <h3>Ce que fait cette classe</h3>
 *
 * <p>Elle interroge <b>workflow par workflow</b>. {@link WorkflowTaskQuery} accepte
 * {@code processId} et {@code actorId} : Activiti répond alors par un index, et les tâches
 * rendues appartiennent <b>par construction</b> au bon workflow — il n'y a plus rien à filtrer,
 * donc plus de chargement du chemin ni de l'instance. La boucle tourne une ou deux fois, pas
 * autant de fois que le fournisseur a de tâches.</p>
 *
 * <h3>La limite, et pourquoi elle est assumée</h3>
 *
 * <p>Les tâches <b>en pool</b> ne sont pas exprimables ainsi : {@link WorkflowTaskQuery} n'a
 * aucun réglage pour les groupes candidats — vérifié contre {@code alfresco-repository 26.1.0.61},
 * il n'expose que {@code setProcessId}, {@code setActorId}, {@code setTaskState} et
 * {@code setActive}. Le balayage reste donc le seul recours pour elles, et il est ici un
 * <b>repli</b> : un fournisseur travaillant sur sa propre tâche sort par la voie rapide.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service("workflowTaskFinder")
public class WorkflowTaskFinder {

	@Autowired
	@Qualifier("WorkflowService")
	private WorkflowService workflowService;

	/**
	 * <p>L'utilisateur détient-il une tâche en cours sur l'un de ces workflows ?</p>
	 *
	 * <p>Les tâches assignées sont interrogées d'abord, par requête bornée ; les tâches en pool
	 * ne sont balayées que si les premières n'ont rien donné.</p>
	 *
	 * @param actorId l'utilisateur, tel qu'Activiti le connaît
	 * @param workflowIds les instances de workflow portées par le contenu — une ou deux
	 * @param accept filtre supplémentaire sur la tâche, ou {@code null} pour tout accepter.
	 *        Il n'est évalué que sur les tâches déjà retenues : un prédicat coûteux ne l'est
	 *        donc que sur les rares candidates, jamais sur tout le carnet.
	 * @return true dès la première tâche qui correspond
	 */
	public boolean hasTaskOn(String actorId, Collection<String> workflowIds, Predicate<WorkflowTask> accept) {
		if ((actorId == null) || (workflowIds == null) || workflowIds.isEmpty()) {
			return false;
		}

		Predicate<WorkflowTask> filter = (accept != null) ? accept : task -> true;

		for (String workflowId : workflowIds) {
			WorkflowTaskQuery query = new WorkflowTaskQuery();
			query.setProcessId(workflowId);
			query.setActorId(actorId);
			query.setTaskState(WorkflowTaskState.IN_PROGRESS);
			query.setActive(Boolean.TRUE);

			for (WorkflowTask task : workflowService.queryTasks(query)) {
				if (filter.test(task)) {
					return true;
				}
			}
		}

		return hasPooledTaskOn(actorId, workflowIds, filter);
	}

	/**
	 * <p>Le repli : les tâches en pool, faute d'API pour les interroger par workflow.</p>
	 *
	 * <p>Le filtre sur l'instance reste nécessaire ici, avec son chargement paresseux — mais il
	 * n'est payé que par les comptes qui n'ont aucune tâche assignée sur ces workflows.</p>
	 *
	 * @param actorId l'utilisateur
	 * @param workflowIds les instances recherchées
	 * @param filter filtre supplémentaire, jamais {@code null}
	 * @return true dès la première tâche qui correspond
	 */
	private boolean hasPooledTaskOn(String actorId, Collection<String> workflowIds, Predicate<WorkflowTask> filter) {
		List<WorkflowTask> pooledTasks = workflowService.getPooledTasks(actorId);
		for (WorkflowTask task : pooledTasks) {
			if (workflowIds.contains(task.getPath().getInstance().getId()) && filter.test(task)) {
				return true;
			}
		}
		return false;
	}

}
