import { request } from '@/utils/request';

export interface RegisterPayload {
  account: string;
  password: string;
  confirmPassword: string;
  name?: string;
  email?: string;
  idCard?: string;
  mobile?: string;
  captchaId: string;
  captchaCode: string;
}

export interface SmsSendPayload {
  phone: string;
}

export interface SmsLoginPayload {
  phone: string;
  code: string;
  force?: boolean;
}

export interface EmailSendPayload {
  email: string;
}

export interface EmailLoginPayload {
  email: string;
  code: string;
  force?: boolean;
}

export interface ResetPasswordPayload {
  account: string;
  phone: string;
  code: string;
  newPassword: string;
  confirmPassword: string;
}

export function register(payload: RegisterPayload) {
  return request.post<boolean>({ url: '/auth/register', data: payload }, { withToken: false });
}

export function sendSmsCode(payload: SmsSendPayload) {
  return request.post<{ expiresIn: number }>({ url: '/auth/sms/send', data: payload }, { withToken: false });
}

export function loginBySms(payload: SmsLoginPayload) {
  return request.post<any>({ url: '/auth/login/sms', data: payload }, { withToken: false });
}

export function sendEmailCode(payload: EmailSendPayload) {
  return request.post<{ expiresIn: number }>({ url: '/auth/email/send', data: payload }, { withToken: false });
}

export function loginByEmail(payload: EmailLoginPayload) {
  return request.post<any>({ url: '/auth/login/email', data: payload }, { withToken: false });
}

export function resetPassword(payload: ResetPasswordPayload) {
  return request.post<boolean>({ url: '/auth/password/reset', data: payload }, { withToken: false });
}
