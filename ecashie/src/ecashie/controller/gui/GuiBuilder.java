package ecashie.controller.gui;

import java.io.IOException;
import java.net.URL;

import ecashie.controller.exception.UnexpectedBehaviourException;
import ecashie.controller.settings.UserData;
import ecashie.main.ExitApp;
import ecashie.main.MainApp;
import ecashie.view.root.RootLayout;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GuiBuilder
{
	public static Stage PrimaryStage;
	public static RootLayout currentRootScene;

	public static void initPrimaryStage(Stage primaryStage)
	{
		PrimaryStage = primaryStage;

		PrimaryStage.setTitle("eCashie");

		PrimaryStage.getIcons()
				.add(new Image(MainApp.class.getResourceAsStream("/ecashie/resources/img/logo_32x32.png")));
		PrimaryStage.getIcons()
				.add(new Image(MainApp.class.getResourceAsStream("/ecashie/resources/img/logo_48x48.png")));

		PrimaryStage.setOnCloseRequest((WindowEvent we) -> {
			ExitApp.exit();
		});
	}

	public static void changeScene(boolean wait)
	{
		try
		{
			determineRootScene();

			changeRootScene();

			setContentRootScene();
		}
		catch (IOException e)
		{
			new UnexpectedBehaviourException();
		}

		if (!wait)
		{
			PrimaryStage.show();
		}
	}

	private static void determineRootScene()
	{
		switch (Navigation.Next)
		{
		case "StartScene":
			currentRootScene = RootLayout.nonFullScreen;
			break;
		default:
			currentRootScene = RootLayout.fullScreen;
			break;
		}
	}

	private static void changeRootScene() throws IOException
	{
		if (Navigation.Next.equals("StartScene") || Navigation.getBefore().equals("StartScene"))
		{
			PrimaryStage.hide();

			initRootLayout();

			switchRootScene();

			setContentToWindowSize();

			setWindowBehaviour();
		}
	}

	public static void initRootLayout() throws IOException
	{
		URL urlRootLayout = MainApp.class.getResource(currentRootScene.getPath());

		FXMLLoader fxmlLoaderRoot = new FXMLLoader();
		fxmlLoaderRoot.setLocation(urlRootLayout);

		BorderPane rootLayout = (BorderPane) fxmlLoaderRoot.load();

		currentRootScene.setBorderPane(rootLayout);
	}

	private static void switchRootScene()
	{
		Scene scene = new Scene(currentRootScene.getBorderPane());

		if (Navigation.Current.equals("MainScene"))
		{
			// Prevents bug in JavaFX:
			// If the css file is only referenced in fxml the context menu isn't affected by
			// the defined style
			scene.getStylesheets().add(MainApp.class.getResource("/ecashie/resources/css/main.css").toExternalForm());
		}

		PrimaryStage.setScene(scene);
	}

	private static void setContentToWindowSize()
	{
		if (currentRootScene.equals(RootLayout.nonFullScreen))
		{
			PrimaryStage.sizeToScene();
		}
	}

	private static void setWindowBehaviour()
	{
		PrimaryStage.centerOnScreen();
		PrimaryStage.setMaximized(currentRootScene.getMaximized());
		PrimaryStage.setResizable(currentRootScene.getResizable());
	}

	private static void setContentRootScene() throws IOException
	{
		AnchorPane contentPane = createSceneContentPane(Navigation.Next);

		currentRootScene.getBorderPane().setCenter(contentPane);
	}

	private static AnchorPane createSceneContentPane(String sceneName) throws IOException
	{
		String scenePath = "";

		switch (Navigation.Current)
		{
		case "MainScene":
			if (sceneName.contains("Settings"))
			{
				scenePath = "/ecashie/view/settings/" + sceneName + ".fxml";
			}
			else
			{
				scenePath = "/ecashie/view/main/" + sceneName + ".fxml";
			}
			break;
		case "StartScene":
			scenePath = "/ecashie/view/start/" + sceneName + ".fxml";
			break;
		}

		URL urlScene = MainApp.class.getResource(scenePath);

		FXMLLoader fxmlLoader = new FXMLLoader();
		fxmlLoader.setLocation(urlScene);
		fxmlLoader.setResources(MainApp.ResourceBundle);

		AnchorPane sceneContentPane = (AnchorPane) fxmlLoader.load();

		registerOnActionEventInMainSceneController(sceneName, fxmlLoader, sceneContentPane);

		return sceneContentPane;
	}

	private static void registerOnActionEventInMainSceneController(String sceneName, FXMLLoader fxmlLoader,
			AnchorPane sceneContentPane)
	{
		// if (sceneName.equals("MainScene"))
		// {
		// MainSceneController mainSceneController = fxmlLoader.getController();
		//
		// sceneContentPane.addEventFilter(MouseEvent.MOUSE_PRESSED, new
		// EventHandler<MouseEvent>()
		// {
		// @Override
		// public void handle(MouseEvent mouseEvent)
		// {
		// if (mainSceneController != null)
		// {
		// mainSceneController.onActionScene(mouseEvent);
		// }
		// }
		// });
		// }
	}

	public static void embedPaneIntoScene(AnchorPane anchorPane, String scene)
	{
		try
		{
			AnchorPane scenePane = createSceneContentPane(scene);

			anchorPane.getChildren().clear();
			anchorPane.getChildren().add(scenePane);
		}
		catch (IOException e)
		{
			new UnexpectedBehaviourException();
		}
	}

	public static void updateWindowTitle()
	{
		if (PrimaryStage != null)
		{
			Platform.runLater(new Runnable()
			{
				@Override
				public void run()
				{
					PrimaryStage.setTitle("eCashie - " + UserData.getCashJournalName());
				}
			});
		}
	}
}
