import { async, ComponentFixture, TestBed, inject } from '@angular/core/testing';

import { ContainerModalComponent } from './container-modal.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SharedModule } from 'src/app/shared/shared.module';
import { MatDialog, MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ContainerService } from '../container.service';
import { Store, StoreModule } from '@ngrx/store';
import { BrowserDynamicTestingModule } from '@angular/platform-browser-dynamic/testing';
import { ContainerState } from 'src/app/store/container/container.state';
import { containerReducer } from 'src/app/store/container/container.reducer';
import { HttpClientModule } from '@angular/common/http';
import { TranslateModule, TranslateService, TranslateLoader, TranslateFakeLoader } from '@ngx-translate/core';
import { Injector } from '@angular/core';


describe('ContainerModalComponent', () => {
  let component: ContainerModalComponent;
  let fixture: ComponentFixture<ContainerModalComponent>;
  let store: Store<ContainerState>;


  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ContainerModalComponent ],
      imports: [
        SharedModule,
        FormsModule,
        MatFormFieldModule,
        StoreModule.forRoot({containers: containerReducer}),
        BrowserDynamicTestingModule,
        MatDialogModule,
        HttpClientModule,
        TranslateModule.forRoot({
          loader: { provide: TranslateLoader, useClass: TranslateFakeLoader }
        }),
        MatInputModule
      ],
      providers: [
        ContainerService,
        Store,
        Injector,
        { provide: MatDialogRef, useValue: {} },
        { provide: MAT_DIALOG_DATA, useValue: {} }]
    }).overrideModule(BrowserDynamicTestingModule, {
      set: {
        entryComponents: [ContainerModalComponent]
      }
    })
    .compileComponents();
  }));

  beforeEach(inject([Store, MAT_DIALOG_DATA], (testStore: Store<ContainerState>) => {
    store = testStore;
    fixture = TestBed.createComponent(ContainerModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {

  });
});
