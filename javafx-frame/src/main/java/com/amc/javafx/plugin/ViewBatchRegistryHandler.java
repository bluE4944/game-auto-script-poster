package com.amc.javafx.plugin;

import com.amc.javafx.constants.FxConstant;
import com.amc.javafx.core.ViewFactory;
import com.amc.javafx.interfaces.ClassParser;
import com.amc.javafx.interfaces.ViewBatchRegistry;
import com.amc.javafx.interfaces.ViewRegistry;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Order(Integer.MIN_VALUE + 1)
public class ViewBatchRegistryHandler implements ClassParser, ViewRegistry {

    private final List<String> beanNames = new ArrayList<>();

    @Override
    public void resolve(Class<?> clazz, String beanName) {
        if (ViewBatchRegistry.class.isAssignableFrom(clazz)) {
            beanNames.add(beanName);
        }
    }

    @Override
    public void register(ViewFactory viewFactory) {
        if (beanNames.size() > 0) {
            Pane rootPane = viewFactory.getView(FxConstant.ROOT_PANE, Pane.class);
            beanNames.forEach(beanName -> {
                ViewBatchRegistry bean = viewFactory.getBean(beanName, ViewBatchRegistry.class);
                Map<String, Node> views = bean.getViews(rootPane);
                viewFactory.registerViews(views);
            });
        }
    }

}
