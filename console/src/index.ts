import { definePlugin } from "@halo-dev/console-shared";
import HomeView from "./views/HomeView.vue";
import { IconPlug } from "@halo-dev/components";
import { markRaw } from "vue";

export default definePlugin({
  components: {},
  routes: [
    {
      parentName: "Root",
      route: {
        path: "/example",
        name: "HctPlugin",
        component: HomeView,
        meta: {
          title: "火车头插件配置",
          searchable: true,
          menu: {
            name: "插件配置",
            group: "火车头",
            icon: markRaw(IconPlug),
            priority: 0,
          },
        },
      },
    },
  ],
  extensionPoints: {},
});
