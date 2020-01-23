import { Component, OnInit, ViewChild, Input, OnChanges } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { EntityApiService } from '../../api/entity-api.service';
import { EntityViewService } from '../../api/entity-view.service';
import { Breakpoints, BreakpointObserver } from '@angular/cdk/layout';
import { Entity } from '../../model/Entity';
import { EntityView } from '../../model/EntityView';
import { Observable, Subscription } from 'rxjs';
import { switchMap, mergeMap } from 'rxjs/operators';
import { ContentActionRef, SelectionState } from '@alfresco/adf-extensions';

import { Store } from '@ngrx/store';


@Component({
  selector: 'app-entity',
  templateUrl: './entity.component.html',
  styleUrls: ['./entity.component.scss']
})
export class EntityComponent implements OnInit {

  nodeId: String;

  entity: Observable<Entity>;

  view: EntityView;

  infoDrawerOpened$: Observable<boolean>;
  selection: SelectionState;
  actions: Array<ContentActionRef> = [];

  protected subscriptions: Subscription[] = [];



  constructor(private route: ActivatedRoute,
    private entityViewService: EntityViewService,
    private entityApiService: EntityApiService) { }

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
      this.view = this.entityViewService.getView(entity, viewId);

    });


    //  this.infoDrawerOpened$ = this.store.select(isInfoDrawerOpened);

    //  this.store
    //    .select(getAppSelection)
    //    .pipe(takeUntil(this.onDestroy$))
    //    .subscribe(selection => {
    //      this.selection = selection;
    //      this.actions = this.extensions.getAllowedToolbarActions();
    //      this.viewerToolbarActions = this.extensions.getViewerToolbarActions();
    //      this.canUpdateNode =
    //        this.selection.count === 1 &&
    //        this.content.canUpdateNode(selection.first);
    //    });

    //  this.store
    //    .select(getCurrentFolder)
    //    .pipe(takeUntil(this.onDestroy$))
    //    .subscribe(node => {
    //      this.canUpload = node && this.content.canUploadContent(node);
    //    });


  }






}
