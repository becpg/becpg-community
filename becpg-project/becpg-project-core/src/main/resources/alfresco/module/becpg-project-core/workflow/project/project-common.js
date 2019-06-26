function createProjectTask()
{

    if (typeof bpm_workflowDueDate != 'undefined')
        task.dueDate = bpm_workflowDueDate
    if (typeof bpm_workflowPriority != 'undefined')
        task.priority = bpm_workflowPriority;
    if (typeof bpm_workflowDescription != 'undefined')
        task.setDescription(bpm_workflowDescription);
    
    if (typeof bpm_assignees != 'undefined' && bpm_assignees != null)
    {
        logger.log("Users size: "+ bpm_assignees.size());
        for (var i = 0; i < bpm_assignees.size(); i++)
        {

            var assignee = bpm_assignees.get(i);

            if (assignee.properties == null)
            {
                assignee = utils.getNodeFromString(assignee);
            }

            logger.log("Adding user : "+ assignee.properties.userName);
            
            if (bpm_assignees.size() > 1 || (typeof bpm_groupAssignees  != 'undefined' && bpm_groupAssignees != null))
            {
                task.addCandidateUser(assignee.properties.userName);
            }
            else
            {
                task.setAssignee(assignee.properties.userName);
            }
        }
    }

    if (typeof bpm_groupAssignees  != 'undefined' && bpm_groupAssignees != null)
    {
        logger.log("Groups size: "+ bpm_groupAssignees.size());
        for (var i = 0; i < bpm_groupAssignees.size(); i++)
        {

            var groupAssignee = bpm_groupAssignees.get(i);

            if (groupAssignee.properties == null)
            {
                groupAssignee = utils.getNodeFromString(groupAssignee);
            }

            logger.log("Adding group: "+groupAssignee.properties["cm:authorityName"]);
 
            task.addCandidateGroup(groupAssignee.properties["cm:authorityName"]);

        }
    }
}
