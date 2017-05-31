
import { ModuleWithProviders }  from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { AuthGuard, AuthGuardEcm, AuthGuardBpm } from 'ng2-alfresco-core';

import {
  HomeComponent,
  FilesComponent,
  LoginDemoComponent,
  MyTasksComponent,
  NewRequestComponent,
  PoliciesComponent,
  PolicyDetailsComponent,
  TasksComponent,
  ClaimsComponent,
  ClaimDetailsComponent
} from './components/index';

export const appRoutes: Routes = [
  {path: 'login', component: LoginDemoComponent},
  {
    path: '',
    component: HomeComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'home',
    component: HomeComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'files',
    component: FilesComponent,
    canActivate: [AuthGuardEcm]
  },
  {
    path: 'files/:id',
    component: FilesComponent,
    canActivate: [AuthGuardEcm]
  },
  {
    path: 'my-tasks',
    component: MyTasksComponent,
    canActivate: [AuthGuardBpm]
  },
  {
    path: 'start/:processKey',
    component: NewRequestComponent,
    canActivate: [AuthGuardBpm]
  },
  {
    path: 'policies',
    component: PoliciesComponent,
    canActivate: [AuthGuardBpm]
  },
  {
    path: 'policydetails/:id',
    component: PolicyDetailsComponent,
    canActivate: [AuthGuardBpm]
  },
  {
    path: 'claims',
    component: ClaimsComponent,
    canActivate: [AuthGuardBpm]
  },
  {
    path: 'claimdetails/:id',
    component: ClaimDetailsComponent,
    canActivate: [AuthGuardBpm]
  },
  {
    path: 'tasks',
    component: TasksComponent,
    canActivate: [AuthGuardBpm]
  }
];

export const routing: ModuleWithProviders = RouterModule.forRoot(appRoutes);
