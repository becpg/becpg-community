<#assign el=args.htmlid?html>

<@markup class="html">
    <@uniqueIdDiv>
    
<div class="project-details">
<!-- Begin Evaluation Widget-->
    <div class ="project-details-evolution white-block">
            <span class="block-title">${msg("projectdetails.projectevolution")}</span>
            <span class="black-bold-text">${projectDetails.projectName}</span>
        <div class= "hr"></div>
        
        <div class="yui-gb display-flex">
            <div class="yui-u">
                <span class="medium-title">${msg("projectdetails.projectevolution.text1")}</span>
                <#if projectDetails.overdueDays ?? ><span class="big-number red-text">${projectDetails.overdueDays!""} </span>
                <span class="vertical-top">${msg("projectdetails.projectevolution.dayslate")}</span></#if>
            </div>
            <div class="yui-u">
                <#if projectDetails.dueDate ??><span class="medium-title margin-left-20">${msg("projectdetails.projectevolution.dueDate")}</span>
                <span class="vertical-top">${projectDetails.dueDate!""}</span></#if>
            </div>
            <div class="yui-u">
                <#assign state = projectDetails.state>
                <span class=" medium-title" <#if state = "InProgress"> style ="color: #17a554
                    <#elseif state = "Completed"> style ="color: red
                    <#else> style ="color: blue"</#if>">${msg("projectdetails.project.state.${projectDetails.state}")}</span>
               <#if projectDetails.remainingDays ?? ><span class="vertical-top">${projectDetails.remainingDays} ${msg("projectdetails.projectevolution.remainingDays")}</span></#if>
            </div>
        </div>
<!-- End of Evaluation Widget-->
<!-- Begin graph evaluation part-->
        <div class="chart">
            <div class="${el}-chart"></div>
        </div>
        <div class="yui-b">
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
<!--End of graph evaluation part-->
    </div>
<!--Progress statment bars & miletones --> 
    <#if projectDetails.totalTaskNumber !=0 >
    <div class="projct-details-advancement white-block">
        <div class="yui-b">
            <span class="block-title">${msg("projectdetails.progressstatement")}</span>
            <span class="black-bold-text">${projectDetails.totalTaskNumber} ${msg("projectdetails.progressstatement.tasks")}</span>
        </div>
        <hr>
        <div class="yui-b">
            <div class="myProgress">
                <div class="blueBar" style="width: ${projectDetails.completionPerc}%">
                    <span class="bar-description"> ${projectDetails.completionPerc}% ${msg("projectdetails.legend.realised")}</span>
                </div>
            </div>
        </div>
        <div class="yui-b">
            <div class="myProgress"><#assign openedWork = 100-projectDetails.completionPerc>
                <div class="lightBlueBar" style="width: ${openedWork}%">
                    <span class="bar-description">${openedWork}% ${msg("projectdetails.legend.inprogresswork")}</span>
                </div>
            </div>
        </div>
        <div class="yui-b">
            <div class="myProgress">
                <div class="redBar" style="width: ${projectDetails.overduePerc?string(",##0")}%">
                    <span class="bar-description float-right"> ${projectDetails.overduePerc?string(",##0")}% ${msg("projectdetails.legend.overdue")}</span>
                </div>
            </div>
        </div>
    </div>
    <#else>
    <div class="projct-details-advancement white-block">
        <div class="yui-b ">
            <span class="block-title">${msg("projectdetails.progressstatement")}</span>
            <span class="black-bold-text"> ${msg("projectdetails.progressstatement.notasks")}</span>
        </div>
    </div>
    </#if>
    <#if projectDetails.milestoneSum !=0 >
    <div class="project-details-milestone white-block">
        <div class="milestone-box">
            <div class="yui-b first-half"> 
                    <span class="bold black-text medium-title">${projectDetails.milestoneSum} ${msg("projectdetails.progressstatement.milestones")}</span>
            <div class="yui-b">
                <div class="myProgress">
                    <#assign mlReleasedPerc = projectDetails.milestoneReleased/projectDetails.milestoneSum * 100>
                    <div class="blueBar" style="width:${mlReleasedPerc?string(",##0")}%">
                        <span class=" bar-description black-text">${projectDetails.milestoneReleased} ${msg("projectdetails.legend.realised")}</span>
                    </div>
                </div>
             </div>   
        </div>
            <div class="second-half">
                <div class="yui-u-1 margin-right">
                    <img src="${url.context}/modules/project-details/images/flag.svg" alt="">
                    <span class="bold black-text medium-title margin-top"> ${msg("projectdetails.milestones.nextmilestones")}</span>
                </div>
                <div class="yui-u-1">
                        <#if projectDetails.nextMilestoneTask.taskName??>
                             <span class="black-text margin-top">${projectDetails.nextMilestoneTask.taskName}</span>
                             <#if projectDetails.nextMilestoneTask.taskStart?exists><span class="black-text bold">${projectDetails.nextMilestoneTask.taskStart}</span>
                             <#else> <span class="black-text bold">${msg("projectDetails.nextMilestoneTask.no_task_start")}</span></#if>
                        <#else><span class="margin-top black-text"> ${msg("projectdetails.no_milestones")}</#if></span>
                </div>
            </div>
        </div>
    </div>
    </#if>           
    
    <#if projectDetails.userTaskSum != 0 >
    <div class="project-details-useradvancement white-block">
        <div class="yui-b">
            <span class="block-title">${msg("projectdetails.userprogressstatement")} </span>
            <span class="black-bold-text">${projectDetails.userTaskSum} ${msg("projectdetails.progressstatement.tasks")}</span>
        </div>
        <div class= "hr"></div>
        <div class="yui-b">
            <div class="myProgress">
                <div class="mauveBar" style="width:${projectDetails.userTaskCompletionPerc?string(",##0")}%">
                    <span class="bar-description">${projectDetails.userTaskCompletionPerc}% ${msg("projectdetails.legend.realised")}</span>
                </div>
            </div>
        </div>
        <div class="yui-b">
            <#assign restUserTaskCompletionPerc = 100 - projectDetails.userTaskCompletionPerc>
            <div class="myProgress">
                <div class="lightmauveBar" style="width:${restUserTaskCompletionPerc?string(",##0")}%">
                    <span class="bar-description">${restUserTaskCompletionPerc}% ${msg("projectdetails.legend.inprogresswork")}</span>
                </div>
            </div>
        </div>
        <div class="yui-b">
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
        <div class="yui-b">
            <div class="green-header yui-skin-sam">
                <span><img src="${url.context}/modules/project-details/images/check-circle.svg"></span>
                <span class="title-block adjust-margin">${msg("projectdetails.tasklist.comming")}</span>
                <span class="white-btn black-text bold filter-btn">${projectDetails.taskList.commingTaskList?size}</span>
                <span class="float-right">
                    <span class="comming-tasks#comming-tasks" class="onFilterDetailsAction">
                        <a class="filter-details-action float-right filter-btn padding-0 btn active">${msg("projectdetails.tasklist.allTasks")}</a>
                    </span>
                <span class="float-left">
                    <span class="comming-tasks#my-tasks" class="onFilterDetailsAction">
                       <a class="filter-details-action float-right filter-btn padding-0 btn">${msg("projectdetails.tasklist.myTasks")}</a>
                    </span>
                </span>
            </div> 
        </div>
        <div class="yui-b scrollableList"<#if args.height??>style="height: ${args.height}px;"</#if>>
            <#list projectDetails.taskList.commingTaskList as task>
                <div class="<#if task.isAuthenticatedUser>my-tasks</#if> comming-tasks">
                	<#if task.taskStart ?? >
                    <div class="yui-u-1-2 margin-top">
                        <span class="black-bold-text">${task.taskStart}</span>
                    </div></#if>
                    <div class="yui-u-1-2 margin-top">
                        <#if task.isMilestoneTask ><img src="${url.context}/modules/project-details/images/flag.svg" class="margin-left">
                        <#else><img src="${url.context}/modules/project-details/images/check.svg" class="margin-left"></#if>
                        <span class="black-text"> ${task.taskName}</span>
                    </div>
                </div>
            </#list>
        </div>
    </div>
    </#if>
    <div class="project-details-overdue-tasks white-block">
        <div class="yui-b">
            <#if projectDetails.taskList.overdueTaskList?exists && projectDetails.taskList.overdueTaskList?size &gt; 0>
            <div class="red-header yui-skin-sam">
                <span><img src="${url.context}/modules/project-details/images/Exclamation_encircled.svg"></span>
                <span class="title-block adjust-margin">${msg("projectdetails.tasklist.overdue")}</span>
                <span class="white-btn black-text bold filter-btn">${projectDetails.taskList.overdueTaskList?size}</span>
                <span class="float-right">
                    <span class="overdue-tasks#overdue-tasks" class="onFilterDetailsAction">
                        <a class="filter-details-action float-right filter-btn padding-0 btn active">${msg("projectdetails.tasklist.allTasks")}</a>
                    </span>
                <span class="float-left">
                    <span class="overdue-tasks#my-tasks" class="onFilterDetailsAction">
                        <a class="filter-details-action float-right filter-btn padding-0 btn">${msg("projectdetails.tasklist.myTasks")}</a>
                    </span>
               </span>
            </div>
        </div>
            <div class="yui-b scrollableList"<#if args.height??>style="height: ${args.height}px;"</#if>>
                <#list projectDetails.taskList.overdueTaskList as task>
                    <div class=" <#if task.isAuthenticatedUser>my-tasks</#if> overdue-tasks">
                    	<#if task.taskEnd ??>
                        <div class="yui-u-1-2 margin-top">
                              <span class="black-bold-text">${task.taskEnd}</span>
                        </div></#if>
                        <div class="yui-u-1-2 margin-top">
                            <#if task.isMilestoneTask ><img src="${url.context}/modules/project-details/images/flag.svg" class="margin-left">
                            <#else><img src="${url.context}/modules/project-details/images/check.svg" class="margin-left"></#if>
                            <span class="black-text"> ${task.taskName}</span>
                        </div>
                    </div>
                </#list>
                </#if>
            </div>
    </div>    
    <div class="project-details-comments-block white-block">
        <div class= "yui-b">
            <#if projectDetails.comments?exists && projectDetails.comments?size &gt; 0>
            <span><img src="${url.context}/components/images/comment-16.png"/></span>
            <span class="block-title">${msg("projectdetails.comments")} </span>
        </div>
        <div class= "hr"></div>
            <div class="yui-b div-resizer scrollableList"<#if args.height??>style="height: ${args.height}px;"</#if>>
            <#list projectDetails.comments as comment>
                <div class="comment-details">
                    <div class="yui-gc">
                        <div class= "yui-u first">
                            <div class = "yui-gf">
                                <div class= "yui-u first">
                                    <span><img class = "icon" src ="${url.context}/proxy/alfresco/${comment.commentCreatorAvatar!""}"></span>
                                </div>
                                <div class = "yui-u-1">
                                    <span class="black-text bold comment-creator">${comment.commentCreator}</span>
                                </div>
                                <div class = "yui-u-1">
                                    <span class="black-text comment-content">${comment.commentContent}</span>
                                </div>
                            </div> 
                        </div>
                        <div class="yui-u">
                            <span class="black-bold-text date-info">${comment.commentCreationDate}</span>
                        </div>
                    </div>
                </div>
            </#list>
        </div>
            </#if>
    </div>    
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
