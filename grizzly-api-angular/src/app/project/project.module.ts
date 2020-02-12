import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatChipsModule } from '@angular/material/chips';
import { MatNativeDateModule, MatRippleModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialogModule } from '@angular/material/dialog';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSliderModule } from '@angular/material/slider';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatSortModule } from '@angular/material/sort';
import { MatStepperModule } from '@angular/material/stepper';
import { MatTabsModule } from '@angular/material/tabs';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatTreeModule } from '@angular/material/tree';

import {MatTableModule} from '@angular/material/table';

import { ProjectRoutingModule } from './project-routing.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ProjectComponent } from './project.component';
import { ResourceGroupModalComponent } from '../resource-group/resource-group-modal/resource-group-modal.component';
import { ResourceModalComponent } from '../resource/resource-modal/resource-modal.component';
import { ContainerModule } from '../container/container.module';
import { SharedModule } from '../shared/shared.module';
import { ConfirmModalComponent } from '../shared/confirm-modal/confirm-modal.component';
import { LoaderComponent } from '../shared/loader/loader.component';
import { SecurityModalComponent } from '../resource-group/security-modal/security-modal.component';
import { RoleModalComponent } from './role-modal/role-modal.component';
import { SelectFilesModalComponent } from '../resource/resource-modal/select-files-modal/select-files-modal.component';
import { ExecuteModalComponent } from '../resource/execute-modal/execute-modal.component';

@NgModule({
  declarations: [
    ProjectComponent,
    RoleModalComponent
  ],
  imports: [
    ContainerModule,
    FormsModule,
    ReactiveFormsModule,
    CommonModule,
    RouterModule,
    ProjectRoutingModule,
    MatAutocompleteModule,
    MatButtonModule,
    MatButtonToggleModule,
    MatCardModule,
    MatCheckboxModule,
    MatChipsModule,
    MatDatepickerModule,
    MatDialogModule,
    MatExpansionModule,
    MatGridListModule,
    MatIconModule,
    MatInputModule,
    MatListModule,
    MatMenuModule,
    MatNativeDateModule,
    MatPaginatorModule,
    MatProgressBarModule,
    MatProgressSpinnerModule,
    MatRadioModule,
    MatRippleModule,
    MatSelectModule,
    MatSidenavModule,
    MatSliderModule,
    MatSlideToggleModule,
    MatSnackBarModule,
    MatSortModule,
    MatTableModule,
    MatTabsModule,
    MatToolbarModule,
    MatTooltipModule,
    MatStepperModule,
    MatTreeModule,
    SharedModule
  ],
  exports: [ProjectComponent],
  entryComponents: [
    ResourceGroupModalComponent,
    SecurityModalComponent ,
    ResourceModalComponent ,
    ExecuteModalComponent,
    ConfirmModalComponent,
    LoaderComponent,
    RoleModalComponent,
    SelectFilesModalComponent]
})
export class ProjectModule { }
