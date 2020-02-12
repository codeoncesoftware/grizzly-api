import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ResourceGroupModalComponent } from './resource-group-modal.component';

describe('ResourceGroupModalComponent', () => {
  let component: ResourceGroupModalComponent;
  let fixture: ComponentFixture<ResourceGroupModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ResourceGroupModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ResourceGroupModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
