<script setup lang="ts">
  import axios from "axios";
  import type { Hct, HctList } from "../types";
  import { computed, onMounted, ref } from "vue";

  const http = axios.create({
    baseURL: "/",
    timeout: 1000,
  });

  interface Tab {
    label: string;
  }

  const Hcts = ref<HctList>({
    page: 1,
    size: 20,
    total: 0,
    items: [],
    first: true,
    last: false,
    hasNext: false,
    hasPrevious: false,
    totalPages: 0,
  });

  const tabs = [
    {
      label: "All",
    },
    {
      label: "Active",
    },
    {
      label: "Completed",
    },
  ];

  const activeTab = ref("All");

  /**
   * 列表展示的数据
   */
  const HctList = computed(() => {
    if (activeTab.value === "All") {
      return Hcts.value.items;
    }
    if (activeTab.value === "Active") {
      return filterByDone(false);
    }
    if (activeTab.value === "Completed") {
      return filterByDone(true);
    }
    return [];
  });

  const filterByDone = (done: boolean) => {
    return Hcts.value.items.filter((Hct) => Hct.spec.done === done);
  };

  // 查看 http://localhost:8090/swagger-ui.html
  function handleFetchHcts() {
    http
      .get<HctList>("/apis/Hct.plugin.halo.run/v1alpha1/Hcts")
      .then((response: { data: { page: number; size: number; total: number; items: { spec: { title: string; done?: boolean | undefined; }; apiVersion: "Hct.plugin.halo.run/v1alpha1"; kind: "Hct"; metadata: { name: string; labels?: { [key: string]: string; } | null | undefined; annotations?: { [key: string]: string; } | null | undefined; version?: number | null | undefined; creationTimestamp?: string | null | undefined; deletionTimestamp?: string | null | undefined; }; }[]; first: boolean; last: boolean; hasNext: boolean; hasPrevious: boolean; totalPages: number; }; }) => {
        Hcts.value = response.data;
      });
  }

  onMounted(handleFetchHcts);

  // 创建的逻辑

  const title = ref("");

  function handleCreate(e: Event) {
    http
      .post<Hct>("/apis/Hct.plugin.halo.run/v1alpha1/Hcts", {
        metadata: {
          // 根据 'Hct-' 前缀自动生成 Hct 的名称作为唯一标识，可以理解为数据库自动生成主键 id
          generateName: "Hct-",
        },
        spec: {
          title: title.value,
          done: false,
        },
        kind: "Hct",
        apiVersion: "Hct.plugin.halo.run/v1alpha1",
      })
      .then((response) => {
        title.value = "";
        handleFetchHcts();
      });
  }

  // 更新的逻辑
  const selectedHct = ref<Hct | undefined>();
  const handleUpdate = () => {
    http
      .put<Hct>(
        `/apis/Hct.plugin.halo.run/v1alpha1/Hcts/${selectedHct.value?.metadata.name}`,
        selectedHct.value
      )
      .then((response) => {
        handleFetchHcts();
      });
  };

  function handleDoneChange(Hct: Hct) {
    Hct.spec.done = !Hct.spec.done;
    http
      .put<Hct>(
        `/apis/Hct.plugin.halo.run/v1alpha1/Hcts/${Hct.metadata.name}`,
        Hct
      )
      .then((response) => {
        handleFetchHcts();
      });
  }

  // 删除
  const handleDelete = (Hct: Hct) => {
    http
      .delete(`/apis/Hct.plugin.halo.run/v1alpha1/Hcts/${Hct.metadata.name}`)
      .then((response) => {
        handleFetchHcts();
      });
  };
</script>

<template>
  <section class="Hctapp">
    <header class="header">
      <h1>Hcts</h1>
      <input
        class="new-Hct"
        autofocus
        v-model="title"
        placeholder="What needs to be done?"
        @keyup.enter="handleCreate"
      />
    </header>
    <section class="main" v-show="Hcts.items.length">
      <input
        id="toggle-all"
        class="toggle-all"
        type="checkbox"
        :checked="filterByDone(false).length > 0"
      />
      <label for="toggle-all">Mark all as complete</label>
      <ul class="Hct-list">
        <li
          v-for="(Hct, index) in HctList"
          class="Hct"
          :key="index"
          :class="{ completed: Hct.spec.done, editing: Hct === selectedHct }"
        >
          <div class="view">
            <input
              class="toggle"
              type="checkbox"
              :checked="Hct.spec.done"
              @click="handleDoneChange(Hct)"
            />
            <label @dblclick="selectedHct = Hct">{{ Hct.spec.title }}</label>
            <button class="destroy" @click="handleDelete(Hct)"></button>
          </div>
          <input
            v-if="selectedHct"
            class="edit"
            type="text"
            v-model="selectedHct.spec.title"
            @vnode-mounted="({ el }) => el.focus()"
            @blur="handleUpdate()"
            @keyup.enter="handleUpdate()"
            @keyup.escape="selectedHct = undefined"
          />
        </li>
      </ul>
    </section>
    <footer class="footer" v-show="Hcts.total">
      <span class="Hct-count">
        <strong>{{ filterByDone(false).length }}</strong>
        <span>
          {{ filterByDone(false).length === 1 ? " item" : " items" }} left</span
        >
      </span>
      <ul class="filters">
        <li v-for="(tab, index) in tabs" :key="index">
          <a
            href="javascript:void(0);"
            @click="activeTab = tab.label"
            :class="{ selected: activeTab === tab.label }"
          >
            {{ tab.label }}
          </a>
        </li>
      </ul>
      <button
        class="clear-completed"
        @click="() => filterByDone(true).map((Hct) => handleDelete(Hct))"
        v-show="Hcts.items.length > filterByDone(false).length"
      >
        Clear completed
      </button>
    </footer>
  </section>
</template>

<style scoped>
  @import "Hctmvc-app-css/index.css";
</style>
