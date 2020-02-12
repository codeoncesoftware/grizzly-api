import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatBadgeModule } from '@angular/material/badge';
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
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatTreeModule } from '@angular/material/tree';
import { RouterModule } from '@angular/router';
import { ConfirmModalComponent } from '../shared/confirm-modal/confirm-modal.component';
import { SharedModule } from '../shared/shared.module';
import { DbsourceDetailsComponent } from './dbsource-details/dbsource-details.component';
import { DomainModalComponent } from './dbsource-list/domain/domain-modal/domain-modal.component';
import { DomainComponent } from './dbsource-list/domain/domain.component';
import { DbsourceModalDirectConnectionComponent } from './dbsource-modal/steps/dbsource-modal-direct-connection/dbsource-modal-direct-connection.component';
import { DbsourceModalFreeComponent } from './dbsource-modal/steps/dbsource-modal-free/dbsource-modal-free.component';
import { DbsourceModalProviderComponent } from './dbsource-modal/steps/dbsource-modal-provider/dbsource-modal-provider.component';
import { DbsourceModalReviewComponent } from './dbsource-modal/steps/dbsource-modal-review/dbsource-modal-review.component';
import { DBSourceRoutingModule } from './dbsource-routing.module';
import { DbsourceComponent } from './dbsource.component';
import { DbsourceModalCloudConnectionComponent } from './dbsource-modal/steps/dbsource-modal-cloud-connection/dbsource-modal-cloud-connection.component';
import { DbsourceModalComponent } from './dbsource-modal/dbsource-modal.component';


@NgModule({
  declarations: [
    DbsourceComponent,
    DbsourceDetailsComponent,
    DomainComponent,
    DomainModalComponent,
    DbsourceModalComponent,
    DbsourceModalProviderComponent,
    DbsourceModalDirectConnectionComponent,
    DbsourceModalFreeComponent,
    DbsourceModalReviewComponent,
    DbsourceModalCloudConnectionComponent
  ],
  imports: [
    FormsModule,
    CommonModule,
    RouterModule,
    DBSourceRoutingModule,
    MatTreeModule,
    MatBadgeModule,
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
  exports: [DbsourceComponent, DbsourceModalComponent],
  entryComponents: [DomainModalComponent, ConfirmModalComponent]
})
export class DBSourceModule { }
