import { Component, OnChanges, Input } from '@angular/core';
import { Entity } from '../../model/Entity';
import { EntityView } from '../../model/EntityView';
import { EntityViewService } from '../../api/entity-view.service';
import { EntityApiService } from '../../api/entity-api.service';
import { MatSidenav, MatDialog } from '@angular/material';
import { ConfirmDialogComponent } from '@alfresco/adf-content-services';

@Component({
  selector: 'app-entity-views',
  templateUrl: './entity-views.component.html',
  styleUrls: ['./entity-views.component.scss']
})
export class EntityViewsComponent implements OnChanges {

  @Input()
  entity: Entity;

  @Input()
  public leftPane: MatSidenav;

  entityViews: EntityView[];

  editMode = true;

  constructor(private entityViewService: EntityViewService,
              private entityApiService: EntityApiService,
              private dialog: MatDialog) { }

  ngOnChanges() {
    if(this.entity){
      if (this.entity.isEntityTemplate) {
        this.editMode = true;
      }


      this.entityViews = this.entityViewService.getViews(this.entity);
    }
  }

  createView() {

  }

  editView(view: EntityView) {

  }

  deleteView(view: EntityView) {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
          title: 'DELETE_ENTITY_LIST',
          message: `Are you sure you want to delete ${view.list.name}?`
      },
      minWidth: '250px'
  });

  dialogRef.afterClosed().subscribe((result) => {
      if (result === true) {
        this.entityViewService.deleteEntityListView(view.list.id)
        .then(
          (success) => {
            // TODO update the entity
            console.log('Success: ' + JSON.stringify(success));
          },
          (error) => {
            console.log('Error: ' + JSON.stringify(error));
          });
      }
  });
  }

  showOrHideActionButton(view: any, value: boolean): void{
    view.showActionButton = value;
  }

  changeViewListState(view: EntityView): void{
    const state = view.isValid ? "Valid" : "ToValidate";
    this.entityViewService.updateEntityListState(view.list.id, state)
    .then(
      (success) => {
         console.log('Success' +  JSON.stringify(success));
      }, (error) => {
        console.log('Error' + JSON.stringify(error));
      });;
  }
}
