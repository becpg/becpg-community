import { Component, OnInit } from '@angular/core';
import { baseHost , WidgetComponent, FormService } from '@alfresco/adf-core';

@Component({
  selector: 'app-color',
  templateUrl: './color.component.html',
  styleUrls: ['./color.component.scss']
})
export class ColorComponent extends WidgetComponent implements OnInit {

  constructor(public formService: FormService) {
    super(formService);
  }

  ngOnInit() {
  }

}
