import { Injectable } from '@angular/core';
import { BehaviorSubject, from, Observable, throwError } from 'rxjs';

import { ApiService } from '../_services/api.service';
import { LoginResult, User } from '../_models/dto';
import { SocialAuthService, FacebookLoginProvider, GoogleLoginProvider, SocialUser } from 'angularx-social-login';
import { map, switchMap } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class AuthService {
    private loginResult = new BehaviorSubject<LoginResult | null>(null);
    private socialUser: SocialUser | null = null;

    private apiLoginTimeout: any = null;

    constructor(
        private socialAuthService: SocialAuthService,
        private apiService: ApiService,
    ) { }

    getJwtToken(): string | null {
        return this.loginResult.value?.token ?? null;
    }

    getUser(): User | null {
        return this.loginResult.value?.user ?? null;
    }

    getUserObservable(): Observable<User | null> {
        return this.loginResult.pipe(map(lr => lr?.user ?? null));
    }

    init() {
        this.socialAuthService.authState.subscribe(socialUser => {
            this.socialUser = socialUser;
            this.apiLogin();
        });
    }

    private apiLogin() {
        if (this.socialUser === null) {
            this.logout();
            return;
        }

        const provider = this.convertProviderId(this.socialUser.provider);
        const token = this.socialUser.idToken;
        this.apiService.login(provider, token).subscribe(loginResult => {
            this.loginResult.next(loginResult);
            this.startApiLoginTimeout();
        }, error => {
            this.logout();
        });
    }

    private convertProviderId(socialProviderId : string): string {
        switch (socialProviderId) {
            case FacebookLoginProvider.PROVIDER_ID: return 'facebook';
            case GoogleLoginProvider.PROVIDER_ID: return 'google';
        }
        throw 'unknown provider id: ' + socialProviderId;
    }

    loginFacebook() {
        this.socialAuthService.signIn(FacebookLoginProvider.PROVIDER_ID);
    }

    loginGoogle() {
        this.socialAuthService.signIn(GoogleLoginProvider.PROVIDER_ID);
    }

    logout() {
        this.socialUser = null;
        this.loginResult.next(null);
        this.socialAuthService.signOut();
    }

    private startApiLoginTimeout() {
        if (this.apiLoginTimeout !== null)
            clearTimeout(this.apiLoginTimeout);

        const jwtToken = this.getJwtToken();
        if (jwtToken === null)
            return;

        const payload = JSON.parse(atob(jwtToken.split('.')[1]));
        const expires = new Date(payload.exp * 1000);
        const timeout = (expires.getTime() - Date.now()) * 0.9;
        this.apiLoginTimeout = setTimeout(() => this.apiLogin(), timeout);
    }
}
