package com.amc.poster.util;

import com.amc.javafx.util.ViewUtil;
import com.amc.poster.constants.PosterConstant;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ElementApi {

	public static void showImageView(BufferedImage buffImg, String path) {
		// 创建新窗口
		int width = buffImg.getWidth();
		int height = buffImg.getHeight();
		Stage stage = new Stage();
		stage.setTitle("快照");
		// 创建窗口内容
		AnchorPane root = new AnchorPane();
		ImageView imageView = ViewUtil.getImageView(0, 0, width, height);
		Image image = ViewUtil.getImage(buffImg);
		imageView.setImage(image);
		root.getChildren().add(imageView);
		Scene scene = new Scene(root);
		stage.setScene(scene);
		dragEventMonitor(root, stage, buffImg, imageView, path);
	}

	private static HBox box = null;
	private static Paint boxColor = null;
	private static Label label = null;
	private static double startX, startY, endX, endY;
	private static ArrayList<Node> nodes = null;
	private static ArrayList<Rectangle> rectangles = null;

	private static void dragEventMonitor(AnchorPane an, Stage stage, BufferedImage buffImg, ImageView imageView, String path) {
		nodes = new ArrayList<>();
		rectangles = new ArrayList<>();
		nodes.add(imageView);
		boxColor = Color.RED;

		// 按下鼠标的效果
    	an.setOnMousePressed((event) -> {
    		// 清空之前的截图区域
			ObservableList<Node> children = an.getChildren();
			children.clear();
			children.addAll(nodes);
			// 设置截图区域显示效果
			box = new HBox();
			box.setBackground(null);
			box.setBorder(new Border(new BorderStroke(boxColor, BorderStrokeStyle.SOLID, null, new BorderWidths(1))));
			// 设置截图提示显示效果
			label = new Label();
			label.setAlignment(Pos.CENTER);
			label.setPrefSize(200, 20);
			label.setTextFill(Color.WHITE);
			label.setStyle("-fx-background-color:black;");
			AnchorPane.setLeftAnchor(label, (stage.getWidth() / 2 - label.getPrefWidth() / 2));
			children.add(label);
			// 设置起点位置
			startX = event.getSceneX();
			startY = event.getSceneY();
			AnchorPane.setLeftAnchor(box, startX);
			AnchorPane.setTopAnchor(box, startY);
			children.add(box);
    	});

    	an.setOnDragDetected((event) -> {
    		// 开始拖动记录
    		an.startFullDrag();
    	});

    	an.setOnMouseDragOver((event) -> {
    		// 拖拽鼠标时的位置坐标
			endX = event.getSceneX();
			endY = event.getSceneY();
			// 设置截图区域的尺寸
			double width = Math.abs(endX - startX);
			double height = Math.abs(endY - startY);
			box.setPrefSize(width, height);
			// 计算截图区域的起点
			if((endX - startX) < 0) {
				AnchorPane.setLeftAnchor(box, endX);
			} else {
				AnchorPane.setLeftAnchor(box, startX);
			}
			if((endY - startY) < 0) {
				AnchorPane.setTopAnchor(box, endY);
			} else {
				AnchorPane.setTopAnchor(box, startY);
			}
			// 更新提示面板
			String buf = "x:" + (int) box.getLayoutX() + " " +
					"y:" + (int) box.getLayoutY() + " " +
					"w:" + (int) width + " " +
					"h:" + (int) height;
			label.setText(buf);
    	});

    	an.setOnMouseDragExited((event) -> {
			// 拖拽结束时的效果
			int X = (int) box.getLayoutX();
			int Y = (int) box.getLayoutY();
			int Width = (int) box.getWidth();
			int Height = (int) box.getHeight();

			int btnWidth = 80, btnHeight = 30, btnX = X + Width - btnWidth, btnY = Y + Height;
			if (btnY + btnHeight > an.getHeight()) btnY -= (Height + btnHeight);
			String btnText = rectangles.isEmpty() ? "视图区域" : "点击区域";
			Button btn = ViewUtil.getButton(btnText, btnX, btnY, btnWidth, btnHeight);
			an.getChildren().addAll(btn);

			btn.setOnAction((event2) -> {
				Rectangle rect = new Rectangle(X, Y, Width, Height);
				rectangles.add(rect);
				// 保留box元素
				box.getChildren().clear();
				nodes.add(box);
				boxColor = Color.GREEN;
				// 清空之前的截图区域
				ObservableList<Node> children = an.getChildren();
				children.clear();
				children.addAll(nodes);
			});
    	});

		stage.show();

		stage.setOnHidden((event -> {
			if (rectangles.isEmpty()) {
				return;
			}
			try {
				Rectangle imgRect = rectangles.get(0);
				String fileName = PointUtil.getString(rectangles);
				// 开始截图
				BufferedImage areaImage = buffImg.getSubimage(imgRect.x, imgRect.y, imgRect.width, imgRect.height);
				WritableImage fxImage = SwingFXUtils.toFXImage(areaImage, null);
				// 复制图片
				Clipboard cbClipboard = Clipboard.getSystemClipboard();
				ClipboardContent clipboardContent = new ClipboardContent();
				clipboardContent.putImage(fxImage);
				cbClipboard.setContent(clipboardContent);
				// 保存图片
				String picName = fileName + ".png";
				String filePath = path + File.separator + picName;
				File file = new File(filePath);
				ImageIO.write(areaImage, "png", file);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}));
	}

	public static String showChoiceDialog(List<String> option) {
		ChoiceDialog<String> dialog = new ChoiceDialog<>(option.get(0), option);
		dialog.setTitle("窗口标题选择框");
		dialog.setHeaderText(null);
		dialog.setContentText("请选择:");
		Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(ElementApi.class.getResourceAsStream(PosterConstant.ICON_PATH)));
		Optional<String> result = dialog.showAndWait();
		return result.orElse(null);
	}

	public static File showDirectoryChooser(String pathPrefix) {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("图片存储文件夹选择框");
		chooser.setInitialDirectory(new File(pathPrefix));
		return chooser.showDialog(new Stage());
	}

}
