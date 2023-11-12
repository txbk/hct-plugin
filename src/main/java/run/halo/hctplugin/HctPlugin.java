package run.halo.hctplugin;

import org.pf4j.PluginWrapper;
import org.springframework.stereotype.Component;
import run.halo.app.extension.Scheme;
import run.halo.app.plugin.BasePlugin;
import run.halo.app.extension.SchemeManager;

@Component
public class HctPlugin extends BasePlugin {

    private final SchemeManager schemeManager;

    public HctPlugin(PluginWrapper wrapper, SchemeManager schemeManager) {
        super(wrapper);
        this.schemeManager = schemeManager;
    }

    @Override
    public void start() {
        // 插件启动时注册自定义模型
        schemeManager.register(Hct.class);
        System.out.println("火车头插件启动了!");
    }

    @Override
    public void stop() {
        // 插件停用时取消注册自定义模型
        Scheme hctScheme = schemeManager.get(Hct.class);
        schemeManager.unregister(hctScheme);
        System.out.println("火车头插件被停止!");
    }
}
