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

import { MatTableModule } from '@angular/material/table';
import { FormsModule } from '@angular/forms';
import { ResourceGroupComponent } from './resource-group.component';
import { ResourceGroupModalComponent } from './resource-group-modal/resource-group-modal.component';

import { ResourceModalComponent } from '../resource/resource-modal/resource-modal.component';
import { ResourceModule } from '../resource/resource.module';
import { SharedModule } from '../shared/shared.module';
import { SecurityModalComponent } from './security-modal/security-modal.component';
import { SelectFilesModalComponent } from '../resource/resource-modal/select-files-modal/select-files-modal.component';
import { FileSearchComponent } from '../resource/resource-modal/select-files-modal/file-search/file-search.component';
import { ExecuteModalComponent } from '../resource/execute-modal/execute-modal.component';

@NgModule({
  declarations: [
    ResourceGroupComponent,
    ResourceGroupModalComponent,
    SecurityModalComponent
  ],
  imports: [
    FormsModule,
    ResourceModule,
    CommonModule,
    RouterModule,
    // ResourceGroupRoutingModule,
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
  exports: [ResourceGroupComponent],
  entryComponents: [
    ResourceGroupComponent,
    ResourceGroupModalComponent,
    SelectFilesModalComponent,
    FileSearchComponent,
    ResourceModalComponent,
    ExecuteModalComponent,
    SecurityModalComponent]
})
export class ResourceGroupModule { }
