import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DbsourceComponent } from './dbsource.component';

describe('DbsourceComponent', () => {
  let component: DbsourceComponent;
  let fixture: ComponentFixture<DbsourceComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DbsourceComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DbsourceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
