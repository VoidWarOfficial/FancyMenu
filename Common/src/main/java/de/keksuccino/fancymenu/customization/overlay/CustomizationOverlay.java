package de.keksuccino.fancymenu.customization.overlay;

import de.keksuccino.fancymenu.event.acara.EventHandler;
import de.keksuccino.fancymenu.event.acara.EventPriority;
import de.keksuccino.fancymenu.event.acara.EventListener;
import de.keksuccino.fancymenu.event.events.screen.RenderScreenEvent;
import de.keksuccino.fancymenu.customization.ScreenCustomization;

public class CustomizationOverlay {
	
	public static void init() {
		EventHandler.INSTANCE.registerListenersOf(new CustomizationOverlay());
		CustomizationOverlayUI.init();
	}

	@EventListener(priority = EventPriority.LOW)
	public void onRenderPost(RenderScreenEvent.Post e) {
		if (!ScreenCustomization.isScreenBlacklisted(e.getScreen().getClass().getName())) {
			CustomizationOverlayUI.render(e.getPoseStack(), e.getScreen());
		}
	}
	
	public static void updateUI() {
		CustomizationOverlayUI.updateUI();
	}

}