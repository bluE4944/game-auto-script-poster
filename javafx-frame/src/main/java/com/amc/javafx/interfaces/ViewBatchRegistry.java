package com.amc.javafx.interfaces;

import javafx.scene.Node;
import javafx.scene.layout.Pane;

import java.util.Map;

/**
 * View的批量注册器
 */
public interface ViewBatchRegistry {

    Map<String, Node> getViews(Pane rootPane);

}