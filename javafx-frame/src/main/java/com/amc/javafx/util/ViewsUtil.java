package com.amc.javafx.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.MediaView;
import javafx.scene.text.Font;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 视图原生工具包
 */
public class ViewsUtil {

    private String node;
    private Pane parent;
    private final Map<String, Node> nodeMap = new LinkedHashMap<>();
    private Map<String, ToggleGroup> toggleGroupMap;

    public static ViewsUtil parent(Pane parentPane) {
        ViewsUtil util = new ViewsUtil();
        util.parent = parentPane;
        return util;
    }

    public ViewsUtil parent(String parentId) {
        parent = getNode(parentId, Pane.class);
        return this;
    }

    public PaneApi anchorPane(String id) {
        node = id;
        return new AbstractPaneApi(new AnchorPane(), this);
    }

    public PaneApi flowPane(String id) {
        node = id;
        return new AbstractPaneApi(new FlowPane(), this);
    }

    public PaneApi hBox(String id) {
        node = id;
        return new AbstractPaneApi(new HBox(), this);
    }

    public PaneApi vBox(String id) {
        node = id;
        return new AbstractPaneApi(new VBox(), this);
    }

    public PaneApi tabPane(String id) {
        node = id;
        TabPane tabpane = new TabPane();
        tabpane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        return new AbstractPaneApi(tabpane, this);
    }

    public ViewsUtil addTab(String title, String paneId) {
        TabPane tabPane = getNode(node, TabPane.class);
        Pane pane = getNode(paneId, Pane.class);
        Tab tab = new Tab(title);
        tab.setContent(pane);
        tabPane.getTabs().add(tab);
        return this;
    }

    public NodeApi label(String id) {
        node = id;
        return getNodeApi(new Label(), this);
    }

    public NodeApi textField(String id) {
        node = id;
        return getNodeApi(new TextField(), this);
    }

    public NodeApi numberField(String id) {
        node = id;
        TextField numberField = new TextField();
        numberField.setTextFormatter(new TextFormatter<Integer>(change -> {
            String text = change.getText();
            return ("".equals(text) || text.matches("[-\\d]+")) ? change : null;
        }));
        return getNodeApi(numberField, this);
    }

    public NodeApi passwordField(String id) {
        node = id;
        return getNodeApi(new PasswordField(), this);
    }

    public NodeApi textArea(String id) {
        node = id;
        return getNodeApi(new TextArea(), this);
    }

    public NodeApi button(String id) {
        node = id;
        return getNodeApi(new Button(), this);
    }

    public NodeApi radioButton(String id, String groupName) {
        node = id;
        RadioButton radioButton = new RadioButton();
        radioButton.setToggleGroup(getToggleGroup(groupName));
        return getNodeApi(radioButton, this);
    }

    public NodeApi checkBox(String id) {
        node = id;
        return getNodeApi(new CheckBox(), this);
    }

    public NodeApi slider(String id) {
        node = id;
        return getNodeApi(new Slider(), this);
    }

    public <T> NodeApi choiceBox(String id, Object... values) {
        node = id;
        ChoiceBox<Object> choiceBox = new ChoiceBox<>();
        choiceBox.getItems().addAll(values);
        return getNodeApi(choiceBox, this);
    }

    public NodeApi imageView(String id) {
        node = id;
        return getNodeApi(new ImageView(), this);
    }

    public NodeApi mediaView(String id) {
        node = id;
        return getNodeApi(new MediaView(), this);
    }

    public Map<String, Node> end() {
        return nodeMap;
    }

    public <T> T getNode(String id, Class<T> clazz) {
        return (T) nodeMap.get(id);
    }

    private ToggleGroup getToggleGroup(String groupName) {
        if (toggleGroupMap == null) {
            toggleGroupMap = new HashMap<>();
        }
        ToggleGroup toggleGroup = toggleGroupMap.get(groupName);
        if (toggleGroup == null) {
            toggleGroup = new ToggleGroup();
            toggleGroupMap.put(groupName, toggleGroup);
        }
        return toggleGroup;
    }

    private ViewsUtil() {
    }

    public interface NodeApi {
        NodeApi title(String text);
        NodeApi font(double num);
        NodeApi x(int num);
        NodeApi x(double num);
        NodeApi y(int num);
        NodeApi y(double num);
        NodeApi top(int num);
        NodeApi bottom(int num);
        NodeApi left(int num);
        NodeApi right(int num);
        NodeApi width(int num);
        NodeApi width(double num);
        NodeApi height(int num);
        NodeApi height(double num);
        NodeApi padding(double top, double right, double bottom, double left);
        NodeApi margin(double top, double right, double bottom, double left);
        ViewsUtil create();
    }

    public interface PaneApi extends NodeApi {
        PaneApi alignment(Pos value);
        PaneApi spacing(double num);
    }

    @RequiredArgsConstructor
    private class AbstractNodeApi implements NodeApi {
        public final String id;
        public final Node node;
        public final Pane parentPane;
        public final ViewsUtil viewsUtil;
        @Override
        public NodeApi title(String text) {
            if (node instanceof Labeled) {
                ((Labeled) node).setText(text);
            }
            else if (node instanceof TextInputControl) {
                ((TextInputControl) node).setText(text);
            }
            return this;
        }
        @Override
        public NodeApi font(double num) {
            if (node instanceof Labeled) {
                ((Labeled) node).setFont(new Font(num));
            }
            else if (node instanceof TextInputControl) {
                ((TextInputControl) node).setFont(new Font(num));
            }
            return this;
        }
        @Override
        public NodeApi x(int num) {
            node.setLayoutX(num);
            return this;
        }
        @Override
        public NodeApi x(double num) {
            double parentWidth = parentPane.getPrefWidth();
            if (parentWidth > 0) {
                double x = parentWidth * num / 100;
                this.x((int) x);
            }
            return this;
        }
        @Override
        public NodeApi y(int num) {
            node.setLayoutY(num);
            return this;
        }
        @Override
        public NodeApi y(double num) {
            double parentHeight = parentPane.getPrefHeight();
            if (parentHeight > 0) {
                double y = parentHeight * num / 100;
                this.y((int) y);
            }
            return this;
        }
        @Override
        public NodeApi top(int num) {
            node.setTranslateY(node.getTranslateY() - num);
            return this;
        }
        @Override
        public NodeApi bottom(int num) {
            node.setTranslateY(node.getTranslateY() + num);
            return this;
        }
        @Override
        public NodeApi left(int num) {
            node.setTranslateX(node.getTranslateX() - num);
            return this;
        }
        @Override
        public NodeApi right(int num) {
            node.setTranslateX(node.getTranslateX() + num);
            return this;
        }
        @Override
        public NodeApi width(int num) {
            if (node instanceof Region) {
                ((Region) node).setPrefWidth(num);
            }
            else if (node instanceof ImageView) {
                ((ImageView) node).setFitWidth(num);
            }
            else if (node instanceof MediaView) {
                ((MediaView) node).setFitWidth(num);
            }
            return this;
        }
        @Override
        public NodeApi width(double num) {
            double parentWidth = parentPane.getPrefWidth();
            if (parentWidth > 0) {
                double width = parentWidth * num / 100;
                this.width((int) width);
            }
            return this;
        }
        @Override
        public NodeApi height(int num) {
            if (node instanceof Region) {
                ((Region) node).setPrefHeight(num);
            }
            else if (node instanceof ImageView) {
                ((ImageView) node).setFitHeight(num);
            }
            else if (node instanceof MediaView) {
                ((MediaView) node).setFitHeight(num);
            }
            return this;
        }
        @Override
        public NodeApi height(double num) {
            double parentHeight = parentPane.getPrefHeight();
            if (parentHeight > 0) {
                double height = parentHeight * num / 100;
                this.height((int) height);
            }
            return this;
        }
        @Override
        public NodeApi padding(double top, double right, double bottom, double left) {
            if (node instanceof Region) {
                ((Region) node).setPadding(new Insets(top, right, bottom, left));
            }
            return this;
        }
        @Override
        public NodeApi margin(double top, double right, double bottom, double left) {
            if (node instanceof FlowPane) {
                FlowPane.setMargin(node, new Insets(top, right, bottom, left));
            }
            else if (node instanceof HBox) {
                VBox.setMargin(node, new Insets(top, right, bottom, left));
            }
            else if (node instanceof VBox) {
                VBox.setMargin(node, new Insets(top, right, bottom, left));
            }
            return this;
        }
        @Override
        public ViewsUtil create() {
            viewsUtil.nodeMap.put(id, node);
            parentPane.getChildren().add(node);
            return viewsUtil;
        }
    }

    private NodeApi getNodeApi(Node node, ViewsUtil viewsUtil) {
        return new AbstractNodeApi(viewsUtil.node, node, viewsUtil.parent, viewsUtil);
    }

    private class AbstractPaneApi extends AbstractNodeApi implements PaneApi {
        public AbstractPaneApi(Node node, ViewsUtil viewsUtil) {
            super(viewsUtil.node, node, viewsUtil.parent, viewsUtil);
        }
        @Override
        public PaneApi alignment(Pos value) {
            if (node instanceof FlowPane) {
                ((FlowPane) node).setAlignment(value);
            }
            else if (node instanceof HBox) {
                ((HBox) node).setAlignment(value);
            }
            else if (node instanceof VBox) {
                ((VBox) node).setAlignment(value);
            }
            return this;
        }
        @Override
        public PaneApi spacing(double num) {
            if (node instanceof FlowPane) {
                ((FlowPane) node).setHgap(num);
                ((FlowPane) node).setVgap(num);
            }
            else if (node instanceof HBox) {
                ((HBox) node).setSpacing(num);
            }
            else if (node instanceof VBox) {
                ((VBox) node).setSpacing(num);
            }
            return this;
        }
    }

}
