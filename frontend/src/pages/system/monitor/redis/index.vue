<template>
  <div class="redis-monitor">
    <t-card v-if="!redisEnabled" :bordered="false">
      <t-alert theme="warning" title="提示">
        <template #message>
          <div>
            <p>Redis 监控功能当前未启用。</p>
            <p>如需使用此功能，请在系统配置中启用 Redis 并确保 Redis 服务正在运行。</p>
          </div>
        </template>
      </t-alert>
    </t-card>

    <template v-else>
      <t-loading :loading="loading">
        <t-card v-if="!redisAvailable" :bordered="false">
          <t-alert theme="error" title="错误">
            <template #message>
              <div>
                <p>无法连接到 Redis 服务。</p>
                <p>{{ redisMessage || '请检查 Redis 服务是否正在运行，以及配置是否正确。' }}</p>
              </div>
            </template>
          </t-alert>
        </t-card>

        <template v-else>
          <t-card title="Redis信息" :bordered="false">
            <template #actions>
              <t-space>
                <t-button variant="outline" size="small" @click="() => loadRedisInfo({ showLoading: true })">
                  立即刷新
                </t-button>
              </t-space>
            </template>
            <t-space direction="vertical" style="width: 100%">
              <t-row :gutter="16">
                <t-col :span="3">
                  <t-statistic title="Redis版本" :value="(redisInfo.redis_version || '-') as unknown as number" />
                </t-col>
                <t-col :span="3">
                  <t-statistic title="运行模式" :value="(redisInfo.redis_mode || '-') as unknown as number" />
                </t-col>
                <t-col :span="3">
                  <t-statistic title="端口" :value="tcpPort" />
                </t-col>
                <t-col :span="3">
                  <t-statistic title="客户端数" :value="connectedClients" />
                </t-col>
              </t-row>

              <t-row :gutter="16">
                <t-col :span="3">
                  <t-statistic title="运行时间(天)" :value="uptimeDays" />
                </t-col>
                <t-col :span="3">
                  <t-statistic title="内存使用量" :value="usedMemoryMb" unit="MB" />
                </t-col>
                <t-col :span="3">
                  <t-statistic title="内存峰值" :value="usedMemoryPeakMb" unit="MB" />
                </t-col>
                <t-col :span="3">
                  <t-statistic title="命中率" :value="hitRate" unit="%" />
                </t-col>
              </t-row>

              <t-row :gutter="16">
                <t-col :span="3">
                  <t-statistic title="命中次数" :value="hitCount" />
                </t-col>
                <t-col :span="3">
                  <t-statistic title="未命中次数" :value="missCount" />
                </t-col>
                <t-col :span="3">
                  <t-statistic title="QPS" :value="redisInfo.instantaneous_ops_per_sec ?? 0" />
                </t-col>
                <t-col :span="3">
                  <t-statistic title="键数量" :value="redisInfo.keyCount ?? 0" />
                </t-col>
              </t-row>
            </t-space>
          </t-card>

          <t-row :gutter="16" style="margin-top: 16px">
            <t-col :xs="24" :sm="12" :md="6">
              <t-card title="内存趋势 (MB)" :bordered="false">
                <div ref="memoryChartRef" class="chart"></div>
              </t-card>
            </t-col>
            <t-col :xs="24" :sm="12" :md="6">
              <t-card title="吞吐趋势 (OPS)" :bordered="false">
                <div ref="opsChartRef" class="chart"></div>
              </t-card>
            </t-col>
          </t-row>

          <t-card title="命令统计" :bordered="false" style="margin-top: 16px">
            <t-table :data="commandStats" :columns="commandColumns" row-key="command" :pagination="null" />
          </t-card>

          <t-card title="Key信息" :bordered="false" style="margin-top: 16px">
            <t-table :data="keyspaceInfo" :columns="keyspaceColumns" row-key="db" :pagination="null" />
          </t-card>
        </template>
      </t-loading>
    </template>
  </div>
</template>
<script setup lang="ts">
import * as echarts from 'echarts';
import debounce from 'lodash/debounce';
import type { PrimaryTableCol } from 'tdesign-vue-next';
import { MessagePlugin } from 'tdesign-vue-next';
import { computed, nextTick, onActivated, onDeactivated, onMounted, onUnmounted, ref } from 'vue';

import { request } from '@/utils/request';

interface RedisInfo {
  redis_version?: string;
  redis_mode?: string;
  tcp_port?: string;
  connected_clients?: string;
  uptime_in_days?: string;
  used_memory_human?: string;
  used_memory_peak_human?: string;
  mem_fragmentation_ratio?: string;
  keyspace_hits?: string;
  keyspace_misses?: string;
  instantaneous_ops_per_sec?: number;
  keyCount?: number;
  timestamp?: number;
  usedMemory?: number;
  hitCount?: number;
  missCount?: number;
  [key: string]: string | number | undefined;
}

interface CommandStat {
  command: string;
  calls: string;
  usec: string;
  usecPerCall: string;
}

interface KeyspaceInfo {
  db: string;
  keys: string;
  expires: string;
  avgTtl: string;
}

const redisInfo = ref<RedisInfo>({});
const commandStats = ref<CommandStat[]>([]);
const keyspaceInfo = ref<KeyspaceInfo[]>([]);
const redisEnabled = ref<boolean>(true);
const redisAvailable = ref<boolean>(true);
const redisMessage = ref<string>('');
const loading = ref(false);

interface MetricPoint {
  timestamp: number;
  memory: number;
  ops: number;
}

const history = ref<MetricPoint[]>([]);
const memoryChartRef = ref<HTMLDivElement>();
const opsChartRef = ref<HTMLDivElement>();
let memoryChart: echarts.ECharts | null = null;
let opsChart: echarts.ECharts | null = null;

const safeInt = (v?: string | number) => {
  if (v == null) return 0;
  if (typeof v === 'number' && Number.isFinite(v)) return v;
  const n = Number.parseInt(String(v), 10);
  return Number.isFinite(n) ? n : 0;
};

const parseHumanToMb = (v?: string) => {
  const raw = String(v || '').trim();
  if (!raw) return 0;
  const m = raw.match(/^(-?\d+(?:\.\d+)?)\s*([KMGTP]?)B?$/i);
  if (!m) return 0;
  const num = Number(m[1]);
  if (!Number.isFinite(num)) return 0;
  const unit = String(m[2] || '').toUpperCase();
  const factor =
    unit === 'K'
      ? 1 / 1024
      : unit === 'M'
        ? 1
        : unit === 'G'
          ? 1024
          : unit === 'T'
            ? 1024 * 1024
            : unit === 'P'
              ? 1024 * 1024 * 1024
              : 1 / (1024 * 1024);
  return Number((num * factor).toFixed(2));
};

const tcpPort = computed(() => safeInt(redisInfo.value.tcp_port));
const connectedClients = computed(() => safeInt(redisInfo.value.connected_clients));
const uptimeDays = computed(() => safeInt(redisInfo.value.uptime_in_days));
const hitCount = computed(() => safeInt(redisInfo.value.keyspace_hits));
const missCount = computed(() => safeInt(redisInfo.value.keyspace_misses));
const hitRate = computed(() => {
  const total = hitCount.value + missCount.value;
  if (total <= 0) return 0;
  return Number(((hitCount.value / total) * 100).toFixed(2));
});
const usedMemoryMb = computed(() => {
  const bytes = Number(redisInfo.value.usedMemory || 0);
  if (Number.isFinite(bytes) && bytes > 0) return Number((bytes / 1024 / 1024).toFixed(2));
  return parseHumanToMb(redisInfo.value.used_memory_human);
});
const usedMemoryPeakMb = computed(() => parseHumanToMb(redisInfo.value.used_memory_peak_human));

const commandColumns: PrimaryTableCol[] = [
  { colKey: 'command', title: '命令', width: 200 },
  { colKey: 'calls', title: '调用次数', width: 200 },
  { colKey: 'usec', title: '耗时(ms)', width: 200 },
  { colKey: 'usecPerCall', title: '平均耗时(ms)', width: 200 },
];

const keyspaceColumns: PrimaryTableCol[] = [
  { colKey: 'db', title: '数据库', width: 150 },
  { colKey: 'keys', title: '键数量', width: 150 },
  { colKey: 'expires', title: '过期键数量', width: 150 },
  { colKey: 'avgTtl', title: '平均TTL', width: 150 },
];

interface RedisHealthStatus {
  redisEnabled: boolean;
  redisAvailable: boolean;
  redisMessage?: string;
}

interface RedisMonitorResponse {
  info: RedisInfo;
  commandStats: CommandStat[];
  keyspace: KeyspaceInfo[];
  timestamp?: number;
  usedMemory?: number;
  totalCommandsProcessed?: number;
  instantaneousOpsPerSec?: number;
  connectedClients?: number;
  keyCount?: number;
  hitCount?: number;
  missCount?: number;
}

const applyRedisInfo = (nextInfo: RedisInfo) => {
  const current = redisInfo.value as Record<string, string | number | undefined>;
  const next = nextInfo as Record<string, string | number | undefined>;

  Object.keys(current).forEach((key) => {
    if (!(key in next)) delete current[key];
  });

  Object.keys(next).forEach((key) => {
    if (current[key] !== next[key]) current[key] = next[key];
  });
};

function replaceIfChanged<T>(currentValue: T, nextValue: T) {
  try {
    return JSON.stringify(currentValue) === JSON.stringify(nextValue) ? currentValue : nextValue;
  } catch {
    return nextValue;
  }
}

const debouncedRenderCharts = debounce(() => {
  renderCharts();
}, 200);

const loadRedisInfo = async (options: { showLoading?: boolean } = {}) => {
  const showLoading = options.showLoading ?? false;
  if (showLoading) loading.value = true;
  try {
    const healthResponse = await request.get<RedisHealthStatus>({
      url: '/health',
    });
    redisEnabled.value = healthResponse.redisEnabled;
    redisAvailable.value = healthResponse.redisAvailable;
    redisMessage.value = healthResponse.redisMessage || '';

    if (redisEnabled.value && redisAvailable.value) {
      const response = await request.get<RedisMonitorResponse>({
        url: '/system/monitor/redis',
      });
      const info: RedisInfo = {
        ...(response.info || {}),
      };
      if (response.timestamp != null) info.timestamp = response.timestamp;
      if (response.usedMemory != null) info.usedMemory = response.usedMemory;
      if (response.instantaneousOpsPerSec != null) {
        info.instantaneous_ops_per_sec =
          typeof info.instantaneous_ops_per_sec === 'number'
            ? info.instantaneous_ops_per_sec
            : response.instantaneousOpsPerSec;
      }
      if (response.keyCount != null) info.keyCount = response.keyCount;
      if (response.hitCount != null) info.hitCount = response.hitCount;
      if (response.missCount != null) info.missCount = response.missCount;

      applyRedisInfo(info);
      commandStats.value = replaceIfChanged(commandStats.value, response.commandStats || []);
      keyspaceInfo.value = replaceIfChanged(keyspaceInfo.value, response.keyspace || []);
      pushHistory();
      await nextTick();
      debouncedRenderCharts();
    }
  } catch (error) {
    console.error('获取Redis信息失败:', error);
    MessagePlugin.error('获取 Redis 信息失败，请检查后端日志');
    redisAvailable.value = false;
    redisMessage.value = '无法获取详细监控数据，可能是连接中断或配置错误。';
  } finally {
    if (showLoading) loading.value = false;
  }
};

let timer: number | null = null;

const REFRESH_MS = 3000;

const stop = () => {
  if (timer) {
    clearInterval(timer);
    timer = null;
  }
};

const start = () => {
  stop();
  loadRedisInfo({ showLoading: true });
  timer = window.setInterval(() => {
    loadRedisInfo({ showLoading: false });
  }, REFRESH_MS);
};

onMounted(start);
onActivated(start);
onDeactivated(stop);
onUnmounted(() => {
  stop();
  memoryChart?.dispose();
  opsChart?.dispose();
  memoryChart = null;
  opsChart = null;
  debouncedRenderCharts.cancel();
});

const pushHistory = () => {
  const point: MetricPoint = {
    timestamp: Number(redisInfo.value.timestamp || Date.now()),
    memory: Number(redisInfo.value.usedMemory || 0) / 1024 / 1024,
    ops: Number(redisInfo.value.instantaneous_ops_per_sec || 0),
  };
  history.value.push(point);
  if (history.value.length > 30) history.value.shift();
};

const renderCharts = () => {
  const labels = history.value.map((p) => new Date(p.timestamp).toLocaleTimeString().replace(/^\d{2}:/, ''));
  const memoryData = history.value.map((p) => Number(p.memory.toFixed(2)));
  const opsData = history.value.map((p) => Number(p.ops.toFixed(2)));

  if (memoryChartRef.value && !memoryChart) {
    memoryChart = echarts.init(memoryChartRef.value);
  }
  if (opsChartRef.value && !opsChart) {
    opsChart = echarts.init(opsChartRef.value);
  }

  memoryChart?.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: labels },
    yAxis: { type: 'value', axisLabel: { formatter: '{value} MB' } },
    series: [{ type: 'line', data: memoryData, smooth: true, areaStyle: {} }],
  });

  opsChart?.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: labels },
    yAxis: { type: 'value', axisLabel: { formatter: '{value}' } },
    series: [{ type: 'line', data: opsData, smooth: true, areaStyle: {} }],
  });
};
</script>
<style lang="less" scoped>
.redis-monitor {
  :deep(.t-card) {
    .t-card__title {
      font-weight: 500;
    }
  }

  :deep(.t-statistic) {
    .t-statistic__title {
      color: var(--td-text-color-secondary);
    }
  }

  .chart {
    height: 240px;
  }
}
</style>
