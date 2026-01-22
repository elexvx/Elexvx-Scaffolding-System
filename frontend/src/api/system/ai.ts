import { request } from '@/utils/request';

export interface AiProviderPayload {
  id?: number;
  name: string;
  vendor: string;
  baseUrl: string;
  endpointPath?: string;
  model?: string;
  apiKey?: string;
  apiVersion?: string;
  temperature?: number;
  maxTokens?: number;
  isDefaultProvider?: boolean;
  enabled?: boolean;
  extraHeaders?: string;
  remark?: string;
  reuseApiKey?: boolean;
}

export interface AiProvider {
  id: number;
  name: string;
  vendor: string;
  baseUrl: string;
  endpointPath?: string;
  model?: string;
  apiVersion?: string;
  temperature?: number;
  maxTokens?: number;
  isDefaultProvider?: boolean;
  enabled?: boolean;
  remark?: string;
  apiKeyConfigured: boolean;
  lastTestStatus?: string;
  lastTestMessage?: string;
  lastTestedAt?: string;
}

export interface AiTestResult {
  success: boolean;
  message: string;
  durationMs: number;
}

export function fetchAiProviders() {
  return request.get<AiProvider[]>({ url: '/ai/providers' });
}

export function saveAiProvider(payload: AiProviderPayload) {
  return request.post<AiProvider>({ url: '/ai/providers', data: payload });
}

export function deleteAiProvider(id: number) {
  return request.delete({ url: `/ai/providers/${id}` });
}

export function testAiProvider(payload: AiProviderPayload) {
  return request.post<AiTestResult>({ url: '/ai/providers/test', data: payload });
}

export function testSavedProvider(id: number) {
  return request.post<AiTestResult>({ url: `/ai/providers/${id}/test` });
}
