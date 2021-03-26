import { HTTP_INTERCEPTORS } from '@angular/common/http';

import { TimeoutInterceptor } from './timeout-interceptor';

/** Http interceptor providers in outside-in order */
export const httpInterceptorProviders = [
  { provide: HTTP_INTERCEPTORS, useClass: TimeoutInterceptor, multi: true },
];
