package de.keksuccino.fancymenu.util.rendering.ui.popup;

import java.awt.Color;
import java.util.function.Consumer;

import de.keksuccino.fancymenu.util.rendering.ui.UIBase;
import de.keksuccino.konkrete.gui.content.AdvancedButton;
import de.keksuccino.konkrete.gui.screens.popup.TextInputPopup;
import de.keksuccino.konkrete.input.CharacterFilter;

//TODO remove this class
@Deprecated
public class FMTextInputPopup extends TextInputPopup {

	public FMTextInputPopup(Color color, String title, CharacterFilter filter, int backgroundAlpha, Consumer<String> callback) {
		super(color, title, filter, backgroundAlpha, callback);
	}
	
	public FMTextInputPopup(Color color, String title, CharacterFilter filter, int backgroundAlpha) {
		super(color, title, filter, backgroundAlpha);
	}
	
	@Override
	protected void colorizePopupButton(AdvancedButton b) {
		UIBase.applyDefaultWidgetSkinTo(b);
	}

}
