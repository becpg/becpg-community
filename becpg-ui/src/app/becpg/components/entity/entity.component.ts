import { Component, OnInit, ViewChild, Input, OnChanges } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { map } from 'rxjs/operators';
import { AlfrescoApi } from '@alfresco/js-api';
import { EntityApiService } from '../../api/entity-api.service';
import { EntityViewService } from '../../api/entity-view.service';
import { Breakpoints, BreakpointObserver } from '@angular/cdk/layout';
import { TableModule } from 'primeng/table';
import { Entity } from '../../model/Entity';
import { EntityListItem } from '../../model/EntityListItem';
import { EntityList } from '../../model/EntityList';
import { EntityView } from '../../model/EntityView';
import { NotificationService, CommentModel } from '@alfresco/adf-core';
import { DocumentListComponent } from '@alfresco/adf-content-services';
import { PreviewService } from '../../../services/preview.service';
import { Observable, of } from 'rxjs';
import { switchMap, mergeMap } from 'rxjs/operators';



@Component({
  selector: 'app-entity',
  templateUrl: './entity.component.html',
  styleUrls: ['./entity.component.scss']
})
export class EntityComponent implements OnInit {

  nodeId: String;

  entity: Observable<Entity>;

  view: EntityView;

  constructor(private route: ActivatedRoute, private entityViewService: EntityViewService, private notificationService: NotificationService, private preview: PreviewService, private breakpointObserver: BreakpointObserver, private entityApiService: EntityApiService) { }

  ngOnInit() {


    const id = this.route.snapshot.paramMap.get('id');
   
   this.entity = this.route.paramMap.pipe(
    switchMap(params => {
      return this.entityApiService.getEntity('workspace://SpacesStore/' + params.get('id'));
    })
   );

   this.entity.subscribe(entity => {
    let viewId = this.route.snapshot.paramMap.get('viewId');
    if (viewId == null) {
        viewId = 'properties';
      }
    this.nodeId = entity.id;
    this.view =   this.entityViewService.getView(entity, viewId);

   });


  }






}
