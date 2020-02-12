import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DbsourceModalFreeComponent } from './dbsource-modal-free.component';

describe('DbsourceModalFreeComponent', () => {
  let component: DbsourceModalFreeComponent;
  let fixture: ComponentFixture<DbsourceModalFreeComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DbsourceModalFreeComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DbsourceModalFreeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
