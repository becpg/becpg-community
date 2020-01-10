
import { Component, EventEmitter, Input, Output, ViewEncapsulation, SimpleChanges, OnInit, OnDestroy, OnChanges } from '@angular/core';
import {
    EcmModelService, NodeService, WidgetVisibilityService,
    FormService, FormRenderingService, FormBaseComponent, FormOutcomeModel,
    FormEvent, FormErrorEvent, FormFieldModel,
    FormModel, FormOutcomeEvent, FormValues, ContentLinkModel
} from '@alfresco/adf-core';
import { Observable, of, Subject } from 'rxjs';
import { switchMap, takeUntil } from 'rxjs/operators';
import { EntityFormService } from '../../api/entity-form.service';
import { Node } from '@alfresco/js-api';

@Component({
    selector: 'app-entity-form',
    templateUrl: './entity-form.component.html',
    styleUrls: ['./entity-form.component.scss'],
    encapsulation: ViewEncapsulation.None
})
export class EntityFormComponent  implements OnInit, OnDestroy, OnChanges {

    form: FormModel;

    /** Content Services node ID for the form metadata. */
    @Input()
    node: Node;

    /** Content Services node ID for the form metadata. */
    @Input()
    nodeType: string;

    /** Name of the form definition to load and display with custom values. */
    @Input()
    formId: string;

    /** Custom form values map to be used with the rendered form. */
    @Input()
    data: FormValues;

    /** Emitted when the form is submitted with the `Save` or custom outcomes. */
    @Output()
    formSaved: EventEmitter<FormModel> = new EventEmitter<FormModel>();

    /** Emitted when the form is submitted with the `Complete` outcome. */
    @Output()
    formCompleted: EventEmitter<FormModel> = new EventEmitter<FormModel>();

    /** Emitted when form content is clicked. */
    @Output()
    formContentClicked: EventEmitter<ContentLinkModel> = new EventEmitter<ContentLinkModel>();

    /** Emitted when the form is loaded or reloaded. */
    @Output()
    formLoaded: EventEmitter<FormModel> = new EventEmitter<FormModel>();

    /** Emitted when form values are refreshed due to a data property change. */
    @Output()
    formDataRefreshed: EventEmitter<FormModel> = new EventEmitter<FormModel>();

    /** Emitted when the supplied form values have a validation error. */
    @Output()
    formError: EventEmitter<FormFieldModel[]> = new EventEmitter<FormFieldModel[]>();

    @Output()
    error: EventEmitter<any> = new EventEmitter<any>();


    /** Emitted when any outcome is executed. Default behaviour can be prevented
     * via `event.preventDefault()`.
     */
    @Output()
    executeOutcome: EventEmitter<FormOutcomeEvent> = new EventEmitter<FormOutcomeEvent>();

    /** Toggle rendering of the `Refresh` button. */
    @Input()
    showRefreshButton: boolean = true;

    /** Toggle rendering of the validation icon next to the form title. */
    @Input()
    showValidationIcon: boolean = true;


    /** Toggle readonly state of the form. Forces all form widgets to render as readonly if enabled. */
    @Input()
    readOnly: boolean = false;


    debugMode: boolean = true;

    protected onDestroy$ = new Subject<boolean>();


    hasForm(): boolean {
        return this.form ? true : false;
    }

    constructor(protected formService: FormService,
        protected visibilityService: WidgetVisibilityService,
        protected ecmModelService: EcmModelService,
        protected nodeService: NodeService,
        protected formRenderingService: FormRenderingService,
        protected entityFormService: EntityFormService) {
        // this.formRenderingService.setComponentTypeResolver('upload', () => AttachFileWidgetComponent, true);
        // this.formRenderingService.setComponentTypeResolver('select-folder', () => AttachFolderWidgetComponent, true);
    }




    ngOnInit() {
        this.formService.formContentClicked
            .pipe(takeUntil(this.onDestroy$))
            .subscribe(content => this.formContentClicked.emit(content));

        this.formService.validateForm
            .pipe(takeUntil(this.onDestroy$))
            .subscribe(validateFormEvent => {
                if (validateFormEvent.errorsField.length > 0) {
                    this.formError.next(validateFormEvent.errorsField);
                }
            });

    }


    ngOnDestroy() {
        this.onDestroy$.next(true);
        this.onDestroy$.complete();
    }

    ngOnChanges(changes: SimpleChanges) {

        const data = changes['data'];
        if (data && data.currentValue) {
            this.refreshFormData();
            return;
        } else {
            this.loadForm();
        }

    }


    /**
       * Invoked when user clicks form refresh button.
       */
    onRefreshClicked() {
        this.loadForm();
    }

    loadForm() {

        if (this.node) {
            this.loadbeCPGForm(this.node.id, false);
        } else if (this.nodeType) {
            this.loadbeCPGForm(this.nodeType, true);
        }

    }
    loadbeCPGForm(itemId: string, isModel: boolean) {

        this.entityFormService.loadForm(this.formId, itemId, isModel).subscribe((form) => {
            this.form = form; //this.parseForm(form);
            this.visibilityService.refreshVisibility(this.form);
            this.form.validateForm();
            this.onFormLoaded(this.form);
        },
            (error) => {
                this.handleError(error);
            }
        );
    }


    handleError(err: any): any {
        this.error.emit(err);
    }

    // parseForm(formRepresentationJSON: any): FormModel {
    //     if (formRepresentationJSON) {
    //         const form = new FormModel(formRepresentationJSON, this.data, this.readOnly, this.formService);
    //         if (!formRepresentationJSON.fields) {
    //             form.outcomes = this.getFormDefinitionOutcomes(form);
    //         }
    //          if (this.fieldValidators && this.fieldValidators.length > 0) {
    //              form.fieldValidators = this.fieldValidators;
    //          }
    //         return form;
    //     }
    //     return null;
    // }

    /**
     * Get custom set of outcomes for a Form Definition.
     * @param form Form definition model.
     */
    getFormDefinitionOutcomes(form: FormModel): FormOutcomeModel[] {
        return [
            new FormOutcomeModel(form, { id: '$save', name: FormOutcomeModel.SAVE_ACTION, isSystem: true })
        ];
    }

    checkVisibility(field: FormFieldModel) {
        if (field && field.form) {
            this.visibilityService.refreshVisibility(field.form);
        }
    }

    private refreshFormData() {
        //this.form = this.parseForm(this.form.json);
        this.onFormLoaded(this.form);
        this.onFormDataRefreshed(this.form);
    }


    protected storeFormAsMetadata() {
        this.entityFormService.saveFormData(this.form, this.data);
    }

    protected onFormLoaded(form: FormModel) {
        this.formLoaded.emit(form);
        this.formService.formLoaded.next(new FormEvent(form));
    }

    protected onFormDataRefreshed(form: FormModel) {
        this.formDataRefreshed.emit(form);
        this.formService.formDataRefreshed.next(new FormEvent(form));
    }

    protected onExecuteOutcome(outcome: FormOutcomeModel): boolean {
        const args = new FormOutcomeEvent(outcome);

        this.formService.executeOutcome.next(args);
        if (args.defaultPrevented) {
            return false;
        }

        this.executeOutcome.emit(args);
        if (args.defaultPrevented) {
            return false;
        }

        return true;
    }

    completeTaskForm(outcome?: string) {
        if (this.form && this.form.taskId) {
            this.formService
                .completeTaskForm(this.form.taskId, this.form.values, outcome)
                .subscribe(
                    () => {
                        this.onTaskCompleted(this.form);
                        this.storeFormAsMetadata();
                    },
                    (error) => this.onTaskCompletedError(this.form, error)
                );
        }
    }


    protected onTaskSaved(form: FormModel) {
        this.formSaved.emit(form);
        this.formService.taskSaved.next(new FormEvent(form));
    }

    protected onTaskSavedError(form: FormModel, error: any) {
        this.handleError(error);
        this.formService.taskSavedError.next(new FormErrorEvent(form, error));
    }

    protected onTaskCompleted(form: FormModel) {
        this.formCompleted.emit(form);
        this.formService.taskCompleted.next(new FormEvent(form));
    }

    protected onTaskCompletedError(form: FormModel, error: any) {
        this.handleError(error);
        this.formService.taskCompletedError.next(new FormErrorEvent(form, error));
    }


    getColorForOutcome(outcomeName: string): string {
        return outcomeName === FormBaseComponent.COMPLETE_OUTCOME_NAME ? FormBaseComponent.COMPLETE_BUTTON_COLOR : '';
    }

    isOutcomeButtonEnabled(outcome: FormOutcomeModel): boolean {
        if (this.form.readOnly) {
            return false;
        }

        if (outcome) {
            // if (outcome.name === FormOutcomeModel.SAVE_ACTION) {
            //     return this.disableSaveButton ? false : this.form.isValid;
            // }
            // if (outcome.name === FormOutcomeModel.COMPLETE_ACTION) {
            //     return this.disableCompleteButton ? false : this.form.isValid;
            // }
            // if (outcome.name === FormOutcomeModel.START_PROCESS_ACTION) {
            //     return this.disableStartProcessButton ? false : this.form.isValid;
            // }
            return this.form.isValid;
        }
        return false;
    }

    isOutcomeButtonVisible(outcome: FormOutcomeModel, isFormReadOnly: boolean): boolean {
        if (outcome && outcome.name) {
            // if (outcome.name === FormOutcomeModel.COMPLETE_ACTION) {
            //     return this.showCompleteButton;
            // }
            // if (isFormReadOnly) {
            //     return outcome.isSelected;
            // }
            // if (outcome.name === FormOutcomeModel.SAVE_ACTION) {
            //     return this.showSaveButton;
            // }
            // if (outcome.name === FormOutcomeModel.START_PROCESS_ACTION) {
            //     return false;
            // }
            return true;
        }
        return false;
    }

    /**
     * Invoked when user clicks outcome button.
     * @param outcome Form outcome model
     */
    onOutcomeClicked(outcome: FormOutcomeModel): boolean {
        if (!this.readOnly && outcome && this.form) {

            if (!this.onExecuteOutcome(outcome)) {
                return false;
            }

            if (outcome.isSystem) {
                if (outcome.id === FormBaseComponent.SAVE_OUTCOME_ID) {
                //   this.saveTaskForm();
                    return true;
                }

                if (outcome.id === FormBaseComponent.COMPLETE_OUTCOME_ID) {
                    this.completeTaskForm();
                    return true;
                }

                if (outcome.id === FormBaseComponent.START_PROCESS_OUTCOME_ID) {
                    this.completeTaskForm();
                    return true;
                }

                if (outcome.id === FormBaseComponent.CUSTOM_OUTCOME_ID) {
                    this.onTaskSaved(this.form);
                    this.storeFormAsMetadata();
                    return true;
                }
            } else {
                // Note: Activiti is using NAME field rather than ID for outcomes
                if (outcome.name) {
                    this.onTaskSaved(this.form);
                    this.completeTaskForm(outcome.name);
                    return true;
                }
            }
        }

        return false;
    }

}
