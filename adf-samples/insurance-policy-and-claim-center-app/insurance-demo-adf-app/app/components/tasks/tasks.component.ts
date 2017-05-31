

import { AfterViewInit, Component, ElementRef, Input, ViewChild } from '@angular/core';
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
import { FormRenderingService, FormService } from 'ng2-activiti-form';
import { BpmUserService } from 'ng2-alfresco-userinfo';
import { BpmUserModel } from 'ng2-alfresco-userinfo/src/models/bpm-user.model';

declare var componentHandler;

@Component({
  selector: 'app-tasks',
  templateUrl: './tasks.component.html',
  styleUrls: ['./tasks.component.css']
})
export class TasksComponent implements AfterViewInit {

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

  processTabActivie: boolean = false;

  reportsTabActivie: boolean = false;

  taskFilter: FilterRepresentationModel;

  // @Input()
  bpmUser: BpmUserModel;

  showTaskDashboard: boolean = false;

  myTaskList: boolean = true;
  taskState: string = 'open';
  openTaskListDisplayName: string = 'My Open Task List';
  completedTaskListDisplayName: string = 'My Completed Task List';
  taskListDisplayName: string = this.openTaskListDisplayName;
  taskFilterTabActive: boolean = false;

  sub: Subscription;
  myTaskListDataTable: ObjectDataTableAdapter;
  dataTasks: ObjectDataTableAdapter;
  dataProcesses: ObjectDataTableAdapter;

  constructor(private elementRef: ElementRef,
    private route: ActivatedRoute,
    private apiService: AlfrescoApiService,
    private formService: FormService,
    private formRenderingService: FormRenderingService,
    private bpmUserService: BpmUserService) {
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
    this.bpmUserService.getCurrentUserInfo()
      .subscribe((res) => {
        this.bpmUser = <BpmUserModel>res;
        if ((this.bpmUser.groups.filter(group => group.name === 'group1' && group.type === 1 && group.status === 'active').length) === 1) {
          this.showTaskDashboard = true;
        }
      });

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

  ngOnDestroy() {
    this.sub.unsubscribe();
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

  myOpenTasksClicked() {
    this.myTaskList = true;
    this.taskState = 'open';
    this.taskListDisplayName = this.openTaskListDisplayName;
    this.taskFilterTabActive = false;
  }

  myCompletedTasksClicked() {
    this.myTaskList = true;
    this.taskState = 'completed';
    this.taskListDisplayName = this.completedTaskListDisplayName;
    this.taskFilterTabActive = false;
  }

  advancedTaskFilterClicked() {
    this.taskFilterTabActive = true;
  }

  onFormCompleted(form) {
    this.activititasklist.reload();
    this.currentTaskId = null;
  }

  onFormCompletedInTaskList(form) {
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
