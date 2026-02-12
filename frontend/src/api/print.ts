import type { AxiosResponse } from 'axios';

import { downloadBlobResponse } from '@/utils/importExport';
import { request } from '@/utils/request';

export interface PrintDefinition {
  definitionId: string;
  name: string;
  permission?: string;
  templateType: string;
}

export interface PrintJobRequest {
  businessRef: string;
  definitionId: string;
  mode?: string;
  params?: Record<string, unknown>;
}

interface CreatePrintJobResult {
  jobId: string;
}

export function fetchPrintDefinitions() {
  return request.get<PrintDefinition[]>({
    url: '/print/definitions',
  });
}

export function createPrintJob(payload: PrintJobRequest) {
  return request.post<CreatePrintJobResult>({
    url: '/print/jobs',
    data: payload,
  });
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
