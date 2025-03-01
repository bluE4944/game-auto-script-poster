package com.amc.javafx.core;

import com.amc.javafx.model.WindowInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@ComponentScan("com.amc.javafx.plugin")
@Import({PluginManager.class, ViewClassParser.class, ViewFactory.class})
public class FxFrameConfiguration {

    @Bean
    public WindowInfo windowInfo() {
        return new WindowInfo();
    }
}
