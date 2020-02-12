import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DbsourceModalComponent } from './dbsource-modal.component';

describe('DbsourceModalComponent', () => {
  let component: DbsourceModalComponent;
  let fixture: ComponentFixture<DbsourceModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DbsourceModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DbsourceModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
