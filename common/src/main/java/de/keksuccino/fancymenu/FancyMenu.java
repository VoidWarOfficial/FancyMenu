package de.keksuccino.fancymenu;

import java.io.File;
import de.keksuccino.fancymenu.util.event.acara.EventHandler;
import de.keksuccino.fancymenu.platform.Services;
import de.keksuccino.fancymenu.util.file.FileUtils;
import de.keksuccino.fancymenu.util.file.GameDirectoryUtils;
import de.keksuccino.fancymenu.util.file.type.types.FileTypes;
import de.keksuccino.fancymenu.util.rendering.text.color.colors.TextColorFormatters;
import de.keksuccino.fancymenu.util.rendering.ui.cursor.CursorHandler;
import de.keksuccino.fancymenu.util.rendering.ui.theme.themes.UIColorThemes;
import de.keksuccino.fancymenu.util.window.WindowHandler;
import de.keksuccino.fancymenu.customization.customlocals.CustomLocalsHandler;
import de.keksuccino.fancymenu.customization.server.ServerCache;
import net.minecraft.SharedConstants;
import de.keksuccino.fancymenu.customization.ScreenCustomization;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class FancyMenu {

	//TODO Markdown: Add more bullet list levels (make better handling for infinite levels)

	//TODO ALLE UNGENUTZTEN FileTypes entfernen (ungenutzte video types)

	//TODO Layout Listener System
	// - Auf Layout Ebene (rechtsklick editor back -> Listeners)
	// - Jeder Listener hat eine ExecutableBlock instanz, die er ausführen kann
	// - ExecutableBlock value placeholders nutzen, um values von Listeners an actions zu übergeben
	// - Listeners:
	//   - First Menu Open (wenn menu type das erste Mal in einer game session geöffnet wird) (checkt auf menu type von layout oder alle, wenn universal)
	//   - Open Menu (checkt auf menu type von layout oder alle, wenn universal)
	//   - Close Menu (checkt auf menu type von layout oder alle, wenn universal)
	//   - Init Or Re-Init Menu (InitOrResize)
	//   - Element Loaded (mit element identifier) -> dann bei ausführen von listener checken, ob erstelltes element == identifier
	//   - Key Typed (mit key name) -> bei ausführen check ob pressed key == given key
	//   - Mouse Clicked (mit mouseX, mouseY)
	//   - Mouse Moved (mit mouseX, mouseY)
	//   - Mouse Scrolled (mit scrollDelta)

	//TODO Button: Nine-Slice Background Mode (toggle on/off)
	// - Option, um Randbreite zu definieren, wenn möglich

	//TODO add widget label scale option (per CustomizableWidget adden)

	//TODO Add Edit GUI to more Action values, if needed

	//TODO "Split Text" placeholder (regex support) (könnte performance killen)

	//TODO Markdown support for tooltips

	//TODO FIXEN: Splash Elemente werden bei resize reloaded (isNewMenu in builder fixen??)

	//TODO "Key Pressed" Loading Requirement
	// - Modes: ONCE_PER_KEY_PRESS (nur einen tick pro key press auf true), CONTINUOUS (hält bei key press dauerhaft auf true)
	// - Setzt "pressed" boolean bei onKeyPress auf true und bei onKeyRelease auf false (für modes)

	//TODO Item Element, das per item meta (wie in give command) customized werden kann

	//TODO Möglichkeit adden, Custom GUIs zu kopieren (bei kopieren öffnet sich input screen, um neuen identifier einzugeben)




	private static final Logger LOGGER = LogManager.getLogger();

	public static final String VERSION = "3.0.4";
	public static final String MOD_LOADER = Services.PLATFORM.getPlatformName();
	public static final String MOD_ID = "fancymenu";

	public static final File MOD_DIR = createDirectory(new File(GameDirectoryUtils.getGameDirectory(), "/config/fancymenu"));
	public static final File INSTANCE_DATA_DIR = createDirectory(new File(GameDirectoryUtils.getGameDirectory(), "/fancymenu_data"));
	public static final File TEMP_DATA_DIR = createDirectory(new File(INSTANCE_DATA_DIR, "/.fancymenu_temp"));
	public static final File CACHE_DIR = createDirectory(new File(INSTANCE_DATA_DIR, "/cached_data"));

	private static Options options;

	public static void init() {

		if (Services.PLATFORM.isOnClient()) {
			LOGGER.info("[FANCYMENU] Loading v" + VERSION + " in client-side mode on " + MOD_LOADER.toUpperCase() + "!");
		} else {
			LOGGER.info("[FANCYMENU] Loading v" + VERSION + " in server-side mode on " + MOD_LOADER.toUpperCase() + "!");
		}

		FileTypes.registerAll();

		if (Services.PLATFORM.isOnClient()) {

			UIColorThemes.registerAll();

			TextColorFormatters.registerAll();

			ScreenCustomization.init();

			//TODO remove debug
			EventHandler.INSTANCE.registerListenersOf(new Test());

		}

		Compat.printInfoLog();

	}

	public static void lateClientInit() {

		LOGGER.info("[FANCYMENU] Starting late client initialization phase..");

		WindowHandler.updateCustomWindowIcon();

		WindowHandler.handleForceFullscreen();

		CursorHandler.init();

		CustomLocalsHandler.loadLocalizations();

		ServerCache.init();

	}

	public static Options getOptions() {
		if (options == null) {
			reloadOptions();
		}
		return options;
	}

	public static void reloadOptions() {
		options = new Options();
	}

	public static String getMinecraftVersion() {
		return SharedConstants.getCurrentVersion().getName();
	}

	private static File createDirectory(@NotNull File directory) {
		return FileUtils.createDirectory(directory);
	}

}
