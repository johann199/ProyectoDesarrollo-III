import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { TokenService } from '../services/token.service';
import { apiConstants } from '../constants/api.constants';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const tokenService = inject(TokenService);
  const token = tokenService.getToken();

  if (token && !req.url.includes(apiConstants.LOGIN)) {
    req = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
  }

  return next(req);
};
