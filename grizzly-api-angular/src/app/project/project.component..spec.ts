import { ProjectComponent } from './project.component';
import { ComponentFixture, async, TestBed } from '@angular/core/testing';
import { DebugElement } from '@angular/core';

describe('ProjectComponent', () => {
    // Define Component To Test
    let component: ProjectComponent;
    // Test Environment for Component
    let fixture: ComponentFixture<ProjectComponent>;
    // Rendered HTML of the Component
    let debug: DebugElement;


    beforeEach(async(() => {
        TestBed.configureTestingModule({
            // Isolated Test For The Component
            declarations: [ProjectComponent],
        })
        // Compile HTML and CSS
        .compileComponents();
    }));
    beforeEach(() => {
        fixture = TestBed.createComponent(ProjectComponent);
        component = fixture.componentInstance;
        debug = fixture.debugElement;
    });
    it('Should create', () => {
        expect(component).toBeTruthy();
    });
});

