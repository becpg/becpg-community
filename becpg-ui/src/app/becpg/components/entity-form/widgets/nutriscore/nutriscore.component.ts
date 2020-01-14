import { Component, OnInit } from '@angular/core';
import { baseHost , WidgetComponent, FormService } from '@alfresco/adf-core';

@Component({
  selector: 'app-nutriscore',
  templateUrl: './nutriscore.component.html',
  styleUrls: ['./nutriscore.component.scss']
})
export class NutriscoreComponent extends WidgetComponent implements OnInit {

  constructor(public formService: FormService) {
    super(formService);
}


  ngOnInit() {
  }

}
