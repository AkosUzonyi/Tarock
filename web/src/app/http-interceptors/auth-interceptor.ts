import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';

import { catchError, switchMap } from 'rxjs/operators';
import { AuthService } from '../_services/auth.service';
import { Router } from '@angular/router';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
    constructor(
        private authService: AuthService,
        private router: Router,
    ) { }

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<any> {
        const token = this.authService.getJwtToken();
        const isApiUrl = request.url.startsWith('/api');
        if (token && isApiUrl) {
            request = request.clone({
                setHeaders: { Authorization: `Bearer ${token}` }
            });
        }

        return next.handle(request).pipe(catchError(err => {
            if (err.status == 401 && !this.router.url.startsWith('/login'))
              this.router.navigate(['/login'], { queryParams: { returnUrl: this.router.url } });

            return throwError(err);
        }))
    }
}
