package de.keksuccino.fancymenu.customization.overlay;

import de.keksuccino.fancymenu.FancyMenu;
import de.keksuccino.fancymenu.events.screen.ScreenKeyPressedEvent;
import de.keksuccino.fancymenu.util.event.acara.EventHandler;
import de.keksuccino.fancymenu.util.event.acara.EventPriority;
import de.keksuccino.fancymenu.util.event.acara.EventListener;
import de.keksuccino.fancymenu.events.screen.InitOrResizeScreenCompletedEvent;
import de.keksuccino.fancymenu.events.screen.RenderScreenEvent;
import de.keksuccino.fancymenu.customization.ScreenCustomization;
import de.keksuccino.fancymenu.util.rendering.ui.menubar.v2.MenuBar;
import de.keksuccino.fancymenu.util.rendering.ui.screen.CustomizableScreen;
import net.minecraft.client.gui.screens.Screen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class CustomizationOverlay {

	private static final Logger LOGGER = LogManager.getLogger();

	private static MenuBar overlayMenuBar;
	private static DebugOverlay debugOverlay;
	
	public static void init() {
		EventHandler.INSTANCE.registerListenersOf(new CustomizationOverlay());
	}

	public static void rebuildOverlay() {
		overlayMenuBar = CustomizationOverlayUI.buildMenuBar((overlayMenuBar == null) || overlayMenuBar.isExpanded());
		rebuildDebugOverlay();
	}

	public static void rebuildDebugOverlay() {
		if (debugOverlay != null) debugOverlay.resetOverlay();
		debugOverlay = CustomizationOverlayUI.buildDebugOverlay(overlayMenuBar);
	}

	@Nullable
	public static MenuBar getCurrentMenuBarInstance() {
		return overlayMenuBar;
	}

	@Nullable
	public static DebugOverlay getCurrentDebugOverlayInstance() {
		return debugOverlay;
	}

	@EventListener(priority = -1000)
	public void onInitScreenPost(InitOrResizeScreenCompletedEvent e) {
		rebuildOverlay();
		e.getWidgets().add(0, overlayMenuBar);
		if (e.getScreen() instanceof CustomizableScreen c) c.removeOnInitChildrenFancyMenu().add(overlayMenuBar);
		e.getWidgets().add(1, debugOverlay);
		if (e.getScreen() instanceof CustomizableScreen c) c.removeOnInitChildrenFancyMenu().add(debugOverlay);
	}

	@EventListener(priority = EventPriority.LOW)
	public void onRenderPost(RenderScreenEvent.Post e) {
		if (!ScreenCustomization.isScreenBlacklisted(e.getScreen().getClass().getName()) && (overlayMenuBar != null) && (debugOverlay != null)) {
			if (FancyMenu.getOptions().showDebugOverlay.getValue()) {
				debugOverlay.render(e.getGraphics(), e.getMouseX(), e.getMouseY(), e.getPartial());
			}
			if (FancyMenu.getOptions().showCustomizationOverlay.getValue()) {
				overlayMenuBar.render(e.getGraphics(), e.getMouseX(), e.getMouseY(), e.getPartial());
			}
		}
	}

	@EventListener
	public void onScreenKeyPressed(ScreenKeyPressedEvent e) {

		if (!ScreenCustomization.isScreenBlacklisted(e.getScreen().getClass().getName())) {

			String keyName = e.getKeyName();

			//Toggle Menu Bar
			if (keyName.equals("c") && Screen.hasControlDown() && Screen.hasAltDown()) {
				FancyMenu.getOptions().showCustomizationOverlay.setValue(!FancyMenu.getOptions().showCustomizationOverlay.getValue());
				ScreenCustomization.reInitCurrentScreen();
			}

			//Toggle Debug Overlay
			if (keyName.equals("d") && Screen.hasControlDown() && Screen.hasAltDown()) {
				FancyMenu.getOptions().showDebugOverlay.setValue(!FancyMenu.getOptions().showDebugOverlay.getValue());
				ScreenCustomization.reInitCurrentScreen();
			}

			//Reload FancyMenu
			if (keyName.equals("r") && Screen.hasControlDown() && Screen.hasAltDown()) {
				ScreenCustomization.reloadFancyMenu();
			}

		}

	}

}