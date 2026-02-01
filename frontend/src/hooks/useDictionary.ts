import { computed, ref } from 'vue';

import type { SysDictItem } from '@/api/system/dictionary';
import { useDictionaryStore } from '@/store';

export const useDictionary = (code: string) => {
  const store = useDictionaryStore();
  const loading = ref(false);

  const items = computed<SysDictItem[]>(() => store.dictCache[code] || []);
  const options = computed(() =>
    items.value.map((item) => ({ label: item.label, value: item.value, disabled: item.status !== 1 })),
  );

  const load = async (force = false) => {
    loading.value = true;
    try {
      await store.getDictItems(code, force);
    } finally {
      loading.value = false;
    }
  };

  return {
    items,
    options,
    load,
    loading,
  };
};
