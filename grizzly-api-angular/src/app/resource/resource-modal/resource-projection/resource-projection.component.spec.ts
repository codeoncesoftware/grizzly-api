import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ResourceProjectionComponent } from './resource-projection.component';

describe('ResourceProjectionComponent', () => {
  let component: ResourceProjectionComponent;
  let fixture: ComponentFixture<ResourceProjectionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ResourceProjectionComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ResourceProjectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
