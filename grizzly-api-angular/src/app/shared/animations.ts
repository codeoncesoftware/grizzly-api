import { trigger, state, style, transition,
    animate
} from '@angular/animations';

export const SlideInOutAnimation = [
    trigger('slideUpDown', [
        state('0', style({ 'max-height': '100%', opacity: 1 })),
        state('1', style({ 'max-height': '0px', opacity: 0 })),
        transition(':enter', animate('100ms ease-in-out')),
        transition('* => *', animate('100ms ease-in-out')),
      ])
];
