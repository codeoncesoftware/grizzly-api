import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DomainModalComponent } from './domain-modal.component';

describe('DomainModalComponent', () => {
  let component: DomainModalComponent;
  let fixture: ComponentFixture<DomainModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DomainModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DomainModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
