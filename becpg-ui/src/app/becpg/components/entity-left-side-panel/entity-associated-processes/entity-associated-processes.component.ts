import { Component, OnInit } from '@angular/core';
import { EntityApiService } from '../../../api/entity-api.service';

@Component({
  selector: 'app-entity-associated-processes',
  templateUrl: './entity-associated-processes.component.html',
  styleUrls: ['./entity-associated-processes.component.scss']
})
export class EntityAssociatedProcessesComponent implements OnInit {


  constructor(private entityApiService: EntityApiService) { }

  ngOnInit() {

  }

}
