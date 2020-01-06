import { Component, OnInit, Input } from '@angular/core';
import { Entity } from 'app/becpg/model/Entity';
import {MatSidenav} from '@angular/material/sidenav';

@Component({
  selector: 'app-entity-view-toolbar',
  templateUrl: './entity-view-toolbar.component.html',
  styleUrls: ['./entity-view-toolbar.component.scss']
})
export class EntityViewToolbarComponent implements OnInit {

  @Input()
  leftPane: MatSidenav;

  @Input()
  rightPane: MatSidenav;

  @Input()
  entity: Entity;

  constructor() { }

  ngOnInit() {
  }

}
