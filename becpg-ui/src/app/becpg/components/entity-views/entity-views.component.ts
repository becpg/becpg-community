import { Component, OnChanges, Input } from '@angular/core';
import { Entity } from 'app/becpg/model/Entity';
import { EntityView } from 'app/becpg/model/EntityView';
import { EntityViewService } from 'app/becpg/api/entity-view.service';

@Component({
  selector: 'app-entity-views',
  templateUrl: './entity-views.component.html',
  styleUrls: ['./entity-views.component.scss']
})
export class EntityViewsComponent implements OnChanges {

  @Input()
  entity: Entity;

  entityViews: EntityView[];

  editMode = false;

  constructor(private entityViewService: EntityViewService) { }

  ngOnChanges() {
    if (this.entity.isEntityTemplate) {
      this.editMode = true;
    }
    

    this.entityViews = this.entityViewService.getViews(this.entity);
  }

  createView() {

  }

  editView(view: EntityView) {

  }

  deleteView(view: EntityView) {

  }

}
