import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DbsourceModalProviderComponent } from './dbsource-modal-provider.component';

describe('DbsourceModalProviderComponent', () => {
  let component: DbsourceModalProviderComponent;
  let fixture: ComponentFixture<DbsourceModalProviderComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DbsourceModalProviderComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DbsourceModalProviderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
