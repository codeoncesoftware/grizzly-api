import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ResourceDetailsModalComponent } from './resource-details-modal.component';

describe('ResourceDetailsModalComponent', () => {
  let component: ResourceDetailsModalComponent;
  let fixture: ComponentFixture<ResourceDetailsModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ResourceDetailsModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ResourceDetailsModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
