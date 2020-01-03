import { Component, OnInit, Input } from '@angular/core';
import { Entity } from 'app/becpg/model/Entity';
import { EntityView } from 'app/becpg/model/EntityView';

@Component({
  selector: 'app-entity-views',
  templateUrl: './entity-views.component.html',
  styleUrls: ['./entity-views.component.scss']
})
export class EntityViewsComponent implements OnInit {

  @Input()
  entity: Entity;

  entityView: EntityView;

  editMode = false;

  constructor() { }

  ngOnInit() {
    if(this.entity.isEntityTemplate){
      this.editMode = true;
    }

  }

  createView() {

  }

  editView(view: EntityView) {

  }

  deleteView(view: EntityView) {

  }

}
