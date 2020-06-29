package de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator.content;

import java.awt.Color;
import java.io.File;
import java.util.function.Consumer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import de.keksuccino.core.filechooser.FileChooser;
import de.keksuccino.core.gui.content.AdvancedButton;
import de.keksuccino.core.gui.screens.popup.TextInputPopup;
import de.keksuccino.core.input.CharacterFilter;
import de.keksuccino.fancymenu.FancyMenu;
import de.keksuccino.fancymenu.localization.Locals;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;

public class ChooseFilePopup extends TextInputPopup {

	protected AdvancedButton chooseFileBtn;
	private String[] fileTypes;
	
	public ChooseFilePopup(Consumer<String> callback, String... fileTypes) {
		super(new Color(0, 0, 0, 0), Locals.localize("helper.creator.choosefile.enterorchoose"), null, 0, callback);
		this.fileTypes = fileTypes;
	}
	
	@Override
	protected void init(Color color, String title, CharacterFilter filter, Consumer<String> callback) {
		super.init(color, title, filter, callback);
		
		this.chooseFileBtn = new AdvancedButton(0, 0, 100, 20, Locals.localize("helper.creator.choosefile.choose"), true, (press) -> {
			if (FancyMenu.isNotHeadless()) {
				FileChooser.askForFile(new File("").getAbsoluteFile(), (call) -> {
					if (call != null) {
						String path = call.getAbsolutePath();
						File home = new File("");
						if (path.startsWith(home.getAbsolutePath())) {
							path = path.replace(home.getAbsolutePath(), "");
							if (path.startsWith("\\") || path.startsWith("/")) {
								path = path.substring(1);
							}
						}
						this.setText(path);
					}
				}, fileTypes);
			}
		});
		this.addButton(chooseFileBtn);
	}
	
	@Override
	public void render(MatrixStack matrix, int mouseX, int mouseY, Screen renderIn) {
		if (!this.isDisplayed()) {
			return;
		}
		RenderSystem.enableBlend();
		IngameGui.func_238467_a_(matrix, 0, 0, renderIn.field_230708_k_, renderIn.field_230709_l_ , new Color(0, 0, 0, 240).getRGB());
		RenderSystem.disableBlend();
		
		renderIn.func_238472_a_(matrix, Minecraft.getInstance().fontRenderer, new StringTextComponent(title), renderIn.field_230708_k_ / 2, (renderIn.field_230709_l_  / 2) - 40, Color.WHITE.getRGB());
		
		this.textField.setX((renderIn.field_230708_k_ / 2) - (this.textField.getWidth() / 2));
		this.textField.setY((renderIn.field_230709_l_  / 2) - (this.textField.getHeight() / 2));
		this.textField.renderButton(matrix, mouseX, mouseY, Minecraft.getInstance().getRenderPartialTicks());
		
		this.doneButton.setX((renderIn.field_230708_k_ / 2) - (this.doneButton.getWidth() / 2));
		this.doneButton.setY(((renderIn.field_230709_l_  / 2) + 100) - this.doneButton.getHeight() - 5);
		
		this.chooseFileBtn.setX((renderIn.field_230708_k_ / 2) - (this.doneButton.getWidth() / 2));
		this.chooseFileBtn.setY(((renderIn.field_230709_l_  / 2) + 50) - this.doneButton.getHeight() - 5);
		
		if (!FancyMenu.isNotHeadless()) {
			renderIn.func_238472_a_(matrix, Minecraft.getInstance().fontRenderer, new StringTextComponent(Locals.localize("helper.creator.choosefile.notsupported")), (renderIn.field_230708_k_ / 2), ((renderIn.field_230709_l_  / 2) + 50), Color.WHITE.getRGB());
		}
		
		this.renderButtons(matrix, mouseX, mouseY);
	}

}