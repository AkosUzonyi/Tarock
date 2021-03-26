import { HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { of, throwError } from 'rxjs';
import { retryWhen, switchMap } from 'rxjs/operators';

@Injectable()
export class TimeoutInterceptor implements HttpInterceptor {
  constructor() {}

  intercept(req: HttpRequest<any>, next: HttpHandler) {
    return next.handle(req)
    .pipe(
      retryWhen(errors => errors.pipe(
        switchMap(error => {
          if (error.status == 408)
            return of(error);
          return throwError(error);
        })
      ))
    );
  }
}
