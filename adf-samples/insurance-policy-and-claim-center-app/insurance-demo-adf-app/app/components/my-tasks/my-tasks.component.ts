

import { AfterViewInit, Component, ElementRef, Input, ViewChild, AfterContentChecked } from '@angular/core';
import {
  ActivitiApps,
  ActivitiFilters,
  ActivitiTaskDetails,
  ActivitiTaskList,
  FilterRepresentationModel
} from 'ng2-activiti-tasklist';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs/Rx';
import {
  ObjectDataTableAdapter,
  DataSorting
} from 'ng2-alfresco-datatable';
import { AlfrescoApiService } from 'ng2-alfresco-core';
import { FormRenderingService } from 'ng2-activiti-form';

declare var componentHandler;

@Component({
  selector: 'app-my-tasks',
  templateUrl: './my-tasks.component.html',
  styleUrls: ['./my-tasks.component.css']
})
export class MyTasksComponent implements AfterViewInit, AfterContentChecked {

  @ViewChild(ActivitiApps)
  activitiapps: ActivitiApps;

  @ViewChild(ActivitiFilters)
  activitifilter: ActivitiFilters;

  @ViewChild(ActivitiTaskList)
  activititasklist: ActivitiTaskList;

  @ViewChild(ActivitiTaskDetails)
  activitidetails: ActivitiTaskDetails;

  @Input()
  appId: number;

  layoutType: string;
  currentTaskId: string;

  taskSchemaColumns: any[] = [];
  processSchemaColumns: any[] = [];

  taskFilter: FilterRepresentationModel;

  myTaskList: boolean = true;

  taskState: string = 'open';
  isOpen: boolean = false;
  taskStateDisplay: string = 'Open';

  el: ElementRef;

  sub: Subscription;
  myTaskListDataTable: ObjectDataTableAdapter;
  dataTasks: ObjectDataTableAdapter;
  dataProcesses: ObjectDataTableAdapter;

  constructor(private elementRef: ElementRef,
    private route: ActivatedRoute,
    private apiService: AlfrescoApiService,
    private formRenderingService: FormRenderingService,
    el: ElementRef
  ) {
    this.el = el;
    this.myTaskListDataTable = new ObjectDataTableAdapter(
      [],
      [
        { type: 'text', key: 'name', title: 'Name', cssClass: 'full-width name-column', sortable: true },
        { type: 'date', key: 'created', title: 'Created', cssClass: 'full-width name-column', sortable: true }
      ]
    );
    this.myTaskListDataTable.setSorting(new DataSorting('created', 'desc'));

    this.dataTasks = new ObjectDataTableAdapter(
      [],
      [
        { type: 'text', key: 'name', title: 'Name', cssClass: 'full-width name-column', sortable: true }
      ]
    );
    this.dataTasks.setSorting(new DataSorting('created', 'desc'));

    this.dataProcesses = new ObjectDataTableAdapter(
      [],
      [
        { type: 'text', key: 'name', title: 'Name', cssClass: 'full-width name-column', sortable: true },
        { type: 'text', key: 'started', title: 'Started', cssClass: 'hidden', sortable: true }
      ]
    );
  }

  ngOnInit() {

    this.sub = this.route.params.subscribe(params => {
      let applicationId = params['appId'];
      if (applicationId && applicationId !== '0') {
        this.appId = params['appId'];
      }

      this.taskFilter = null;
      this.currentTaskId = null;
    });
    this.layoutType = ActivitiApps.LAYOUT_GRID;
  }

  ngAfterContentChecked() {
    // A workaround to hide a few things!
    if (!this.myTaskList) {
      
      if (this.el.nativeElement.querySelector('.activiti-form-debug-container')) {
        this.el.nativeElement.querySelector('.activiti-form-debug-container').style.display = 'none';
      }
    }
  }

  ngOnDestroy() {
    this.sub.unsubscribe();
  }

  onChange(flag: boolean) {
    if (flag === false) {
      this.taskState = 'open';
      this.taskStateDisplay = 'Open';
    } else {
      this.taskState = 'completed';
      this.taskStateDisplay = 'Completed';
    }
  }
  onTaskFilterClick(event: FilterRepresentationModel) {
    this.taskFilter = event;
  }

  onSuccessTaskFilterList(event: any) {
    this.taskFilter = this.activitifilter.getCurrentFilter();
  }

  onStartTaskSuccess(event: any) {
    this.activitifilter.selectFirstFilter();
    this.taskFilter = this.activitifilter.getCurrentFilter();
    this.activititasklist.reload();
  }

  onSuccessTaskList(event: FilterRepresentationModel) {
    this.currentTaskId = this.activititasklist.getCurrentId();
  }

  onTaskRowClick(taskId) {
    this.currentTaskId = taskId;
    this.myTaskList = false;
  }

  taskStateFilterChanged() {
    this.myTaskList = true;
  }

  onFormCompletedInTaskList(form) {
    console.log(form);
    this.currentTaskId = null;
    this.myTaskList = true;
  }

  ngAfterViewInit() {
    // workaround for MDL issues with dynamic components
    if (componentHandler) {
      componentHandler.upgradeAllRegistered();
    }

    this.loadStencilScriptsInPageFromActiviti();
  }

  loadStencilScriptsInPageFromActiviti() {
    this.apiService.getInstance().activiti.scriptFileApi.getControllers().then(response => {
      if (response) {
        let s = document.createElement('script');
        s.type = 'text/javascript';
        s.text = response;
        this.elementRef.nativeElement.appendChild(s);
      }
    });
  }

}
