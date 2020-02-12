import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DbsourceModalCloudConnectionComponent } from './dbsource-modal-cloud-connection.component';

describe('DbsourceModalCloudConnectionComponent', () => {
  let component: DbsourceModalCloudConnectionComponent;
  let fixture: ComponentFixture<DbsourceModalCloudConnectionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DbsourceModalCloudConnectionComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DbsourceModalCloudConnectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
