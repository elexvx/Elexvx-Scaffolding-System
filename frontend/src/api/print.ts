import type { AxiosResponse } from 'axios';

import { downloadBlobResponse } from '@/utils/importExport';
import { request } from '@/utils/request';

export interface PrintTemplateItem {
  id: number;
  biz_type: string;
  name: string;
  html: string;
  css: string;
  current_version: number;
}

export interface RenderHtmlPayload {
  templateId?: number;
  templateKey?: string;
  version?: number;
  data: Record<string, unknown>;
}

export function fetchPrintTemplates(params: { bizType?: string; enabled?: number }) {
  return request.get<PrintTemplateItem[]>({ url: '/module-api/print/templates', params });
}

export function fetchTemplateVersions(templateId: number) {
  return request.get<Array<{ version: number; published: number; created_at: string }>>({ url: `/module-api/print/templates/${templateId}/versions` });
}

export function renderPrintHtml(data: RenderHtmlPayload) {
  return request.post<{ computedHtml: string }>({ url: '/module-api/print/render/html', data });
}

export function renderPrintPdf(data: RenderHtmlPayload & { bizType?: string; bizId?: string }) {
  return request.post<{ fileUrl: string; jobId: number }>({ url: '/module-api/print/render/pdf', data });
}

export function fetchPrintJobs(params: {
  page?: number;
  pageSize?: number;
  bizType?: string;
  bizId?: string;
  templateId?: number;
  createdAtFrom?: string;
  createdAtTo?: string;
}) {
  return request.get<{ items: any[]; total: number; page: number; pageSize: number }>({ url: '/module-api/print/jobs', params });
}

// backward-compatible exports for existing console page
export interface PrintDefinition {
  definitionId: string;
  name: string;
  templateType: string;
}

export interface PrintJobRequest {
  businessRef: string;
  definitionId: string;
  mode?: string;
  params?: Record<string, unknown>;
}

export function fetchPrintDefinitions() {
  return request.get<PrintDefinition[]>({ url: '/print/definitions' });
}

export function createPrintJob(payload: PrintJobRequest) {
  return request.post<{ jobId: string }>({ url: '/print/jobs', data: payload });
}

export function downloadPrintJobFile(jobId: string) {
  return request.get<AxiosResponse<Blob>>(
    { url: `/print/jobs/${jobId}/file`, responseType: 'blob' },
    { isReturnNativeResponse: true, isTransformResponse: false },
  );
}

export async function downloadPrintJobPdf(jobId: string) {
  const response = await downloadPrintJobFile(jobId);
  await downloadBlobResponse(response, `${jobId}.pdf`);
}
