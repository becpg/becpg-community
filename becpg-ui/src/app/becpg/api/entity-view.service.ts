import { Injectable } from '@angular/core';
import { Entity } from '../model/Entity';
import { EntityList } from '../model/EntityList';
import { EntityView } from '../model/EntityView';
import entityViewsModel from '../../../assets/becpg/entity-views.json';


@Injectable({
  providedIn: 'root'
})
export class EntityViewService {

  constructor() { }

  hiddenViews: string[] = ['activityList'];



  getView(entity: Entity, viewId: string): EntityView {
    let view: EntityView;

    entity.datalists.forEach(datalist => {
      if (datalist.name === viewId) {
        view = this.createView(datalist);
      }

    });

    return view;
  }


  getViews(entity: Entity): EntityView[] {
    const views: EntityView[] = new Array();
    if(entity.datalists){
      entity.datalists.forEach(datalist => {
        if (!entityViewsModel.views[datalist.name] || !entityViewsModel.views[datalist.name].hidden) {
          views.push(this.createView(datalist));
        }

      });
    }
    return views.sort( (a, b) => a.sort - b.sort);
  }


  createView(list: EntityList): EntityView {
    const view = new EntityView(list);
    if (entityViewsModel.views[view.id]) {
      view.sort = entityViewsModel.views[view.id].sort;
      if (entityViewsModel.views[view.id].cards) {
        view.cards = entityViewsModel.views[view.id].cards;
      } else {
        view.cards = entityViewsModel.views['default'].cards;
      }
    } else {
      view.sort = entityViewsModel.views['default'].sort;
      view.cards = entityViewsModel.views['default'].cards;
    }


    return view;
  }


  // this.breakpointObserver.observe(Breakpoints.Handset).pipe(
  //   map(({ matches }) => {
  //     if (matches) {
  //       return [
  //         { title: 'Card 1', cols: 1, rows: 1 },
  //         { title: 'Card 2', cols: 1, rows: 1 },
  //         { title: 'Card 3', cols: 1, rows: 1 },
  //         { title: 'Card 4', cols: 1, rows: 1 }
  //       ];
  //     }

  //     return [
  //       { title: 'Card 1', cols: 2, rows: 1, type: 'entity' },
  //       { title: 'Card 2', cols: 1, rows: 1, type: 'list' },
  //       { title: 'Card 3', cols: 1, rows: 2, type: 'document' },
  //       { title: 'Card 4', cols: 1, rows: 1, type: 'properties' }
  //     ];
  //   })
  // );


}
