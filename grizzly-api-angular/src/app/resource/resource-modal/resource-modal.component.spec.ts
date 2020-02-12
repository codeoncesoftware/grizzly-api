import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ResourceModalComponent } from './resource-modal.component';

describe('ResourceModalComponent', () => {
  let component: ResourceModalComponent;
  let fixture: ComponentFixture<ResourceModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ResourceModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ResourceModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
