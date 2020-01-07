import { Component, OnChanges, Input } from '@angular/core';
import { Entity } from '../../model/Entity';
import { EntityView } from '../../model/EntityView';
import { EntityViewService } from '../../api/entity-view.service';

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

  }

}
