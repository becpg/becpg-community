import { BeCPGHelper } from './../utils/becpg-helper.';
import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'nodeRefToId'
})
export class NodeRefToIdPipe implements PipeTransform {

  transform(value: any, args?: any): any {
    return value.replace(BeCPGHelper.NODE_REF_SPACE_PREFIX, "");
  }

}
