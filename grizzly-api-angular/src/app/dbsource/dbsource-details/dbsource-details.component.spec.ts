import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DbsourceDetailsComponent } from './dbsource-details.component';

describe('DbsourceDetailsComponent', () => {
  let component: DbsourceDetailsComponent;
  let fixture: ComponentFixture<DbsourceDetailsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DbsourceDetailsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DbsourceDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
