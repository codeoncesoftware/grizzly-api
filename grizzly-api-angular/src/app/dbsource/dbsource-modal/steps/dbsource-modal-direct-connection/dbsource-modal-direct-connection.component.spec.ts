import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DbsourceModalDirectConnectionComponent } from './dbsource-modal-direct-connection.component';

describe('DbsourceModalDirectConnectionComponent', () => {
  let component: DbsourceModalDirectConnectionComponent;
  let fixture: ComponentFixture<DbsourceModalDirectConnectionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DbsourceModalDirectConnectionComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DbsourceModalDirectConnectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
