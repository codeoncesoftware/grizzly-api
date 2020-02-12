import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DbsourceListComponent } from './dbsource-list.component';

describe('DbsourceListComponent', () => {
  let component: DbsourceListComponent;
  let fixture: ComponentFixture<DbsourceListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DbsourceListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DbsourceListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
