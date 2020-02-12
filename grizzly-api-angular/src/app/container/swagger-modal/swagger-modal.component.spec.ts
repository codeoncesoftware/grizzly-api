import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SwaggerModalComponent } from './swagger-modal.component';

describe('SwaggerModalComponent', () => {
  let component: SwaggerModalComponent;
  let fixture: ComponentFixture<SwaggerModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SwaggerModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SwaggerModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
