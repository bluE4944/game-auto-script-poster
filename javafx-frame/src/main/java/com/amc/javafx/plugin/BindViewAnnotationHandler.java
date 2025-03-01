package com.amc.javafx.plugin;

import com.amc.javafx.annotations.BindView;
import com.amc.javafx.core.ViewFactory;
import com.amc.javafx.interfaces.FieldParser;
import com.amc.javafx.interfaces.ViewPostProcessor;
import com.amc.javafx.model.BindViewData;
import com.amc.javafx.util.ViewUtil;
import com.amc.javafx.util.spring.ProxyUtil;
import com.amc.util.reflect.ReflectUtil;
import com.amc.util.reflect.core.ClassUtil;
import com.amc.util.reflect.core.FieldUtil;
import com.amc.util.reflect.core.TypeParseUtil;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Order(Integer.MIN_VALUE + 1)
public class BindViewAnnotationHandler implements FieldParser, ViewPostProcessor {

    private final List<BindViewData> bindViewDataList = new ArrayList<>();
    private final List<ModelAndViewUpdater<Node>> modelAndViewUpdaters = new ArrayList() {{
        add(new LabeledUpdater());
        add(new SliderUpdater());
        add(new ChoiceBoxUpdater());
        add(new ImageViewUpdater());
        add(new SelectButtonUpdater());
        add(new TextInputControlUpdater());
    }};

    @Override
    public void resolve(Class<?> clazz, Field field, String beanName) {
        BindView bindViewAnnotation = field.getAnnotation(BindView.class);
        if (bindViewAnnotation != null && !ClassUtil.isSimpleType(field.getType())) {
            BindViewData bindViewData = new BindViewData();
            bindViewData.setBeanName(beanName);
            bindViewData.setBeanField(field);
            bindViewDataList.add(bindViewData);
        }
    }

    @Override
    public void postProcessAfterCreate(ViewFactory viewFactory) {
        Map<String, Node> modelAndView = new HashMap<>();
        Map<Node, List<ModelMethod>> viewAndModel = new HashMap<>();

        bindViewDataList.forEach(bindViewData -> {
            String beanName = bindViewData.getBeanName();
            Field beanField = bindViewData.getBeanField();

            // 创建模型
            Class<?> modelType = beanField.getType();
            Object target = ReflectUtil.newInstance(modelType);

            // 二次解析 + 填充数据
            for (Field modelField : FieldUtil.getFields(modelType)) {
                String viewName = modelField.getName();
                Object view = viewFactory.getView(viewName);
                if (view instanceof Node) {
                    try {
                        PropertyDescriptor pd = new PropertyDescriptor(viewName, modelType);
                        Method setMethod = pd.getWriteMethod();
                        String setMethodName = setMethod.getName();
                        Class<?> argType = setMethod.getParameters()[0].getType();
                        Node node = (Node) view;

                        modelAndView.put(setMethodName, node);
                        List<ModelMethod> modelMethods = viewAndModel.computeIfAbsent(node, k -> new ArrayList<>());
                        modelMethods.add(new ModelMethod(target, setMethod, argType));

                        Object viewValue = getViewValue(node);
                        if (viewValue != null) {
                            Object argVal = TypeParseUtil.parse(viewValue, argType);
                            ReflectUtil.invokeMethod(target, setMethod, argVal);
                        }
                    } catch (Exception e) {
                        System.err.println("err: " + e.getMessage());
                    }
                }
            }

            // 模型 -> 视图
            Object proxy = ProxyUtil.getCglibProxy(target, (mi) -> {
                String methodName = mi.getMethod().getName();
                Node view = modelAndView.get(methodName);
                if (view != null) {
                    // 改变对应视图
                    Object newVal = mi.getArguments()[0];
                    updateView(view, newVal);
                    // 改变其他模型的属性
                    List<ModelMethod> modelMethods = viewAndModel.get(view);
                    if (modelMethods != null) {
                        setModelField(modelMethods, newVal);
                    }
                }
                return mi.proceed();
            });

            // 模型赋值
            Object bean = viewFactory.getBean(beanName);
            ReflectUtil.setFieldValue(bean, beanField, proxy);
        });

        // 视图 -> 模型
        viewAndModel.forEach((view, modelInfoList) -> {
            for (ModelAndViewUpdater<Node> updater : modelAndViewUpdaters) {
                if (updater.match(view)) {
                    updater.addListener(view, modelInfoList);
                    break;
                }
            }
        });
    }

    private Object getViewValue(Node view) {
        for (ModelAndViewUpdater<Node> updater : modelAndViewUpdaters) {
            if (updater.match(view)) {
                return updater.getViewValue(view);
            }
        }
        return null;
    }

    private void updateView(Node view, Object value) {
        Platform.runLater(() -> {
            for (ModelAndViewUpdater<Node> updater : modelAndViewUpdaters) {
                if (updater.match(view)) {
                    updater.updateView(view, value);
                    break;
                }
            }
        });
    }

    private static void setModelField(List<ModelMethod> modelMethods, Object newValue) {
        modelMethods.forEach(modelMethod -> {
            try {
                Object argVal = TypeParseUtil.parse(newValue, modelMethod.argType);
                ReflectUtil.invokeMethod(modelMethod.target, modelMethod.setMethod, argVal);
            } catch (Exception e) {
                System.err.println("err: " + e.getMessage());
            }
        });
    }

    @Data
    @AllArgsConstructor
    private class ModelMethod {
        private Object target;
        private Method setMethod;
        private Class<?> argType;
    }

    private interface ModelAndViewUpdater<T> {
        Boolean match(Node view);
        Object getViewValue(T view);
        void updateView(T view, Object modelValue);
        void addListener(T view, List<ModelMethod> modelMethods);
    }

    private class LabeledUpdater implements ModelAndViewUpdater<Labeled> {
        @Override
        public Boolean match(Node view) {
            return view instanceof Labeled;
        }
        @Override
        public Object getViewValue(Labeled view) {
            return view.getText();
        }
        @Override
        public void updateView(Labeled view, Object modelValue) {
            view.setText(modelValue.toString());
        }
        @Override
        public void addListener(Labeled view, List<ModelMethod> modelMethods) {

        }
    }

    private class TextInputControlUpdater implements ModelAndViewUpdater<TextInputControl> {
        @Override
        public Boolean match(Node view) {
            return view instanceof TextInputControl;
        }
        @Override
        public Object getViewValue(TextInputControl view) {
            return view.getText();
        }
        @Override
        public void updateView(TextInputControl view, Object modelValue) {
            String text = modelValue.toString();
            view.setText(text);
            view.positionCaret(text.length());
        }
        @Override
        public void addListener(TextInputControl view, List<ModelMethod> modelMethods) {
            view.textProperty().addListener((observableValue, oldValue, newValue) -> {
                setModelField(modelMethods, newValue);
            });
        }
    }

    private class SelectButtonUpdater implements ModelAndViewUpdater<ButtonBase> {
        @Override
        public Boolean match(Node view) {
            return view instanceof Toggle || view instanceof CheckBox;
        }
        @Override
        public Object getViewValue(ButtonBase view) {
            if (view instanceof Toggle) {
                return ((Toggle) view).isSelected();
            }
            return ((CheckBox) view).isSelected();
        }
        @Override
        public void updateView(ButtonBase view, Object modelValue) {
            Boolean val = (Boolean) modelValue;
            if (view instanceof Toggle) {
                ((Toggle) view).setSelected(val);
                return;
            }
            ((CheckBox) view).setSelected(val);
        }
        @Override
        public void addListener(ButtonBase view, List<ModelMethod> modelMethods) {
            if (view instanceof Toggle) {
                ((Toggle) view).selectedProperty().addListener((observable, oldValue, newValue) -> {
                    setModelField(modelMethods, newValue);
                });
                return;
            }
            ((CheckBox) view).selectedProperty().addListener((observable, oldValue, newValue) -> {
                setModelField(modelMethods, newValue);
            });
        }
    }

    private class SliderUpdater implements ModelAndViewUpdater<Slider> {
        @Override
        public Boolean match(Node view) {
            return view instanceof Slider;
        }
        @Override
        public Object getViewValue(Slider view) {
            return view.getValue();
        }
        @Override
        public void updateView(Slider view, Object modelValue) {
            view.setValue(Double.parseDouble(modelValue.toString()));
        }
        @Override
        public void addListener(Slider view, List<ModelMethod> modelMethods) {
            view.valueProperty().addListener((observable, oldValue, newValue) -> {
                setModelField(modelMethods, newValue);
            });
        }
    }

    private class ChoiceBoxUpdater implements ModelAndViewUpdater<ChoiceBox> {
        @Override
        public Boolean match(Node view) {
            return view instanceof ChoiceBox;
        }
        @Override
        public Object getViewValue(ChoiceBox view) {
            return view.getValue();
        }
        @Override
        public void updateView(ChoiceBox view, Object modelValue) {
            view.setValue(modelValue);
        }
        @Override
        public void addListener(ChoiceBox view, List<ModelMethod> modelMethods) {
            view.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                setModelField(modelMethods, newValue);
            });
        }
    }

    private class ImageViewUpdater implements ModelAndViewUpdater<ImageView> {
        @Override
        public Boolean match(Node view) {
            return view instanceof ImageView;
        }
        @Override
        public Object getViewValue(ImageView view) {
            return null;
        }
        @Override
        public void updateView(ImageView view, Object modelValue) {
            if (modelValue instanceof Image) {
                view.setImage((Image) modelValue);
            }
            else if (modelValue instanceof String) {
                Image image = ViewUtil.getImage((String) modelValue);
                view.setImage(image);
            }
            else if (modelValue instanceof BufferedImage) {
                Image image = ViewUtil.getImage((BufferedImage) modelValue);
                view.setImage(image);
            }
        }
        @Override
        public void addListener(ImageView view, List<ModelMethod> modelMethods) {

        }
    }

}
