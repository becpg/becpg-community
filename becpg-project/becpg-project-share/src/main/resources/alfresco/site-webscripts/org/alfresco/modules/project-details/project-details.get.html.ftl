<#assign el=args.htmlid?html>

<@markup class="html">
    <@uniqueIdDiv>
<div id="${el}" class="project-details">
<!-- Begin Evaluation Widget-->
    <div class="project-details-evolution white-block">
    	<div class="yui-g"> 
		    <div class="yui-g first">
	            <span class="block-title">${msg("projectdetails.projectevolution.title")}</span>
            </div>
            <div class="yui-g"> 
	            <span class="black-bold-text medium-title">${projectDetails.projectName}</span>
           	</div>
       	</div>
        <div class= "hr"></div>
        <div class="yui-gb display-flex">
            <div class="yui-u">
                <span class="medium-title">${msg("projectdetails.projectevolution.text1")}</span>
                <#if projectDetails.overdueDays ??><span class="big-number red-text">${projectDetails.overdueDays!""} </span>
                <span class="vertical-top">${msg("projectdetails.projectevolution.dayslate")}</span></#if>
            </div>
            <div class="yui-u">
                <#if projectDetails.dueDate ??><span class="medium-title ">${msg("projectdetails.projectevolution.dueDate")}</span>
                <span class="vertical-top">${projectDetails.dueDate!""}</span></#if>
            </div>
            <div class="yui-u txt-align-end">
                <#assign state = projectDetails.state>
                <span class="medium-title" <#if state = "InProgress"> style ="color: #17a554
                    <#elseif state = "Completed"> style ="color: red
                    <#else> style ="color: blue"</#if>">${msg("projectdetails.project.state.${projectDetails.state}")}</span>
               <#if projectDetails.remainingDays ?? ><span class="vertical-top">${projectDetails.remainingDays} ${msg("projectdetails.projectevolution.remainingDays")}</span></#if>
            </div>
        </div>
<!-- End of Evaluation Widget-->
<!-- Begin graph evaluation part-->
        <div class="chart">
            <div id="${el}-chart"></div>
        </div>
        <div class="yui-g">
            <div class="yui-u-1-2">
                <#if projectDetails.startDate?? ><span>${msg("projectdetails.start")}</span>
                <span class="black-text">${projectDetails.startDate}</span>
                <#else><span class="black-text"><${msg("projectdetails.nosatartdate")}</span></#if>
            <div class="yui-u-1-2 float-right">
                <#if projectDetails.dueDate ??><span>${msg("projectdetails.end")}</span>
                <span class="black-text">${projectDetails.dueDate}</span>
                <#else><span class="black-text"><${msg("projectdetails.noduedate")}</span></#if>
            </div>
            </div>
        </div>
    </div>
<!--End of graph evaluation part-->
<!--Progress statment bars & miletones --> 
    <#if projectDetails.totalTaskNumber != 0>
    <div class="projct-details-advancement white-block">
        <div class="yui-g"> 
		    <div class="yui-g first">
            	<span class="block-title">${msg("projectdetails.progressstatement.title")}</span>
            </div>
            <div class="yui-u"> 
	            <span class="black-bold-text">${projectDetails.totalTaskNumber} ${msg("projectdetails.progressstatement.tasks")}</span>
            </div>
        </div>
        <div class= "hr"></div>
        <div class="myProgress">
            <div class="blueBar" style="width: ${projectDetails.completionPerc}%">
                <span class="bar-description"> ${projectDetails.completionPerc}% ${msg("projectdetails.legend.realised")}</span>
            </div>
        </div>
        <div class="myProgress"><#assign openedWork = 100-projectDetails.completionPerc>
            <div class="lightBlueBar" style="width: ${openedWork}%">
                <span class="bar-description">${openedWork}% ${msg("projectdetails.legend.inprogresswork")}</span>
            </div>
        </div>
        <div class="myProgress">
            <div class="redBar" style="width: ${projectDetails.overduePerc?string(",##0")}%">
                <span class="bar-description float-right"> ${projectDetails.overduePerc?string(",##0")}% ${msg("projectdetails.legend.overdue")}</span>
            </div>
        </div>
    </div>
    <#else>
    <div class="projct-details-advancement white-block">
		<div class="yui-g"> 
		    <div class="yui-g first">
                <span class="block-title">${msg("projectdetails.progressstatement")}</span>
            </div>
            <div class="yui-g"> 
	            <span class="black-bold-text"> ${msg("projectdetails.progressstatement.notasks")}</span>
            </div>
        </div>
    </div>
    </#if>
    <#if projectDetails.milestoneSum !=0 >
    <div class="project-details-milestone white-block">
        <div class="milestone-box">
            <div class="yui-g"> 
            	<div class="yui-g first"> 
                    <span class="bold black-text medium-title">${projectDetails.milestoneSum} ${msg("projectdetails.progressstatement.milestones")}</span>
                	<div class="yui-g myProgress">
	                    <#assign mlReleasedPerc = projectDetails.milestoneReleased/projectDetails.milestoneSum * 100>
	                    <div class="blueBar" style="width:${mlReleasedPerc?string(",##0")}%">
	                        <span class=" bar-description black-text">${projectDetails.milestoneReleased} ${msg("projectdetails.legend.realised")}</span>
	                    </div>
                	</div>
            	</div>
	            <div class="yui-g txt-align-end">
    	            <div class="yui-u-1">
            	        <span class="black-bold-text medium-title task-milestone"> ${msg("projectdetails.milestones.nextmilestones")}</span>
                	</div>
                	<div class="yui-u-1">
                        <#if projectDetails.nextMilestoneTask.taskName??>
                             <span class="black-text margin-top">${projectDetails.nextMilestoneTask.taskName}</span>
                             <#if projectDetails.nextMilestoneTask.taskStart?exists><span class="black-text bold">${projectDetails.nextMilestoneTask.taskStart}</span>
                             <#else> <span class="black-text bold">${msg("projectDetails.nextMilestoneTask.noTaskStartDate")}</span></#if>
                        <#else><span class="margin-top black-text"> ${msg("projectdetails.noMilestones")}</#if></span>
                	</div>
            	</div>
        	</div>
    	</div>
	</div>
    </#if>   
    
    <#if projectDetails.userTaskSum != 0 >
    <div class="project-details-useradvancement white-block">
        <div class="yui-g"> 
		    <div class="yui-g first">
		        <span class="block-title">${msg("projectdetails.userprogressstatement")} </span>
         	</div>
		    <div class="yui-g"> 
               <span class="black-bold-text">${projectDetails.userTaskSum} ${msg("projectdetails.progressstatement.tasks")}</span>
        	</div>
    	</div>
        <div class= "hr"></div>
        <div class="yui-g">
            <div class="myProgress">
                <div class="mauveBar" style="width:${projectDetails.userTaskCompletionPerc?string(",##0")}%">
                    <span class="bar-description">${projectDetails.userTaskCompletionPerc}% ${msg("projectdetails.legend.realised")}</span>
                </div>
            </div>
        </div>
        <div class="yui-g">
            <#assign restUserTaskCompletionPerc = 100 - projectDetails.userTaskCompletionPerc>
            <div class="myProgress">
                <div class="lightmauveBar" style="width:${restUserTaskCompletionPerc?string(",##0")}%">
                    <span class="bar-description">${restUserTaskCompletionPerc}% ${msg("projectdetails.legend.inprogresswork")}</span>
                </div>
            </div>
        </div>
        <div class="yui-g">
            <div class="myProgress">
                <div class="redBar" style="width:${projectDetails.userOverdueTaskCompletionPerc?string(",##0")}%">
                    <span class="bar-description">${projectDetails.userOverdueTaskCompletionPerc}% ${msg("projectdetails.legend.overdue")}</span>
                </div>
            </div>
        </div>
    </div>
    </#if>

 <!-- task listing--> 
    <#if projectDetails.taskList.commingTaskList?exists && projectDetails.taskList.commingTaskList?size &gt; 0>
    <div class="project-details-comming-tasks white-block">
        <div class="yui-g">
            <div class="green-header">
                <span class="title-block comming-tasks-title">${msg("projectdetails.tasklist.comming")}</span>
                <span class="black-text bold white-btn">${projectDetails.taskList.commingTaskList?size}</span>
                <span class="float-right">
                    <span id="comming-tasks#comming-tasks" class="onFilterDetailsAction">
                        <a class="filter-details-action float-right filter-btn btn active">${msg("projectdetails.tasklist.allTasks")}</a>
                    </span>
                <span class="float-left">
                    <span id="comming-tasks#my-tasks" class="onFilterDetailsAction">
                       <a class="filter-details-action float-right filter-btn btn">${msg("projectdetails.tasklist.myTasks")}</a>
                    </span>
                </span>
            </div> 
        </div>
        <div class="yui-g scrollableList"<#if args.height??>style="height: ${args.height}px;"</#if>>
            <#list projectDetails.taskList.commingTaskList as task>
                <div class="<#if task.isAuthenticatedUser>my-tasks</#if> comming-tasks">
                	<#if task.taskStart ?? >
                    <div class="yui-u-1-2">
                        <span class="black-bold-text">${task.taskStart}</span>
                    </div></#if>
                    <div class="yui-u-1-2 margin-top">
                        <#if task.isMilestoneTask > <span class="task-milestone">${task.taskName}</span>
                        <#else><span class="task">${task.taskName}</span></#if>
                    </div>
                </div>
            </#list>
        </div>
    </div>
    </#if>
	<#if projectDetails.taskList.overdueTaskList?exists && projectDetails.taskList.overdueTaskList?size &gt; 0>
    <div class="project-details-overdue-tasks white-block">
        <div class="yui-g">
            <div class="red-header">
                <span class="title-block overdue-tasks-title">${msg("projectdetails.tasklist.overdue")}</span>
                <span class="black-text bold white-btn">${projectDetails.taskList.overdueTaskList?size}</span>
                <span class="float-right">
                    <span id="overdue-tasks#overdue-tasks" class="onFilterDetailsAction">
                        <a class="filter-details-action float-right filter-btn btn active">${msg("projectdetails.tasklist.allTasks")}</a>
                    </span>
                <span class="float-left">
                    <span id="overdue-tasks#my-tasks" class="onFilterDetailsAction">
                        <a class="filter-details-action float-right filter-btn btn">${msg("projectdetails.tasklist.myTasks")}</a>
                    </span>
               </span>
            </div>
        </div>
            <div class="yui-g scrollableList"<#if args.height??>style="height: ${args.height}px;"</#if>>
                <#list projectDetails.taskList.overdueTaskList as task>
                    <div class=" <#if task.isAuthenticatedUser>my-tasks</#if> overdue-tasks">
                    	<#if task.taskEnd ??>
                        <div class="yui-u-1-2 ">
                              <span class="black-bold-text">${task.taskEnd}</span>
                        </div></#if>
                        <div class="yui-u-1-2 margin-top">
                             <#if task.isMilestoneTask?? && task.isMilestoneTask> <span class="task-milestone">${task.taskName}</span>
                       		 <#else><span class="task">${task.taskName}</span></#if>
                        </div>
                    </div>
                </#list>
            </div>
    </div>    
    </#if>
    <#if projectDetails.comments?exists && projectDetails.comments?size &gt; 0>
    <div class="project-details-comments-block white-block">
        <div class= "yui-g">
            <span><img src="${url.context}/components/images/comment-16.png"/></span>
            <span class="block-title">${msg("projectdetails.comments")} </span>
        </div>
        <div class= "hr"></div>
            <div class="div-resizer scrollableList"<#if args.height??>style="height: ${args.height}px;"</#if>>
            <#list projectDetails.comments as comment>
                <div class="comment-details">
                    <div class="yui-gf">
	                    <div class="yui-gf first">
                            <img class="icon" src ="${url.context}/proxy/alfresco/slingshot/profile/avatar/${comment.commentCreator}/thumbnail/avatar32">
						</div>
                    	<div class="yui-u">
							<div class="yui-u float-left">
	                        	<div class = "yui-u-1">
		                        	<span class="black-text bold comment-creator">${comment.commentCreator}</span>
	    	                	</div>
    	    	            	<div class = "yui-u-1">
        	    	            	<span class="black-text comment-content">${comment.commentContent}</span>
            	    	    	</div>
        	    	    	</div>
							<div class="yui-u first">
    	                    	<span class="black-bold-text date-info">${comment.commentCreationDate}</span>
        	            	</div>
    	            	</div>
                    </div>
                </div>
            </#list>
        </div>
    </div>    
	</#if>
</div>

<script type="text/javascript">
      (function(){
     var projectDetails = new beCPG.component.ProjectDetails("${el}").setOptions(
       {
       entityNodeRef: "${entityNodeRef}",
       graphData : [    <#list projectDetails.graphData as data>
                     ["${data[0]}", ${data[1]}] <#if data_has_next>,</#if>
                       </#list>
        ] 
       }).setMessages(${messages});
       
       
      })();
</script>


</@>    
</@>
