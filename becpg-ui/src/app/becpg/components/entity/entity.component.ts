import { Component, OnInit, ViewChild, Input } from '@angular/core';
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
import { NotificationService } from '@alfresco/adf-core';
import { DocumentListComponent } from '@alfresco/adf-content-services';
import { PreviewService } from '../../../services/preview.service';


@Component({
  selector: 'app-entity',
  templateUrl: './entity.component.html',
  styleUrls: ['./entity.component.scss']
})
export class EntityComponent implements OnInit {

  entity: Entity;

  view: EntityView;

  list: EntityList;

  showCreate = true;

  /** Based on the screen size, switch from standard to one column per row */
  cards = this.breakpointObserver.observe(Breakpoints.Handset).pipe(
    map(({ matches }) => {
      if (matches) {
        return [
          { title: 'Card 1', cols: 1, rows: 1 },
          { title: 'Card 2', cols: 1, rows: 1 },
          { title: 'Card 3', cols: 1, rows: 1 },
          { title: 'Card 4', cols: 1, rows: 1 }
        ];
      }

      return [
        { title: 'Card 1', cols: 2, rows: 1, type: 'entity' },
        { title: 'Card 2', cols: 1, rows: 1, type: 'list' },
        { title: 'Card 3', cols: 1, rows: 2, type: 'document' },
        { title: 'Card 4', cols: 1, rows: 1, type: 'properties' }
      ];
    })
  );




  @Input()
  showViewer = false;
  nodeId: string = null;

  @ViewChild('documentList')
  documentList: DocumentListComponent;


  constructor(private route: ActivatedRoute, private entityViewService: EntityViewService, private notificationService: NotificationService, private preview: PreviewService, private breakpointObserver: BreakpointObserver, private entityApiService: EntityApiService) { }

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    let viewId = this.route.snapshot.paramMap.get('viewId');
    if (viewId == null) {
      viewId = 'properties';
    }
    this.entityApiService.getEntity('workspace://SpacesStore/' + id).then(entity => {
      this.entity = entity;
      this.view = this.entityViewService.getView(this.entity, viewId);
    });


  }




  uploadSuccess(event: any) {
    this.notificationService.openSnackMessage('File uploaded');
    this.documentList.reload();
  }

  showPreview(event) {
    const entry = event.value.entry;
    if (entry && entry.isFile) {
      this.preview.showResource(entry.id);
    }
  }

  onGoBack(event: any) {
    this.showViewer = false;
    this.nodeId = null;
  }








}
