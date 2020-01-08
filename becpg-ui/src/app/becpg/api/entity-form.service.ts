import { Injectable } from '@angular/core';
import {
    EcmModelService, NodeService, WidgetVisibilityService,
    FormService, FormRenderingService, FormBaseComponent, FormOutcomeModel,
    FormEvent, FormErrorEvent, FormFieldModel,
    FormModel, FormOutcomeEvent, FormValues, ContentLinkModel
} from '@alfresco/adf-core';
import { Observable, of, Subject, from } from 'rxjs';
import { switchMap, takeUntil } from 'rxjs/operators';
import { AlfrescoApiService } from '@alfresco/adf-core';

@Injectable({
    providedIn: 'root'
})
export class EntityFormService {

    formDefinition: any;


    constructor(protected formService: FormService,
        protected visibilityService: WidgetVisibilityService,
        protected ecmModelService: EcmModelService,
        protected nodeService: NodeService,
        private apiService: AlfrescoApiService) {

        this.apiService.getInstance().webScript
            .executeWebScript('GET', 'becpg/designer/form/export?nodeRef=workspace://SpacesStore/6e6c3f7b-9c21-4927-8f30-531a6c5db37e', null, null, null, null).then(
                (ret) => {

                    this.formDefinition = ret;
                }, (error) => {
                    console.log('Error' + error);
                });
    }


    loadForm(formName: string, nodeType: string, isModel: boolean): Observable<FormModel> {


        if (this.formDefinition[nodeType] && this.formDefinition[nodeType][formName]) {
            let formDefinitionModel = new FormModel(this.formDefinition[nodeType][formName]);


            const fields: string[] = [];
            const forcedFields: string[] = [];

            formDefinitionModel.getFormFields().forEach(field => {
                fields.push(field.id);
                forcedFields.push(field.id);
            });

            return from(new Promise<FormModel>((success) => {
                this.apiService.getInstance().webScript.executeWebScript('POST', 'api/formdefinitions', null, null, null, {
                    itemKind: 'type',
                    itemId: nodeType,
                    fields: fields,
                    force: forcedFields
                }
                ).then(
                    (ret) => {

                        if (ret.data.definition.fields) {
                            for (const i in ret.data.definition.fields) {
                                if (Object.prototype.hasOwnProperty.call(ret.data.definition.fields, i)) {


                                    let formField = formDefinitionModel.getFieldById(ret.data.definition.fields[i].name);
                                    formField.name = ret.data.definition.fields[i].label;
                                    //    ret.data.definition.fields[i].dataType
                                    //    switch (ret.data.definition.fields[i].type) {
                                    //        case value:

                                    //            break;

                                    //        default:
                                    //            break;
                                    //    }


                                }

                            }
                        }
                        success(formDefinitionModel);

                    }
                );

            }));


        }


        return of(this.formService.parseForm(this.getDefinition()));


        // return from(new Promise<FormModel>((success) => {
        //     this.apiService.getInstance().webScript
        //     .executeWebScript('GET', 'becpg/designer/form/export?nodeRef=workspace://SpacesStore/6e6c3f7b-9c21-4927-8f30-531a6c5db37e', null, null, null, null).then(
        //         (ret) => {
        //             let formDefinitionModel = new FormModel;
        //             if (ret[nodeType] && ret[nodeType][formName]) {
        //                 console.log(ret[nodeType][formName])
        //                 formDefinitionModel = this.formService.parseForm(ret[nodeType][formName]);


        //                 // if (form && form.tabs && form.tabs.length > 0) {
        //                 //     form.tabs.map((tabModel) => this.refreshEntityVisibility(tabModel));
        //                 // }

        //                 // if (form) {
        //                 //     form.getFormFields().map((field) => this.refreshEntityVisibility(field));
        //                 // }

        //             }


        //             success(formDefinitionModel);

        //         }, (error) => {
        //             console.log('Error' + error);
        //         });
        // }));










        // this.ecmModelService.searchEcmType(formName, EcmModelService.MODEL_NAME).subscribe(
        //   //                     (customType) => {
        //   //                         const formDefinitionModel = new FormDefinitionModel(form.id, form.name, form.lastUpdatedByFullName, form.lastUpdated, customType.entry.properties);
        //   //                         from(
        //   //                             this.editorApi.saveForm(form.id, formDefinitionModel)
        //   //                         ).subscribe((formData) => {
        //   //                             observer.next(formData);
        //   //                             observer.complete();
        //   //                         }, (err) => this.handleError(err));
        //   //                     },

    }







    saveFormData(form: FormModel, data: FormValues) {


        // return   this.ecmModelService.createEcmTypeForActivitiForm(this.formName, this.form).subscribe((type) => {
        //     this.nodeService.createNodeMetadata(type.nodeType || type.entry.prefixedName, EcmModelService.MODEL_NAMESPACE, this.form.values, this.path, this.nameNode);
        // },
        //     (error) => {
        //         this.handleError(error);
        //     }
        // );

    }



    //       //beCPG
    //       this.form =;

    //           //this.loadFormForEcmNode(nodeId.currentValue);








    // /**
    //  * Creates a Form with a field for each metadata property.
    //  * @param formName Name of the new form
    //  * @returns The new form
    //  */
    // createFormFromANode(formName: string): Observable<any> {
    //     return new Observable((observer) => {
    //         this.createForm(formName).subscribe(
    //             (form) => {
    //                 this.ecmModelService.searchEcmType(formName, EcmModelService.MODEL_NAME).subscribe(
    //                     (customType) => {
    //                         const formDefinitionModel = new FormDefinitionModel(form.id, form.name, form.lastUpdatedByFullName, form.lastUpdated, customType.entry.properties);
    //                         from(
    //                             this.editorApi.saveForm(form.id, formDefinitionModel)
    //                         ).subscribe((formData) => {
    //                             observer.next(formData);
    //                             observer.complete();
    //                         }, (err) => this.handleError(err));
    //                     },
    //                     (err) => this.handleError(err));
    //             },
    //             (err) => this.handleError(err));
    //     });
    // }







    getDefinition(): any {
        return {
            "id": 3003,
            "name": "plunker-01",
            "taskId": "7501",
            "taskName": "Plunk 01",
            "tabs": [
                {
                    "id": "tab1",
                    "title": "Text",
                    "visibilityCondition": null
                },
                {
                    "id": "tab2",
                    "title": "Misc",
                    "visibilityCondition": null
                }
            ],
            "fields": [
                {
                    "fieldType": "ContainerRepresentation",
                    "id": "1488274019966",
                    "name": "Label",
                    "type": "container",
                    "value": null,
                    "required": false,
                    "readOnly": false,
                    "overrideId": false,
                    "colspan": 1,
                    "placeholder": null,
                    "minLength": 0,
                    "maxLength": 0,
                    "minValue": null,
                    "maxValue": null,
                    "regexPattern": null,
                    "optionType": null,
                    "hasEmptyValue": null,
                    "options": null,
                    "restUrl": null,
                    "restResponsePath": null,
                    "restIdProperty": null,
                    "restLabelProperty": null,
                    "tab": null,
                    "className": null,
                    "dateDisplayFormat": null,
                    "layout": null,
                    "sizeX": 2,
                    "sizeY": 1,
                    "row": -1,
                    "col": -1,
                    "visibilityCondition": null,
                    "numberOfColumns": 2,
                    "fields": {
                        "1": [],
                        "2": []
                    }
                },
                {
                    "fieldType": "ContainerRepresentation",
                    "id": "section4",
                    "name": "Section 4",
                    "type": "group",
                    "value": null,
                    "required": false,
                    "readOnly": false,
                    "overrideId": false,
                    "colspan": 1,
                    "placeholder": null,
                    "minLength": 0,
                    "maxLength": 0,
                    "minValue": null,
                    "maxValue": null,
                    "regexPattern": null,
                    "optionType": null,
                    "hasEmptyValue": null,
                    "options": null,
                    "restUrl": null,
                    "restResponsePath": null,
                    "restIdProperty": null,
                    "restLabelProperty": null,
                    "tab": "tab2",
                    "className": null,
                    "dateDisplayFormat": null,
                    "layout": {
                        "row": -1,
                        "column": -1,
                        "colspan": 2
                    },
                    "sizeX": 2,
                    "sizeY": 1,
                    "row": -1,
                    "col": -1,
                    "visibilityCondition": null,
                    "numberOfColumns": 2,
                    "fields": {
                        "1": [
                            {
                                "fieldType": "FormFieldRepresentation",
                                "id": "label8",
                                "name": "Label8",
                                "type": "people",
                                "value": null,
                                "required": false,
                                "readOnly": false,
                                "overrideId": false,
                                "colspan": 1,
                                "placeholder": null,
                                "minLength": 0,
                                "maxLength": 0,
                                "minValue": null,
                                "maxValue": null,
                                "regexPattern": null,
                                "optionType": null,
                                "hasEmptyValue": null,
                                "options": null,
                                "restUrl": null,
                                "restResponsePath": null,
                                "restIdProperty": null,
                                "restLabelProperty": null,
                                "tab": "tab2",
                                "className": null,
                                "params": {
                                    "existingColspan": 1,
                                    "maxColspan": 2
                                },
                                "dateDisplayFormat": null,
                                "layout": {
                                    "row": -1,
                                    "column": -1,
                                    "colspan": 1
                                },
                                "sizeX": 1,
                                "sizeY": 1,
                                "row": -1,
                                "col": -1,
                                "visibilityCondition": null
                            },
                            {
                                "fieldType": "FormFieldRepresentation",
                                "id": "label13",
                                "name": "Label13",
                                "type": "functional-group",
                                "value": null,
                                "required": false,
                                "readOnly": false,
                                "overrideId": false,
                                "colspan": 1,
                                "placeholder": null,
                                "minLength": 0,
                                "maxLength": 0,
                                "minValue": null,
                                "maxValue": null,
                                "regexPattern": null,
                                "optionType": null,
                                "hasEmptyValue": null,
                                "options": null,
                                "restUrl": null,
                                "restResponsePath": null,
                                "restIdProperty": null,
                                "restLabelProperty": null,
                                "tab": "tab2",
                                "className": null,
                                "params": {
                                    "existingColspan": 1,
                                    "maxColspan": 2
                                },
                                "dateDisplayFormat": null,
                                "layout": {
                                    "row": -1,
                                    "column": -1,
                                    "colspan": 1
                                },
                                "sizeX": 1,
                                "sizeY": 1,
                                "row": -1,
                                "col": -1,
                                "visibilityCondition": null
                            },
                            {
                                "fieldType": "FormFieldRepresentation",
                                "id": "label18",
                                "name": "Label18",
                                "type": "readonly",
                                "value": null,
                                "required": false,
                                "readOnly": false,
                                "overrideId": false,
                                "colspan": 1,
                                "placeholder": null,
                                "minLength": 0,
                                "maxLength": 0,
                                "minValue": null,
                                "maxValue": null,
                                "regexPattern": null,
                                "optionType": null,
                                "hasEmptyValue": null,
                                "options": null,
                                "restUrl": null,
                                "restResponsePath": null,
                                "restIdProperty": null,
                                "restLabelProperty": null,
                                "tab": "tab2",
                                "className": null,
                                "params": {
                                    "existingColspan": 1,
                                    "maxColspan": 2
                                },
                                "dateDisplayFormat": null,
                                "layout": {
                                    "row": -1,
                                    "column": -1,
                                    "colspan": 1
                                },
                                "sizeX": 1,
                                "sizeY": 1,
                                "row": -1,
                                "col": -1,
                                "visibilityCondition": null
                            },
                            {
                                "fieldType": "FormFieldRepresentation",
                                "id": "label19",
                                "name": "Label19",
                                "type": "readonly-text",
                                "value": "Display text as part of the form",
                                "required": false,
                                "readOnly": false,
                                "overrideId": false,
                                "colspan": 1,
                                "placeholder": null,
                                "minLength": 0,
                                "maxLength": 0,
                                "minValue": null,
                                "maxValue": null,
                                "regexPattern": null,
                                "optionType": null,
                                "hasEmptyValue": null,
                                "options": null,
                                "restUrl": null,
                                "restResponsePath": null,
                                "restIdProperty": null,
                                "restLabelProperty": null,
                                "tab": "tab2",
                                "className": null,
                                "params": {
                                    "existingColspan": 1,
                                    "maxColspan": 2
                                },
                                "dateDisplayFormat": null,
                                "layout": {
                                    "row": -1,
                                    "column": -1,
                                    "colspan": 1
                                },
                                "sizeX": 1,
                                "sizeY": 1,
                                "row": -1,
                                "col": -1,
                                "visibilityCondition": null
                            }
                        ],
                        "2": [
                            {
                                "fieldType": "HyperlinkRepresentation",
                                "id": "label15",
                                "name": "Label15",
                                "type": "hyperlink",
                                "value": null,
                                "required": false,
                                "readOnly": false,
                                "overrideId": false,
                                "colspan": 1,
                                "placeholder": null,
                                "minLength": 0,
                                "maxLength": 0,
                                "minValue": null,
                                "maxValue": null,
                                "regexPattern": null,
                                "optionType": null,
                                "hasEmptyValue": null,
                                "options": null,
                                "restUrl": null,
                                "restResponsePath": null,
                                "restIdProperty": null,
                                "restLabelProperty": null,
                                "tab": "tab2",
                                "className": null,
                                "params": {
                                    "existingColspan": 1,
                                    "maxColspan": 1
                                },
                                "dateDisplayFormat": null,
                                "layout": {
                                    "row": -1,
                                    "column": -1,
                                    "colspan": 1
                                },
                                "sizeX": 1,
                                "sizeY": 1,
                                "row": -1,
                                "col": -1,
                                "visibilityCondition": null,
                                "hyperlinkUrl": "www.google.com",
                                "displayText": null
                            }/*,
                            {
                                "fieldType": "AttachFileFieldRepresentation",
                                "id": "label16",
                                "name": "Label16",
                                "type": "upload",
                                "value": [],
                                "required": false,
                                "readOnly": false,
                                "overrideId": false,
                                "colspan": 1,
                                "placeholder": null,
                                "minLength": 0,
                                "maxLength": 0,
                                "minValue": null,
                                "maxValue": null,
                                "regexPattern": null,
                                "optionType": null,
                                "hasEmptyValue": null,
                                "options": null,
                                "restUrl": null,
                                "restResponsePath": null,
                                "restIdProperty": null,
                                "restLabelProperty": null,
                                "tab": "tab2",
                                "className": null,
                                "params": {
                                    "existingColspan": 1,
                                    "maxColspan": 1,
                                    "fileSource": {
                                        "serviceId": "all-file-sources",
                                        "name": "All file sources"
                                    }
                                },
                                "dateDisplayFormat": null,
                                "layout": {
                                    "row": -1,
                                    "column": -1,
                                    "colspan": 1
                                },
                                "sizeX": 1,
                                "sizeY": 1,
                                "row": -1,
                                "col": -1,
                                "visibilityCondition": null,
                                "metaDataColumnDefinitions": null
                            },
                            {
                                "fieldType": "FormFieldRepresentation",
                                "id": "label17",
                                "name": "Label17",
                                "type": "select-folder",
                                "value": null,
                                "required": false,
                                "readOnly": false,
                                "overrideId": false,
                                "colspan": 1,
                                "placeholder": null,
                                "minLength": 0,
                                "maxLength": 0,
                                "minValue": null,
                                "maxValue": null,
                                "regexPattern": null,
                                "optionType": null,
                                "hasEmptyValue": null,
                                "options": null,
                                "restUrl": null,
                                "restResponsePath": null,
                                "restIdProperty": null,
                                "restLabelProperty": null,
                                "tab": "tab2",
                                "className": null,
                                "params": {
                                    "existingColspan": 1,
                                    "maxColspan": 1,
                                    "folderSource": {
                                        "serviceId": "alfresco-1",
                                        "name": "Alfresco 5.2 Local",
                                        "metaDataAllowed": true
                                    }
                                },
                                "dateDisplayFormat": null,
                                "layout": {
                                    "row": -1,
                                    "column": -1,
                                    "colspan": 1
                                },
                                "sizeX": 1,
                                "sizeY": 1,
                                "row": -1,
                                "col": -1,
                                "visibilityCondition": null
                            }*/
                        ]
                    }
                },
                {
                    "fieldType": "DynamicTableRepresentation",
                    "id": "label14",
                    "name": "Label14",
                    "type": "dynamic-table",
                    "value": null,
                    "required": false,
                    "readOnly": false,
                    "overrideId": false,
                    "colspan": 1,
                    "placeholder": null,
                    "minLength": 0,
                    "maxLength": 0,
                    "minValue": null,
                    "maxValue": null,
                    "regexPattern": null,
                    "optionType": null,
                    "hasEmptyValue": null,
                    "options": null,
                    "restUrl": null,
                    "restResponsePath": null,
                    "restIdProperty": null,
                    "restLabelProperty": null,
                    "tab": "tab2",
                    "className": null,
                    "params": {
                        "existingColspan": 1,
                        "maxColspan": 1
                    },
                    "dateDisplayFormat": null,
                    "layout": {
                        "row": -1,
                        "column": -1,
                        "colspan": 2
                    },
                    "sizeX": 2,
                    "sizeY": 2,
                    "row": -1,
                    "col": -1,
                    "visibilityCondition": null,
                    "columnDefinitions": [
                        {
                            "id": "id",
                            "name": "id",
                            "type": "String",
                            "value": null,
                            "optionType": null,
                            "options": null,
                            "restResponsePath": null,
                            "restUrl": null,
                            "restIdProperty": null,
                            "restLabelProperty": null,
                            "amountCurrency": null,
                            "amountEnableFractions": false,
                            "required": true,
                            "editable": true,
                            "sortable": true,
                            "visible": true,
                            "endpoint": null,
                            "requestHeaders": null
                        },
                        {
                            "id": "name",
                            "name": "name",
                            "type": "String",
                            "value": null,
                            "optionType": null,
                            "options": null,
                            "restResponsePath": null,
                            "restUrl": null,
                            "restIdProperty": null,
                            "restLabelProperty": null,
                            "amountCurrency": null,
                            "amountEnableFractions": false,
                            "required": true,
                            "editable": true,
                            "sortable": true,
                            "visible": true,
                            "endpoint": null,
                            "requestHeaders": null
                        }
                    ]
                },
                {
                    "fieldType": "ContainerRepresentation",
                    "id": "section1",
                    "name": "Section 1",
                    "type": "group",
                    "value": null,
                    "required": false,
                    "readOnly": false,
                    "overrideId": false,
                    "colspan": 1,
                    "placeholder": null,
                    "minLength": 0,
                    "maxLength": 0,
                    "minValue": null,
                    "maxValue": null,
                    "regexPattern": null,
                    "optionType": null,
                    "hasEmptyValue": null,
                    "options": null,
                    "restUrl": null,
                    "restResponsePath": null,
                    "restIdProperty": null,
                    "restLabelProperty": null,
                    "tab": "tab1",
                    "className": null,
                    "dateDisplayFormat": null,
                    "layout": {
                        "row": -1,
                        "column": -1,
                        "colspan": 2
                    },
                    "sizeX": 2,
                    "sizeY": 1,
                    "row": -1,
                    "col": -1,
                    "visibilityCondition": null,
                    "numberOfColumns": 2,
                    "fields": {
                        "1": [
                            {
                                "fieldType": "FormFieldRepresentation",
                                "id": "label1",
                                "name": "Label1",
                                "type": "text",
                                "value": null,
                                "required": false,
                                "readOnly": false,
                                "overrideId": false,
                                "colspan": 1,
                                "placeholder": null,
                                "minLength": 0,
                                "maxLength": 0,
                                "minValue": null,
                                "maxValue": null,
                                "regexPattern": null,
                                "optionType": null,
                                "hasEmptyValue": null,
                                "options": null,
                                "restUrl": null,
                                "restResponsePath": null,
                                "restIdProperty": null,
                                "restLabelProperty": null,
                                "tab": "tab1",
                                "className": null,
                                "params": {
                                    "existingColspan": 1,
                                    "maxColspan": 2
                                },
                                "dateDisplayFormat": null,
                                "layout": {
                                    "row": -1,
                                    "column": -1,
                                    "colspan": 1
                                },
                                "sizeX": 1,
                                "sizeY": 1,
                                "row": -1,
                                "col": -1,
                                "visibilityCondition": null
                            },
                            {
                                "fieldType": "FormFieldRepresentation",
                                "id": "label3",
                                "name": "Label3",
                                "type": "text",
                                "value": null,
                                "required": false,
                                "readOnly": false,
                                "overrideId": false,
                                "colspan": 1,
                                "placeholder": null,
                                "minLength": 0,
                                "maxLength": 0,
                                "minValue": null,
                                "maxValue": null,
                                "regexPattern": null,
                                "optionType": null,
                                "hasEmptyValue": null,
                                "options": null,
                                "restUrl": null,
                                "restResponsePath": null,
                                "restIdProperty": null,
                                "restLabelProperty": null,
                                "tab": "tab1",
                                "className": null,
                                "params": {
                                    "existingColspan": 1,
                                    "maxColspan": 2
                                },
                                "dateDisplayFormat": null,
                                "layout": {
                                    "row": -1,
                                    "column": -1,
                                    "colspan": 1
                                },
                                "sizeX": 1,
                                "sizeY": 1,
                                "row": -1,
                                "col": -1,
                                "visibilityCondition": null
                            }
                        ],
                        "2": [
                            {
                                "fieldType": "FormFieldRepresentation",
                                "id": "label2",
                                "name": "Label2",
                                "type": "multi-line-text",
                                "value": null,
                                "required": false,
                                "readOnly": false,
                                "overrideId": false,
                                "colspan": 1,
                                "placeholder": null,
                                "minLength": 0,
                                "maxLength": 0,
                                "minValue": null,
                                "maxValue": null,
                                "regexPattern": null,
                                "optionType": null,
                                "hasEmptyValue": null,
                                "options": null,
                                "restUrl": null,
                                "restResponsePath": null,
                                "restIdProperty": null,
                                "restLabelProperty": null,
                                "tab": "tab1",
                                "className": null,
                                "params": {
                                    "existingColspan": 1,
                                    "maxColspan": 1
                                },
                                "dateDisplayFormat": null,
                                "layout": {
                                    "row": -1,
                                    "column": -1,
                                    "colspan": 1
                                },
                                "sizeX": 1,
                                "sizeY": 2,
                                "row": -1,
                                "col": -1,
                                "visibilityCondition": null
                            }
                        ]
                    }
                },
                {
                    "fieldType": "ContainerRepresentation",
                    "id": "section2",
                    "name": "Section 2",
                    "type": "group",
                    "value": null,
                    "required": false,
                    "readOnly": false,
                    "overrideId": false,
                    "colspan": 1,
                    "placeholder": null,
                    "minLength": 0,
                    "maxLength": 0,
                    "minValue": null,
                    "maxValue": null,
                    "regexPattern": null,
                    "optionType": null,
                    "hasEmptyValue": null,
                    "options": null,
                    "restUrl": null,
                    "restResponsePath": null,
                    "restIdProperty": null,
                    "restLabelProperty": null,
                    "tab": "tab1",
                    "className": null,
                    "dateDisplayFormat": null,
                    "layout": {
                        "row": -1,
                        "column": -1,
                        "colspan": 2
                    },
                    "sizeX": 2,
                    "sizeY": 1,
                    "row": -1,
                    "col": -1,
                    "visibilityCondition": null,
                    "numberOfColumns": 2,
                    "fields": {
                        "1": [
                            {
                                "fieldType": "FormFieldRepresentation",
                                "id": "label4",
                                "name": "Label4",
                                "type": "integer",
                                "value": null,
                                "required": false,
                                "readOnly": false,
                                "overrideId": false,
                                "colspan": 1,
                                "placeholder": null,
                                "minLength": 0,
                                "maxLength": 0,
                                "minValue": null,
                                "maxValue": null,
                                "regexPattern": null,
                                "optionType": null,
                                "hasEmptyValue": null,
                                "options": null,
                                "restUrl": null,
                                "restResponsePath": null,
                                "restIdProperty": null,
                                "restLabelProperty": null,
                                "tab": "tab1",
                                "className": null,
                                "params": {
                                    "existingColspan": 1,
                                    "maxColspan": 2
                                },
                                "dateDisplayFormat": null,
                                "layout": {
                                    "row": -1,
                                    "column": -1,
                                    "colspan": 1
                                },
                                "sizeX": 1,
                                "sizeY": 1,
                                "row": -1,
                                "col": -1,
                                "visibilityCondition": null
                            },
                            {
                                "fieldType": "FormFieldRepresentation",
                                "id": "label7",
                                "name": "Label7",
                                "type": "date",
                                "value": null,
                                "required": false,
                                "readOnly": false,
                                "overrideId": false,
                                "colspan": 1,
                                "placeholder": null,
                                "minLength": 0,
                                "maxLength": 0,
                                "minValue": null,
                                "maxValue": null,
                                "regexPattern": null,
                                "optionType": null,
                                "hasEmptyValue": null,
                                "options": null,
                                "restUrl": null,
                                "restResponsePath": null,
                                "restIdProperty": null,
                                "restLabelProperty": null,
                                "tab": "tab1",
                                "className": null,
                                "params": {
                                    "existingColspan": 1,
                                    "maxColspan": 2
                                },
                                "dateDisplayFormat": null,
                                "layout": {
                                    "row": -1,
                                    "column": -1,
                                    "colspan": 1
                                },
                                "sizeX": 1,
                                "sizeY": 1,
                                "row": -1,
                                "col": -1,
                                "visibilityCondition": null
                            }
                        ],
                        "2": [
                            {
                                "fieldType": "FormFieldRepresentation",
                                "id": "label5",
                                "name": "Label5",
                                "type": "boolean",
                                "value": null,
                                "required": false,
                                "readOnly": false,
                                "overrideId": false,
                                "colspan": 1,
                                "placeholder": null,
                                "minLength": 0,
                                "maxLength": 0,
                                "minValue": null,
                                "maxValue": null,
                                "regexPattern": null,
                                "optionType": null,
                                "hasEmptyValue": null,
                                "options": null,
                                "restUrl": null,
                                "restResponsePath": null,
                                "restIdProperty": null,
                                "restLabelProperty": null,
                                "tab": "tab1",
                                "className": null,
                                "params": {
                                    "existingColspan": 1,
                                    "maxColspan": 1
                                },
                                "dateDisplayFormat": null,
                                "layout": {
                                    "row": -1,
                                    "column": -1,
                                    "colspan": 1
                                },
                                "sizeX": 1,
                                "sizeY": 1,
                                "row": -1,
                                "col": -1,
                                "visibilityCondition": null
                            },
                            {
                                "fieldType": "FormFieldRepresentation",
                                "id": "label6",
                                "name": "Label6",
                                "type": "boolean",
                                "value": null,
                                "required": false,
                                "readOnly": false,
                                "overrideId": false,
                                "colspan": 1,
                                "placeholder": null,
                                "minLength": 0,
                                "maxLength": 0,
                                "minValue": null,
                                "maxValue": null,
                                "regexPattern": null,
                                "optionType": null,
                                "hasEmptyValue": null,
                                "options": null,
                                "restUrl": null,
                                "restResponsePath": null,
                                "restIdProperty": null,
                                "restLabelProperty": null,
                                "tab": "tab1",
                                "className": null,
                                "params": {
                                    "existingColspan": 1,
                                    "maxColspan": 1
                                },
                                "dateDisplayFormat": null,
                                "layout": {
                                    "row": -1,
                                    "column": -1,
                                    "colspan": 1
                                },
                                "sizeX": 1,
                                "sizeY": 1,
                                "row": -1,
                                "col": -1,
                                "visibilityCondition": null
                            },
                            {
                                "fieldType": "AmountFieldRepresentation",
                                "id": "label11",
                                "name": "Label11",
                                "type": "amount",
                                "value": null,
                                "required": false,
                                "readOnly": false,
                                "overrideId": false,
                                "colspan": 1,
                                "placeholder": null,
                                "minLength": 0,
                                "maxLength": 0,
                                "minValue": null,
                                "maxValue": null,
                                "regexPattern": null,
                                "optionType": null,
                                "hasEmptyValue": null,
                                "options": null,
                                "restUrl": null,
                                "restResponsePath": null,
                                "restIdProperty": null,
                                "restLabelProperty": null,
                                "tab": "tab1",
                                "className": null,
                                "params": {
                                    "existingColspan": 1,
                                    "maxColspan": 1
                                },
                                "dateDisplayFormat": null,
                                "layout": {
                                    "row": -1,
                                    "column": -1,
                                    "colspan": 1
                                },
                                "sizeX": 1,
                                "sizeY": 1,
                                "row": -1,
                                "col": -1,
                                "visibilityCondition": null,
                                "enableFractions": false,
                                "currency": null
                            }
                        ]
                    }
                },
                {
                    "fieldType": "ContainerRepresentation",
                    "id": "section3",
                    "name": "Section 3",
                    "type": "group",
                    "value": null,
                    "required": false,
                    "readOnly": false,
                    "overrideId": false,
                    "colspan": 1,
                    "placeholder": null,
                    "minLength": 0,
                    "maxLength": 0,
                    "minValue": null,
                    "maxValue": null,
                    "regexPattern": null,
                    "optionType": null,
                    "hasEmptyValue": null,
                    "options": null,
                    "restUrl": null,
                    "restResponsePath": null,
                    "restIdProperty": null,
                    "restLabelProperty": null,
                    "tab": "tab1",
                    "className": null,
                    "dateDisplayFormat": null,
                    "layout": {
                        "row": -1,
                        "column": -1,
                        "colspan": 2
                    },
                    "sizeX": 2,
                    "sizeY": 1,
                    "row": -1,
                    "col": -1,
                    "visibilityCondition": null,
                    "numberOfColumns": 2,
                    "fields": {
                        "1": [
                            {
                                "fieldType": "RestFieldRepresentation",
                                "id": "label9",
                                "name": "Label9",
                                "type": "dropdown",
                                "value": "Choose one...",
                                "required": false,
                                "readOnly": false,
                                "overrideId": false,
                                "colspan": 1,
                                "placeholder": null,
                                "minLength": 0,
                                "maxLength": 0,
                                "minValue": null,
                                "maxValue": null,
                                "regexPattern": null,
                                "optionType": null,
                                "hasEmptyValue": true,
                                "options": [
                                    {
                                        "id": "empty",
                                        "name": "Choose one..."
                                    }
                                ],
                                "restUrl": null,
                                "restResponsePath": null,
                                "restIdProperty": null,
                                "restLabelProperty": null,
                                "tab": "tab1",
                                "className": null,
                                "params": {
                                    "existingColspan": 1,
                                    "maxColspan": 2
                                },
                                "dateDisplayFormat": null,
                                "layout": {
                                    "row": -1,
                                    "column": -1,
                                    "colspan": 1
                                },
                                "sizeX": 1,
                                "sizeY": 1,
                                "row": -1,
                                "col": -1,
                                "visibilityCondition": null,
                                "endpoint": null,
                                "requestHeaders": null
                            },
                            {
                                "fieldType": "RestFieldRepresentation",
                                "id": "label12",
                                "name": "Label12",
                                "type": "radio-buttons",
                                "value": null,
                                "required": false,
                                "readOnly": false,
                                "overrideId": false,
                                "colspan": 1,
                                "placeholder": null,
                                "minLength": 0,
                                "maxLength": 0,
                                "minValue": null,
                                "maxValue": null,
                                "regexPattern": null,
                                "optionType": null,
                                "hasEmptyValue": null,
                                "options": [
                                    {
                                        "id": "option_1",
                                        "name": "Option 1"
                                    },
                                    {
                                        "id": "option_2",
                                        "name": "Option 2"
                                    }
                                ],
                                "restUrl": null,
                                "restResponsePath": null,
                                "restIdProperty": null,
                                "restLabelProperty": null,
                                "tab": "tab1",
                                "className": null,
                                "params": {
                                    "existingColspan": 1,
                                    "maxColspan": 2
                                },
                                "dateDisplayFormat": null,
                                "layout": {
                                    "row": -1,
                                    "column": -1,
                                    "colspan": 1
                                },
                                "sizeX": 1,
                                "sizeY": 1,
                                "row": -1,
                                "col": -1,
                                "visibilityCondition": null,
                                "endpoint": null,
                                "requestHeaders": null
                            }
                        ],
                        "2": [
                            {
                                "fieldType": "RestFieldRepresentation",
                                "id": "label10",
                                "name": "Label10",
                                "type": "typeahead",
                                "value": null,
                                "required": false,
                                "readOnly": false,
                                "overrideId": false,
                                "colspan": 1,
                                "placeholder": null,
                                "minLength": 0,
                                "maxLength": 0,
                                "minValue": null,
                                "maxValue": null,
                                "regexPattern": null,
                                "optionType": null,
                                "hasEmptyValue": null,
                                "options": null,
                                "restUrl": null,
                                "restResponsePath": null,
                                "restIdProperty": null,
                                "restLabelProperty": null,
                                "tab": "tab1",
                                "className": null,
                                "params": {
                                    "existingColspan": 1,
                                    "maxColspan": 1
                                },
                                "dateDisplayFormat": null,
                                "layout": {
                                    "row": -1,
                                    "column": -1,
                                    "colspan": 1
                                },
                                "sizeX": 1,
                                "sizeY": 1,
                                "row": -1,
                                "col": -1,
                                "visibilityCondition": null,
                                "endpoint": null,
                                "requestHeaders": null
                            }
                        ]
                    }
                }
            ],
            "outcomes": [],
            "javascriptEvents": [],
            "className": "",
            "style": "",
            "customFieldTemplates": {},
            "metadata": {},
            "variables": [],
            "gridsterForm": false,
            "globalDateFormat": "D-M-YYYY"
        }
    }


}
