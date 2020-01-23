import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { EntityApiService } from '../../api/entity-api.service';
import { EntityLink } from '../../model/EntityLink';
import { Observable } from 'rxjs';


@Component({
  selector: 'app-entity-recents',
  templateUrl: './entity-recents.component.html',
  styleUrls: ['./entity-recents.component.scss']
})
export class EntityRecentsComponent implements OnInit {

  entities: Observable<EntityLink[]>;

  constructor(private route: ActivatedRoute, private entityApiService: EntityApiService) { }

  ngOnInit() {

    const id = this.route.snapshot.paramMap.get('id');
    const viewId = this.route.snapshot.paramMap.get('viewId');

    this.entities = this.entityApiService.getRecents(id, viewId);

  }

}
