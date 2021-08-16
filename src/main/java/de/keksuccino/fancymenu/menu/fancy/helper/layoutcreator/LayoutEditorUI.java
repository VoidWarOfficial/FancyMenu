package de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.google.common.io.Files;

import de.keksuccino.fancymenu.menu.animation.AdvancedAnimation;
import de.keksuccino.fancymenu.menu.animation.AnimationHandler;
import de.keksuccino.fancymenu.menu.fancy.MenuCustomization;
import de.keksuccino.fancymenu.menu.fancy.MenuCustomizationProperties;
import de.keksuccino.fancymenu.menu.fancy.guicreator.CustomGuiBase;
import de.keksuccino.fancymenu.menu.fancy.helper.CustomizationButton;
import de.keksuccino.fancymenu.menu.fancy.helper.CustomizationHelper;
import de.keksuccino.fancymenu.menu.fancy.helper.DynamicValueInputPopup;
import de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator.LayoutEditorScreen;
import de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator.WindowSizePopup.ActionType;
import de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator.content.BackgroundOptionsPopup;
import de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator.content.ChooseFilePopup;
import de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator.content.LayoutElement;
import de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator.content.button.LayoutButton;
import de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator.content.button.LayoutButtonDummyCustomizationItem;
import de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator.content.button.LayoutVanillaButton;
import de.keksuccino.fancymenu.menu.fancy.helper.ui.FMContextMenu;
import de.keksuccino.fancymenu.menu.fancy.helper.ui.MenuBar;
import de.keksuccino.fancymenu.menu.fancy.helper.ui.UIBase;
import de.keksuccino.fancymenu.menu.fancy.helper.ui.popup.FMTextInputPopup;
import de.keksuccino.fancymenu.menu.fancy.helper.ui.popup.FMYesNoPopup;
import de.keksuccino.fancymenu.menu.fancy.item.ShapeCustomizationItem.Shape;
import de.keksuccino.fancymenu.menu.slideshow.SlideshowHandler;
import de.keksuccino.fancymenu.menu.fancy.helper.ui.MenuBar.ElementAlignment;
import de.keksuccino.konkrete.gui.content.AdvancedButton;
import de.keksuccino.konkrete.gui.content.AdvancedImageButton;
import de.keksuccino.konkrete.gui.screens.popup.PopupHandler;
import de.keksuccino.konkrete.gui.screens.popup.TextInputPopup;
import de.keksuccino.konkrete.input.CharacterFilter;
import de.keksuccino.konkrete.input.StringUtils;
import de.keksuccino.konkrete.localization.Locals;
import de.keksuccino.konkrete.math.MathUtils;
import de.keksuccino.konkrete.properties.PropertiesSection;
import de.keksuccino.konkrete.properties.PropertiesSet;
import de.keksuccino.konkrete.rendering.animation.IAnimationRenderer;
import de.keksuccino.konkrete.sound.SoundHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;

public class LayoutEditorUI extends UIBase {
	
	public MenuBar bar;
	public LayoutEditorScreen parent;

	protected int tick = 0;
	
	protected static final ResourceLocation CLOSE_BUTTON_TEXTURE = new ResourceLocation("keksuccino", "close_btn.png");
	
	public LayoutEditorUI(LayoutEditorScreen parent) {
		this.parent = parent;
		this.updateUI();
	}
	
	public void updateUI() {
		try {
			
			boolean extended = true;
			if (bar != null) {
				extended = bar.isExtended();
			}
			
			bar = new MenuBar();
			bar.setExtended(extended);
			
			/** LAYOUT TAB **/
			FMContextMenu layoutMenu = new FMContextMenu();
			layoutMenu.setAutoclose(true);
			bar.addChild(layoutMenu, "fm.editor.ui.tab.layout", ElementAlignment.LEFT);
			
			AdvancedButton newLayoutButton = new AdvancedButton(0, 0, 0, 0, Locals.localize("helper.editor.ui.layout.new"), true, (press) -> {
				this.displayUnsavedWarning((call) -> {
					if (call) {
						MenuCustomization.stopSounds();
						MenuCustomization.resetSounds();
						Minecraft.getInstance().displayGuiScreen(new LayoutEditorScreen(this.parent.screen));
					}
				});
			});
			layoutMenu.addContent(newLayoutButton);
			
			OpenLayoutContextMenu openLayoutMenu = new OpenLayoutContextMenu(this);
			openLayoutMenu.setAutoclose(true);
			layoutMenu.addChild(openLayoutMenu);
			
			AdvancedButton openLayoutButton = new AdvancedButton(0, 0, 0, 0, Locals.localize("helper.editor.ui.layout.open"), true, (press) -> {
				openLayoutMenu.setParentButton((AdvancedButton) press);
				openLayoutMenu.openMenuAt(0, press.y);
			});
			layoutMenu.addContent(openLayoutButton);
			
			AdvancedButton layoutSaveButton = new AdvancedButton(0, 0, 0, 0, Locals.localize("helper.editor.ui.layout.save"), true, (press) -> {
				this.parent.saveLayout();
			});
			layoutMenu.addContent(layoutSaveButton);
			
			AdvancedButton layoutSaveAsButton = new AdvancedButton(0, 0, 0, 0, Locals.localize("helper.editor.ui.layout.saveas"), true, (press) -> {
				this.parent.saveLayoutAs();
			});
			layoutMenu.addContent(layoutSaveAsButton);
			
			LayoutPropertiesContextMenu layoutPropertiesMenu = new LayoutPropertiesContextMenu(this.parent, false);
			layoutPropertiesMenu.setAutoclose(true);
			layoutMenu.addChild(layoutPropertiesMenu);
			
			AdvancedButton layoutPropertiesButton = new AdvancedButton(0, 0, 0, 0, Locals.localize("helper.editor.ui.layout.properties"), true, (press) -> {
				layoutPropertiesMenu.setParentButton((AdvancedButton) press);
				layoutPropertiesMenu.openMenuAt(0, press.y);
			});
			layoutMenu.addContent(layoutPropertiesButton);
			
			AdvancedButton exitButton = new AdvancedButton(0, 0, 0, 0, Locals.localize("helper.editor.ui.exit"), true, (press) -> {
				this.closeEditor();
			});
			layoutMenu.addContent(exitButton);
			
			CustomizationButton layoutTab = new CustomizationButton(0, 0, 0, 0, Locals.localize("helper.editor.ui.layout"), true, (press) -> {
				layoutMenu.setParentButton((AdvancedButton) press);
				layoutMenu.openMenuAt(press.x, press.y + press.getHeight());
			});
			bar.addElement(layoutTab, "fm.editor.ui.tab.layout", ElementAlignment.LEFT, false);
			
			/** EDIT TAB **/
			FMContextMenu editMenu = new FMContextMenu();
			editMenu.setAutoclose(true);
			bar.addChild(editMenu, "fm.editor.ui.tab.edit", ElementAlignment.LEFT);
			
			AdvancedButton undoButton = new AdvancedButton(0, 0, 0, 0, Locals.localize("helper.editor.ui.edit.undo"), true, (press) -> {
				this.parent.history.stepBack();
				try {
					((LayoutEditorScreen)Minecraft.getInstance().currentScreen).ui.bar.getChild("fm.editor.ui.tab.edit").openMenuAt(editMenu.getX(), editMenu.getY());
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			editMenu.addContent(undoButton);
			
			AdvancedButton redoButton = new AdvancedButton(0, 0, 0, 0, Locals.localize("helper.editor.ui.edit.redo"), true, (press) -> {
				this.parent.history.stepForward();
				try {
					((LayoutEditorScreen)Minecraft.getInstance().currentScreen).ui.bar.getChild("fm.editor.ui.tab.edit").openMenuAt(editMenu.getX(), editMenu.getY());
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			editMenu.addContent(redoButton);
			
			editMenu.addSeparator();
			
			AdvancedButton copyButton = new AdvancedButton(0, 0, 0, 0, Locals.localize("helper.editor.ui.edit.copy"), true, (press) -> {
				this.parent.copySelectedElements();
			});
			editMenu.addContent(copyButton);
			
			AdvancedButton pasteButton = new AdvancedButton(0, 0, 0, 0, Locals.localize("helper.editor.ui.edit.paste"), true, (press) -> {
				this.parent.pasteElements();
			});
			editMenu.addContent(pasteButton);
			
			CustomizationButton editTab = new CustomizationButton(0, 0, 0, 0, Locals.localize("helper.editor.ui.edit"), true, (press) -> {
				editMenu.setParentButton((AdvancedButton) press);
				editMenu.openMenuAt(press.x, press.y + press.getHeight());
			});
			bar.addElement(editTab, "fm.editor.ui.tab.edit", ElementAlignment.LEFT, false);
			
			/** ELEMENT TAB **/
			FMContextMenu elementMenu = new FMContextMenu();
			elementMenu.setAutoclose(true);
			bar.addChild(elementMenu, "fm.editor.ui.tab.element", ElementAlignment.LEFT);
			
			NewElementContextMenu newElementMenu = new NewElementContextMenu(this.parent);
			newElementMenu.setAutoclose(true);
			elementMenu.addChild(newElementMenu);
			
			AdvancedButton newElementButton = new AdvancedButton(0, 0, 0, 0, Locals.localize("helper.editor.ui.element.new"), true, (press) -> {
				newElementMenu.setParentButton((AdvancedButton) press);
				newElementMenu.openMenuAt(0, press.y);
			});
			elementMenu.addContent(newElementButton);
			
			ManageAudioContextMenu manageAudioMenu = new ManageAudioContextMenu(this.parent);
			manageAudioMenu.setAutoclose(true);
			elementMenu.addChild(manageAudioMenu);
			
			AdvancedButton manageAudioButton = new AdvancedButton(0, 0, 0, 0, Locals.localize("helper.editor.ui.element.manageaudio"), true, (press) -> {
				manageAudioMenu.setParentButton((AdvancedButton) press);
				manageAudioMenu.openMenuAt(0, press.y);
			});
			elementMenu.addContent(manageAudioButton);
			
			HiddenVanillaButtonContextMenu hiddenVanillaMenu = new HiddenVanillaButtonContextMenu(this.parent);
			hiddenVanillaMenu.setAutoclose(true);
			elementMenu.addChild(hiddenVanillaMenu);
			
			AdvancedButton hiddenVanillaButton = new AdvancedButton(0, 0, 0, 0, Locals.localize("helper.editor.ui.element.deletedvanillabuttons"), true, (press) -> {
				hiddenVanillaMenu.setParentButton((AdvancedButton) press);
				hiddenVanillaMenu.openMenuAt(0, press.y);
			});
			elementMenu.addContent(hiddenVanillaButton);
			
			CustomizationButton elementTab = new CustomizationButton(0, 0, 0, 0, Locals.localize("helper.editor.ui.element"), true, (press) -> {
				elementMenu.setParentButton((AdvancedButton) press);
				elementMenu.openMenuAt(press.x, press.y + press.getHeight());
			});
			bar.addElement(elementTab, "fm.editor.ui.tab.element", ElementAlignment.LEFT, false);
			
			/** CLOSE GUI BUTTON TAB **/
			AdvancedImageButton exitEditorButtonTab = new AdvancedImageButton(20, 20, 0, 0, CLOSE_BUTTON_TEXTURE, true, (press) -> {
				this.closeEditor();
			}) {
				@Override
				public void render(int mouseX, int mouseY, float partialTicks) {
					this.width = this.height;
					super.render(mouseX, mouseY, partialTicks);
				}
			};
			exitEditorButtonTab.ignoreLeftMouseDownClickBlock = true;
			exitEditorButtonTab.ignoreBlockedInput = true;
			exitEditorButtonTab.enableRightclick = true;
			exitEditorButtonTab.setDescription(StringUtils.splitLines(Locals.localize("helper.editor.ui.exit.desc"), "%n%"));
			bar.addElement(exitEditorButtonTab, "fm.editor.ui.tab.exit", ElementAlignment.RIGHT, false);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void render(Screen screen) {
		try {

			if (bar != null) {
				if (!PopupHandler.isPopupActive()) {
					if (screen instanceof LayoutEditorScreen) {

						bar.render(screen);

					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void displayUnsavedWarning(Consumer<Boolean> callback) {
		PopupHandler.displayPopup(new FMYesNoPopup(300, new Color(0, 0, 0, 0), 240, callback, Locals.localize("helper.editor.ui.unsavedwarning")));
	}
	
	public void closeEditor() {
		this.displayUnsavedWarning((call) -> {
			if (call) {
				LayoutEditorScreen.isActive = false;
				for (IAnimationRenderer r : AnimationHandler.getAnimations()) {
					if (r instanceof AdvancedAnimation) {
						((AdvancedAnimation)r).stopAudio();
						if (((AdvancedAnimation)r).replayIntro()) {
							((AdvancedAnimation)r).resetAnimation();
						}
					}
				}
				MenuCustomization.stopSounds();
				MenuCustomization.resetSounds();
				MenuCustomizationProperties.loadProperties();

				Minecraft.getInstance().getMainWindow().setGuiScale(Minecraft.getInstance().getMainWindow().calcGuiScale(Minecraft.getInstance().gameSettings.guiScale, Minecraft.getInstance().getForceUnicodeFont()));
				this.parent.height = Minecraft.getInstance().getMainWindow().getScaledHeight();
				this.parent.width = Minecraft.getInstance().getMainWindow().getScaledWidth();

				Minecraft.getInstance().displayGuiScreen(this.parent.screen);
			}
		});
	}

	private static class OpenLayoutContextMenu extends FMContextMenu {

		private LayoutEditorUI ui;
		
		public OpenLayoutContextMenu(LayoutEditorUI ui) {
			this.ui = ui;
		}
		
		@Override
		public void openMenuAt(int x, int y, int screenWidth, int screenHeight) {
			
			this.content.clear();

			String identifier = this.ui.parent.screen.getClass().getName();
			if (this.ui.parent.screen instanceof CustomGuiBase) {
				identifier = ((CustomGuiBase) this.ui.parent.screen).getIdentifier();
			}
			
			List<PropertiesSet> enabled = MenuCustomizationProperties.getPropertiesWithIdentifier(identifier);
			if (!enabled.isEmpty()) {
				for (PropertiesSet s : enabled) {
					List<PropertiesSection> secs = s.getPropertiesOfType("customization-meta");
					if (secs.isEmpty()) {
						secs = s.getPropertiesOfType("type-meta");
					}
					if (!secs.isEmpty()) {
						String name = "<missing name>";
						PropertiesSection meta = secs.get(0);
						File f = new File(meta.getEntryValue("path"));
						if (f.isFile()) {
							name = Files.getNameWithoutExtension(f.getName());
							
							int totalactions = s.getProperties().size() - 1;
							AdvancedButton layoutEntryBtn = new AdvancedButton(0, 0, 0, 0, "§a" + name, (press) -> {
								this.ui.displayUnsavedWarning((call) -> {
									CustomizationHelper.editLayout(this.ui.parent.screen, f);
								});
							});
							layoutEntryBtn.setDescription(StringUtils.splitLines(Locals.localize("helper.buttons.customization.managelayouts.layout.btndesc", Locals.localize("helper.buttons.customization.managelayouts.enabled"), "" + totalactions), "%n%"));
							this.addContent(layoutEntryBtn);
						}
					}
				}
			}
			
			List<PropertiesSet> disabled = MenuCustomizationProperties.getDisabledPropertiesWithIdentifier(identifier);
			if (!disabled.isEmpty()) {
				for (PropertiesSet s : disabled) {
					List<PropertiesSection> secs = s.getPropertiesOfType("customization-meta");
					if (secs.isEmpty()) {
						secs = s.getPropertiesOfType("type-meta");
					}
					if (!secs.isEmpty()) {
						String name = "<missing name>";
						PropertiesSection meta = secs.get(0);
						File f = new File(meta.getEntryValue("path"));
						if (f.isFile()) {
							name = Files.getNameWithoutExtension(f.getName());
							
							int totalactions = s.getProperties().size() - 1;
							AdvancedButton layoutEntryBtn = new AdvancedButton(0, 0, 0, 0, "§c" + name, (press) -> {
								this.ui.displayUnsavedWarning((call) -> {
									CustomizationHelper.editLayout(this.ui.parent.screen, f);
								});
							});
							layoutEntryBtn.setDescription(StringUtils.splitLines(Locals.localize("helper.buttons.customization.managelayouts.layout.btndesc", Locals.localize("helper.buttons.customization.managelayouts.disabled"), "" + totalactions), "%n%"));
							this.addContent(layoutEntryBtn);
						}
					}
				}
			}
			
			if (enabled.isEmpty() && disabled.isEmpty()) {
				AdvancedButton emptyBtn = new AdvancedButton(0, 0, 0, 0, Locals.localize("helper.creator.empty"), (press) -> {});
				this.addContent(emptyBtn);
			}
			
			super.openMenuAt(x, y, screenWidth, screenHeight);
		}
		
	}
	
	public static class LayoutPropertiesContextMenu extends FMContextMenu {
		
		private LayoutEditorScreen parent;
		
		private AdvancedButton renderingOrderBackgroundButton;
		private AdvancedButton renderingOrderForegroundButton;
		
		private boolean isRightclickOpened;
		
		public LayoutPropertiesContextMenu(LayoutEditorScreen parent, boolean openedByRightclick) {
			this.parent = parent;
			this.isRightclickOpened = openedByRightclick;
		}
		
		@Override
		public void openMenuAt(int x, int y, int screenWidth, int screenHeight) {
			
			this.content.clear();
			
			/** BACKGROUND OPTIONS **/
			AdvancedButton backgroundOptionsButton = new AdvancedButton(0, 0, 0, 16, Locals.localize("helper.creator.backgroundoptions"), true, (press) -> {
				PopupHandler.displayPopup(new BackgroundOptionsPopup(this.parent));
			});
			this.addContent(backgroundOptionsButton);
			
			/** RESET BACKGROUND **/
			AdvancedButton resetBackgroundButton = new AdvancedButton(0, 0, 0, 16, Locals.localize("helper.creator.layoutoptions.resetbackground"), true, (press) -> {
				if ((this.parent.backgroundTexture != null) || (this.parent.backgroundAnimation != null) || (this.parent.backgroundPanorama != null)) {
					this.parent.history.saveSnapshot(this.parent.history.createSnapshot());
				}
				
				if (this.parent.backgroundAnimation != null) {
					((AdvancedAnimation)this.parent.backgroundAnimation).stopAudio();
				}

				this.parent.backgroundAnimationNames = new ArrayList<String>();
				this.parent.backgroundPanorama = null;
				this.parent.backgroundSlideshow = null;
				this.parent.backgroundAnimation = null;
				this.parent.backgroundTexture = null;
			});
			this.addContent(resetBackgroundButton);
			
			this.addSeparator();
			
			/** RANDOM MODE **/
			String randomModeString = Locals.localize("fancymenu.helper.creator.layoutoptions.randommode.on");
			if (!this.parent.randomMode) {
				randomModeString = Locals.localize("fancymenu.helper.creator.layoutoptions.randommode.off");
			}
			AdvancedButton randomModeButton = new AdvancedButton(0, 0, 0, 16, randomModeString, true, (press) -> {
				if (this.parent.randomMode) {
					((AdvancedButton)press).setMessage(Locals.localize("fancymenu.helper.creator.layoutoptions.randommode.off"));
					this.parent.randomMode = false;
				} else {
					((AdvancedButton)press).setMessage(Locals.localize("fancymenu.helper.creator.layoutoptions.randommode.on"));
					this.parent.randomMode = true;
				}
			});
			randomModeButton.setDescription(StringUtils.splitLines(Locals.localize("fancymenu.helper.creator.layoutoptions.randommode.btn.desc"), "%n%"));
			this.addContent(randomModeButton);
			
			AdvancedButton randomModeGroupButton = new AdvancedButton(0, 0, 0, 16, Locals.localize("fancymenu.helper.creator.layoutoptions.randommode.setgroup"), true, (press) -> {
				FMTextInputPopup pop = new FMTextInputPopup(new Color(0, 0, 0, 0), Locals.localize("fancymenu.helper.creator.layoutoptions.randommode.setgroup"), CharacterFilter.getIntegerCharacterFiler(), 240, (call) -> {
					if (call != null) {
						if (!MathUtils.isInteger(call)) {
							call = "1";
						}
						if (!call.equalsIgnoreCase(this.parent.randomGroup)) {
							this.parent.history.saveSnapshot(this.parent.history.createSnapshot());
						}
						this.parent.randomGroup = call;
					}
				});
				if (this.parent.randomGroup != null) {
					pop.setText(this.parent.randomGroup);
				}
				PopupHandler.displayPopup(pop);
			}) {
				@Override
				public void render(int mouseX, int mouseY, float partialTicks) {
					if (parent.randomMode) {
						this.active = true;
					} else {
						this.active = false;
					}
					super.render(mouseX, mouseY, partialTicks);
				}
			};
			randomModeGroupButton.setDescription(StringUtils.splitLines(Locals.localize("fancymenu.helper.creator.layoutoptions.randommode.setgroup.btn.desc"), "%n%"));
			this.addContent(randomModeGroupButton);
			
			String randomModeFirstTimeString = Locals.localize("fancymenu.helper.creator.layoutoptions.randommode.onlyfirsttime.on");
			if (!this.parent.randomOnlyFirstTime) {
				randomModeFirstTimeString = Locals.localize("fancymenu.helper.creator.layoutoptions.randommode.onlyfirsttime.off");
			}
			AdvancedButton randomModeFirstTimeButton = new AdvancedButton(0, 0, 0, 16, randomModeFirstTimeString, true, (press) -> {
				if (this.parent.randomOnlyFirstTime) {
					((AdvancedButton)press).setMessage(Locals.localize("fancymenu.helper.creator.layoutoptions.randommode.onlyfirsttime.off"));
					this.parent.randomOnlyFirstTime = false;
				} else {
					((AdvancedButton)press).setMessage(Locals.localize("fancymenu.helper.creator.layoutoptions.randommode.onlyfirsttime.on"));
					this.parent.randomOnlyFirstTime = true;
				}
			}) {
				@Override
				public void render(int mouseX, int mouseY, float partialTicks) {
					if (parent.randomMode) {
						this.active = true;
					} else {
						this.active = false;
					}
					super.render(mouseX, mouseY, partialTicks);
				}
			};
			randomModeFirstTimeButton.setDescription(StringUtils.splitLines(Locals.localize("fancymenu.helper.creator.layoutoptions.randommode.onlyfirsttime.btn.desc"), "%n%"));
			this.addContent(randomModeFirstTimeButton);

			this.addSeparator();

			/** RENDERING ORDER **/
			FMContextMenu renderingOrderMenu = new FMContextMenu();
			renderingOrderMenu.setAutoclose(true);
			this.addChild(renderingOrderMenu);
			
			this.renderingOrderBackgroundButton = new AdvancedButton(0, 0, 0, 16, Locals.localize("helper.creator.layoutoptions.renderorder.background"), true, (press) -> {
				((AdvancedButton)press).setMessage("§a" + Locals.localize("helper.creator.layoutoptions.renderorder.background"));
				this.renderingOrderForegroundButton.setMessage(Locals.localize("helper.creator.layoutoptions.renderorder.foreground"));
				if (!this.parent.renderorder.equals("background")) {
					this.parent.history.saveSnapshot(this.parent.history.createSnapshot());
				}
				
				this.parent.renderorder = "background";
			});
			renderingOrderMenu.addContent(renderingOrderBackgroundButton);
			
			this.renderingOrderForegroundButton = new AdvancedButton(0, 0, 0, 16, "§a" + Locals.localize("helper.creator.layoutoptions.renderorder.foreground"), true, (press) -> {
				((AdvancedButton)press).setMessage("§a" + Locals.localize("helper.creator.layoutoptions.renderorder.foreground"));
				this.renderingOrderBackgroundButton.setMessage(Locals.localize("helper.creator.layoutoptions.renderorder.background"));
				if (!this.parent.renderorder.equals("foreground")) {
					this.parent.history.saveSnapshot(this.parent.history.createSnapshot());
				}
				
				this.parent.renderorder = "foreground";
			});
			renderingOrderMenu.addContent(renderingOrderForegroundButton);
			
			if (this.parent.renderorder.equals("background")) {
				renderingOrderForegroundButton.setMessage(Locals.localize("helper.creator.layoutoptions.renderorder.foreground"));
				renderingOrderBackgroundButton.setMessage("§a" + Locals.localize("helper.creator.layoutoptions.renderorder.background"));
			}
			
			AdvancedButton renderingOrderButton = new AdvancedButton(0, 0, 0, 16, Locals.localize("helper.creator.layoutoptions.renderorder"), true, (press) -> {
				renderingOrderMenu.setParentButton((AdvancedButton) press);
				renderingOrderMenu.openMenuAt(0, press.y, screenWidth, screenHeight);
			});
			this.addContent(renderingOrderButton);
			
			/** MENU SCALE **/
			AdvancedButton menuScaleButton = new AdvancedButton(0, 0, 0, 16, Locals.localize("helper.creator.rightclick.scale"), true, (press) -> {
				TextInputPopup p = new TextInputPopup(new Color(0, 0, 0, 0), Locals.localize("helper.creator.rightclick.scale"), CharacterFilter.getIntegerCharacterFiler(), 240, (call) -> {
					if ((call != null) && MathUtils.isInteger(call)) {
						int s = Integer.parseInt(call);
						if (s < 0) {
							LayoutEditorScreen.displayNotification(Locals.localize("helper.creator.rightclick.scale.invalid"), "", "", "", "");
						} else {

							if (this.parent.scale != s) {
								this.parent.history.saveSnapshot(this.parent.history.createSnapshot());
							}
							
							this.parent.scale = s;
							this.parent.init(Minecraft.getInstance(), Minecraft.getInstance().getMainWindow().getScaledWidth(), Minecraft.getInstance().getMainWindow().getScaledHeight());
						
						}
					}
				});
				p.setText("" + this.parent.scale);
				PopupHandler.displayPopup(p);
			});
			this.addContent(menuScaleButton);
			
			/** OPEN/CLOSE SOUND **/
			FMContextMenu openCloseSoundMenu = new FMContextMenu();
			openCloseSoundMenu.setAutoclose(true);
			this.addChild(openCloseSoundMenu);
			
			AdvancedButton openSoundBtn = new AdvancedButton(0, 0, 0, 16, Locals.localize("helper.creator.openaudio"), true, (press) -> {
				ChooseFilePopup p = new ChooseFilePopup((call) -> {
					if (call != null) {
						if (call.length() < 3) {
							if (this.parent.openAudio != null) {
								this.parent.history.saveSnapshot(this.parent.history.createSnapshot());
							}
							this.parent.openAudio = null;
						} else {
							File f = new File(call);
							if (f.exists() && f.isFile() && f.getName().toLowerCase().endsWith(".wav")) {
								if (this.parent.openAudio != call) {
									this.parent.history.saveSnapshot(this.parent.history.createSnapshot());
								}
								this.parent.openAudio = call;
							} else {
								LayoutEditorScreen.displayNotification("§c§l" + Locals.localize("helper.creator.invalidaudio.title"), "", Locals.localize("helper.creator.invalidaudio.desc"), "", "", "", "", "");
							}
						}
					} else {
						if (this.parent.openAudio != null) {
							this.parent.history.saveSnapshot(this.parent.history.createSnapshot());
						}
						this.parent.openAudio = null;
					}
				}, "wav");
				if (this.parent.openAudio != null) {
					p.setText(this.parent.openAudio);
				}
				PopupHandler.displayPopup(p);
			});
			openSoundBtn.setDescription(StringUtils.splitLines(Locals.localize("helper.creator.openaudio.desc"), "%n%"));
			openCloseSoundMenu.addContent(openSoundBtn);
			
			AdvancedButton resetOpenBtn = new AdvancedButton(0, 0, 0, 16, Locals.localize("helper.creator.openaudio.reset"), true, (press) -> {
				if (this.parent.openAudio != null) {
					this.parent.history.saveSnapshot(this.parent.history.createSnapshot());
				}
				this.parent.openAudio = null;
			});
			resetOpenBtn.setDescription(StringUtils.splitLines(Locals.localize("helper.creator.opencloseaudio.reset.desc"), "%n%"));
			openCloseSoundMenu.addContent(resetOpenBtn);
			
			AdvancedButton closeSoundBtn = new AdvancedButton(0, 0, 0, 16, Locals.localize("helper.creator.closeaudio"), true, (press) -> {
				ChooseFilePopup p = new ChooseFilePopup((call) -> {
					if (call != null) {
						if (call.length() < 3) {
							if (this.parent.closeAudio != null) {
								this.parent.history.saveSnapshot(this.parent.history.createSnapshot());
							}
							this.parent.closeAudio = null;
						} else {
							File f = new File(call);
							if (f.exists() && f.isFile() && f.getName().toLowerCase().endsWith(".wav")) {
								if (this.parent.closeAudio != call) {
									this.parent.history.saveSnapshot(this.parent.history.createSnapshot());
								}
								this.parent.closeAudio = call;
							} else {
								LayoutEditorScreen.displayNotification("§c§l" + Locals.localize("helper.creator.invalidaudio.title"), "", Locals.localize("helper.creator.invalidaudio.desc"), "", "", "", "", "");
							}
						}
					} else {
						if (this.parent.closeAudio != null) {
							this.parent.history.saveSnapshot(this.parent.history.createSnapshot());
						}
						this.parent.closeAudio = null;
					}
				}, "wav");
				if (this.parent.closeAudio != null) {
					p.setText(this.parent.closeAudio);
				}
				PopupHandler.displayPopup(p);
			});
			closeSoundBtn.setDescription(StringUtils.splitLines(Locals.localize("helper.creator.closeaudio.desc"), "%n%"));
			openCloseSoundMenu.addContent(closeSoundBtn);
			
			AdvancedButton resetCloseBtn = new AdvancedButton(0, 0, 0, 16, Locals.localize("helper.creator.closeaudio.reset"), true, (press) -> {
				if (this.parent.closeAudio != null) {
					this.parent.history.saveSnapshot(this.parent.history.createSnapshot());
				}
				this.parent.closeAudio = null;
			});
			resetCloseBtn.setDescription(StringUtils.splitLines(Locals.localize("helper.creator.opencloseaudio.reset.desc"), "%n%"));
			openCloseSoundMenu.addContent(resetCloseBtn);
			
			AdvancedButton openCloseSoundButton = new AdvancedButton(0, 0, 0, 16, Locals.localize("helper.creator.opencloseaudio"), true, (press) -> {
				openCloseSoundMenu.setParentButton((AdvancedButton) press);
				openCloseSoundMenu.openMenuAt(0, press.y, screenWidth, screenHeight);
			});
			openCloseSoundButton.setDescription(StringUtils.splitLines(Locals.localize("helper.creator.opencloseaudio.desc"), "%n%"));
			this.addContent(openCloseSoundButton);
			
			this.addSeparator();
			
			/** WINDOW SIZE RESTRICTIONS **/
			FMContextMenu windowSizeMenu = new FMContextMenu();
			windowSizeMenu.setAutoclose(true);
			this.addChild(windowSizeMenu);
			
			AdvancedButton biggerThanButton = new AdvancedButton(0, 0, 0, 0, Locals.localize("helper.creator.windowsize.biggerthan"), true, (press) -> {
				PopupHandler.displayPopup(new WindowSizePopup(this.parent, ActionType.BIGGERTHAN));
			});
			windowSizeMenu.addContent(biggerThanButton);
			
			AdvancedButton smallerThanButton = new AdvancedButton(0, 0, 0, 0, Locals.localize("helper.creator.windowsize.smallerthan"), true, (press) -> {
				PopupHandler.displayPopup(new WindowSizePopup(this.parent, ActionType.SMALLERTHAN));
			});
			windowSizeMenu.addContent(smallerThanButton);
			
			AdvancedButton windowSizeButton = new AdvancedButton(0, 0, 0, 16, Locals.localize("helper.creator.windowsize"), true, (press) -> {
				windowSizeMenu.setParentButton((AdvancedButton) press);
				windowSizeMenu.openMenuAt(0, press.y, screenWidth, screenHeight);
			});
			this.addContent(windowSizeButton);
			
			/** REQUIRED MODS **/
			AdvancedButton requiredModsButton = new AdvancedButton(0, 0, 0, 16, Locals.localize("helper.creator.layoutoptions.requiredmods"), true, (press) -> {
				TextInputPopup p = new TextInputPopup(new Color(0, 0, 0, 0), "§l" + Locals.localize("helper.creator.layoutoptions.requiredmods.desc"), null, 240, (call) -> {
					if (call != null) {
						if (this.parent.requiredmods != call) {
							this.parent.history.saveSnapshot(this.parent.history.createSnapshot());
						}
						
						this.parent.requiredmods = call;
					}
				});
				if (this.parent.requiredmods != null) {
					p.setText(this.parent.requiredmods);
				}
				PopupHandler.displayPopup(p);
			});
			this.addContent(requiredModsButton);
			
			/** MC VERSION **/
			FMContextMenu mcVersionMenu = new FMContextMenu();
			mcVersionMenu.setAutoclose(true);
			this.addChild(mcVersionMenu);
			
			AdvancedButton minMcVersionButton = new AdvancedButton(0, 0, 0, 16, Locals.localize("helper.creator.layoutoptions.version.minimum"), true, (press) -> {
				FMTextInputPopup p = new FMTextInputPopup(new Color(0, 0, 0, 0), "§l" + Locals.localize("helper.creator.layoutoptions.version.minimum.mc"), null, 240, (call) -> {
					if (call != null) {
						if (this.parent.minimumMC != call) {
							this.parent.history.saveSnapshot(this.parent.history.createSnapshot());
						}
						
						this.parent.minimumMC = call;
					}
				});
				if (this.parent.minimumMC != null) {
					p.setText(this.parent.minimumMC);
				}
				PopupHandler.displayPopup(p);
			});
			mcVersionMenu.addContent(minMcVersionButton);
			
			AdvancedButton maxMcVersionButton = new AdvancedButton(0, 0, 0, 16, Locals.localize("helper.creator.layoutoptions.version.maximum"), true, (press) -> {
				FMTextInputPopup p = new FMTextInputPopup(new Color(0, 0, 0, 0), "§l" + Locals.localize("helper.creator.layoutoptions.version.maximum.mc"), null, 240, (call) -> {
					if (call != null) {
						if (this.parent.maximumMC != call) {
							this.parent.history.saveSnapshot(this.parent.history.createSnapshot());
						}
						
						this.parent.maximumMC = call;
					}
				});
				if (this.parent.maximumMC != null) {
					p.setText(this.parent.maximumMC);
				}
				PopupHandler.displayPopup(p);
			});
			mcVersionMenu.addContent(maxMcVersionButton);
			
			AdvancedButton mcVersionButton = new AdvancedButton(0, 0, 0, 16, Locals.localize("helper.creator.layoutoptions.version.mc"), true, (press) -> {
				mcVersionMenu.setParentButton((AdvancedButton) press);
				mcVersionMenu.openMenuAt(0, press.y, screenWidth, screenHeight);
			});
			this.addContent(mcVersionButton);
			
			/** FM VERSION **/
			FMContextMenu fmVersionMenu = new FMContextMenu();
			fmVersionMenu.setAutoclose(true);
			this.addChild(fmVersionMenu);
			
			AdvancedButton minFmVersionButton = new AdvancedButton(0, 0, 0, 16, Locals.localize("helper.creator.layoutoptions.version.minimum"), true, (press) -> {
				FMTextInputPopup p = new FMTextInputPopup(new Color(0, 0, 0, 0), "§l" + Locals.localize("helper.creator.layoutoptions.version.minimum.fm"), null, 240, (call) -> {
					if (call != null) {
						if (this.parent.minimumFM != call) {
							this.parent.history.saveSnapshot(this.parent.history.createSnapshot());
						}
						
						this.parent.minimumFM = call;
					}
				});
				if (this.parent.minimumFM != null) {
					p.setText(this.parent.minimumFM);
				}
				PopupHandler.displayPopup(p);
			});
			fmVersionMenu.addContent(minFmVersionButton);
			
			AdvancedButton maxFmVersionButton = new AdvancedButton(0, 0, 0, 16, Locals.localize("helper.creator.layoutoptions.version.maximum"), true, (press) -> {
				FMTextInputPopup p = new FMTextInputPopup(new Color(0, 0, 0, 0), "§l" + Locals.localize("helper.creator.layoutoptions.version.maximum.fm"), null, 240, (call) -> {
					if (call != null) {
						if (this.parent.maximumFM != call) {
							this.parent.history.saveSnapshot(this.parent.history.createSnapshot());
						}
						
						this.parent.maximumFM = call;
					}
				});
				if (this.parent.maximumFM != null) {
					p.setText(this.parent.maximumFM);
				}
				PopupHandler.displayPopup(p);
			});
			fmVersionMenu.addContent(maxFmVersionButton);
			
			AdvancedButton fmVersionButton = new AdvancedButton(0, 0, 0, 16, Locals.localize("helper.creator.layoutoptions.version.fm"), true, (press) -> {
				fmVersionMenu.setParentButton((AdvancedButton) press);
				fmVersionMenu.openMenuAt(0, press.y, screenWidth, screenHeight);
			});
			this.addContent(fmVersionButton);
			
			if (this.isRightclickOpened) {
				this.addSeparator();
			}
			
			/** PASTE **/
			AdvancedButton pasteButton = new AdvancedButton(0, 0, 0, 16, Locals.localize("helper.editor.ui.edit.paste"), (press) -> {
				this.parent.pasteElements();
			});
			if (this.isRightclickOpened) {
				this.addContent(pasteButton);
			}
			
			/** NEW ELEMENT **/
			NewElementContextMenu newElementMenu = new NewElementContextMenu(this.parent);
			newElementMenu.setAutoclose(true);
			this.addChild(newElementMenu);
			
			AdvancedButton newElementButton = new AdvancedButton(0, 0, 0, 16, Locals.localize("helper.editor.ui.layoutproperties.newelement"), (press) -> {
				newElementMenu.setParentButton((AdvancedButton) press);
				newElementMenu.openMenuAt(0, press.y, screenWidth, screenHeight);
			});
			if (this.isRightclickOpened) {
				this.addContent(newElementButton);
			}
			
			
			super.openMenuAt(x, y, screenWidth, screenHeight);
		}
		
	}
	
	public static class NewElementContextMenu extends FMContextMenu {
		
		private LayoutEditorScreen parent;
		
		public NewElementContextMenu(LayoutEditorScreen parent) {
			this.parent = parent;
		}
		
		@Override
		public void openMenuAt(int x, int y, int screenWidth, int screenHeight) {
			
			this.content.clear();
			
			/** IMAGE **/
			AdvancedButton imageButton = new AdvancedButton(0, 0, 0, 20, Locals.localize("helper.creator.add.image"), (press) -> {
				PopupHandler.displayPopup(new ChooseFilePopup(this.parent::addTexture, "jpg", "jpeg", "png", "gif"));
			});
			this.addContent(imageButton);

			/** WEB IMAGE **/
			AdvancedButton webImageButton = new AdvancedButton(0, 0, 0, 20, Locals.localize("helper.creator.add.webimage"), (press) -> {
				PopupHandler.displayPopup(new DynamicValueInputPopup(new Color(0, 0, 0, 0), "§l" + Locals.localize("helper.creator.web.enterurl"), null, 240, this.parent::addWebTexture));
			});
			this.addContent(webImageButton);
			
			/** TEXT **/
			AdvancedButton textButton = new AdvancedButton(0, 0, 0, 20, Locals.localize("helper.creator.add.text"), (press) -> {
				PopupHandler.displayPopup(new DynamicValueInputPopup(new Color(0, 0, 0, 0), "§l" + Locals.localize("helper.creator.add.text.newtext") + ":", null, 240, this.parent::addText));
			});
			this.addContent(textButton);
			
			/** WEB TEXT **/
			AdvancedButton webTextButton = new AdvancedButton(0, 0, 0, 20, Locals.localize("helper.creator.add.webtext"), (press) -> {
				PopupHandler.displayPopup(new DynamicValueInputPopup(new Color(0, 0, 0, 0), "§l" + Locals.localize("helper.creator.web.enterurl"), null, 240, this.parent::addWebText));
			});
			this.addContent(webTextButton);
			
			/** SPLASH TEXT **/
			FMContextMenu splashMenu = new FMContextMenu();
			splashMenu.setAutoclose(true);
			this.addChild(splashMenu);
			
			AdvancedButton singleSplashButton = new AdvancedButton(0, 0, 0, 0, Locals.localize("helper.creator.add.splash.single"), true, (press) -> {
				PopupHandler.displayPopup(new DynamicValueInputPopup(new Color(0, 0, 0, 0), Locals.localize("helper.creator.add.splash.single.desc"), null, 240, this.parent::addSingleSplashText));
			});
			singleSplashButton.setDescription(StringUtils.splitLines(Locals.localize("helper.creator.add.splash.single.desc"), "%n%"));
			splashMenu.addContent(singleSplashButton);
			
			AdvancedButton multiSplashButton = new AdvancedButton(0, 0, 0, 0, Locals.localize("helper.creator.add.splash.multi"), true, (press) -> {
				PopupHandler.displayPopup(new ChooseFilePopup(this.parent::addMultiSplashText, "txt"));
			});
			multiSplashButton.setDescription(StringUtils.splitLines(Locals.localize("helper.creator.add.splash.multi.desc"), "%n%"));
			splashMenu.addContent(multiSplashButton);
			
			AdvancedButton splashButton = new AdvancedButton(0, 0, 0, 20, Locals.localize("helper.creator.add.splash"), (press) -> {
				splashMenu.setParentButton((AdvancedButton) press);
				splashMenu.openMenuAt(0, press.y, screenWidth, screenHeight);
			});
			this.addContent(splashButton);
			
			/** BUTTON **/
			AdvancedButton buttonButton = new AdvancedButton(0, 0, 0, 20, Locals.localize("helper.creator.add.button"), (press) -> {
				PopupHandler.displayPopup(new DynamicValueInputPopup(new Color(0, 0, 0, 0), "§l" + Locals.localize("helper.creator.add.button.label") + ":", null, 240, this.parent::addButton));
			});
			this.addContent(buttonButton);
			
			/** AUDIO **/
			AdvancedButton audioButton = new AdvancedButton(0, 0, 0, 20, Locals.localize("helper.creator.add.audio"), (press) -> {
				PopupHandler.displayPopup(new ChooseFilePopup(this.parent::addAudio, "wav"));
			});
			this.addContent(audioButton);

			/** PLAYER ENTITY **/
			AdvancedButton playerEntityButton = new AdvancedButton(0, 0, 0, 20, Locals.localize("helper.creator.add.playerentity"), (press) -> {
				this.parent.addPlayerEntity();
			});
			this.addContent(playerEntityButton);
			
			/** ANIMATION **/
			FMContextMenu animationMenu = new FMContextMenu();
			animationMenu.setAutoclose(true);
			this.addChild(animationMenu);
			
			AdvancedButton inputAnimationButton = new AdvancedButton(0, 0, 0, 20, Locals.localize("helper.creator.add.animation.entername"), true, (press) -> {
				PopupHandler.displayPopup(new FMTextInputPopup(new Color(0, 0, 0, 0), "§l" + Locals.localize("helper.creator.add.animation.entername.title") + ":", null, 240, this.parent::addAnimation));
			});
			animationMenu.addContent(inputAnimationButton);
			
			animationMenu.addSeparator();
			
			for (String s : AnimationHandler.getCustomAnimationNames()) {
				AdvancedButton aniB = new AdvancedButton(0, 0, 0, 20, s, true, (press) -> {
					this.parent.addAnimation(s);
				});
				animationMenu.addContent(aniB);
			}
			
			AdvancedButton animationButton = new AdvancedButton(0, 0, 0, 20, Locals.localize("helper.creator.add.animation"), (press) -> {
				animationMenu.setParentButton((AdvancedButton) press);
				animationMenu.openMenuAt(0, press.y, screenWidth, screenHeight);
			});
			this.addContent(animationButton);

			/** SLIDESHOW **/
			FMContextMenu slideshowMenu = new FMContextMenu();
			slideshowMenu.setAutoclose(true);
			this.addChild(slideshowMenu);

			AdvancedButton inputSlideshowButton = new AdvancedButton(0, 0, 0, 20, Locals.localize("helper.creator.add.slideshow.entername"), true, (press) -> {
				PopupHandler.displayPopup(new FMTextInputPopup(new Color(0, 0, 0, 0), "§l" + Locals.localize("helper.creator.add.slideshow.entername.title") + ":", null, 240, this.parent::addSlideshow));
			});
			slideshowMenu.addContent(inputSlideshowButton);
			
			slideshowMenu.addSeparator();
			
			for (String s : SlideshowHandler.getSlideshowNames()) {
				String name = s;
				if (Minecraft.getInstance().fontRenderer.getStringWidth(name) > 90) {
					name = Minecraft.getInstance().fontRenderer.trimStringToWidth(name, 90) + "..";
				}
				
				AdvancedButton slideshowB = new AdvancedButton(0, 0, 0, 20, name, true, (press) -> {
					if (SlideshowHandler.slideshowExists(s)) {
						this.parent.addSlideshow(s);
					}
				});
				slideshowMenu.addContent(slideshowB);
			}

			AdvancedButton slideshowButton = new AdvancedButton(0, 0, 0, 20, Locals.localize("helper.creator.add.slideshow"), (press) -> {
				slideshowMenu.setParentButton((AdvancedButton) press);
				slideshowMenu.openMenuAt(0, press.y, screenWidth, screenHeight);
			});
			this.addContent(slideshowButton);

			/** SHAPE **/
			FMContextMenu shapesMenu = new FMContextMenu();
			shapesMenu.setAutoclose(true);
			this.addChild(shapesMenu);

			AdvancedButton addRectangleButton = new AdvancedButton(0, 0, 0, 20, Locals.localize("helper.creator.add.shapes.rectangle"), (press) -> {
				this.parent.addShape(Shape.RECTANGLE);
			});
			shapesMenu.addContent(addRectangleButton);
			
			AdvancedButton shapesButton = new AdvancedButton(0, 0, 0, 20, Locals.localize("helper.creator.add.shapes"), (press) -> {
				shapesMenu.setParentButton((AdvancedButton) press);
				shapesMenu.openMenuAt(0, press.y, screenWidth, screenHeight);
			});
			this.addContent(shapesButton);

			
			super.openMenuAt(x, y, screenWidth, screenHeight);
		}
		
	}
	
	public static class ManageAudioContextMenu extends FMContextMenu {
		
		private LayoutEditorScreen parent;
		
		public ManageAudioContextMenu(LayoutEditorScreen parent) {
			this.parent = parent;
		}
		
		@Override
		public void openMenuAt(int x, int y, int screenWidth, int screenHeight) {
			
			this.content.clear();
			
			if (this.parent.audio.isEmpty()) {
				
				AdvancedButton bt = new AdvancedButton(0, 0, 0, 16, Locals.localize("helper.creator.empty"), true, (press) -> {});
				this.addContent(bt);
				
			} else {
				
				for (Map.Entry<String, Boolean> m : this.parent.audio.entrySet()) {
					
					String label = new File(m.getKey()).getName();
					if (Minecraft.getInstance().fontRenderer.getStringWidth(label) > 200) {
						label = Minecraft.getInstance().fontRenderer.trimStringToWidth(label, 200) + "..";
					}
					
					FMContextMenu actionsMenu = new FMContextMenu();
					actionsMenu.setAutoclose(true);
					this.addChild(actionsMenu);
					
					AdvancedButton deleteButton = new AdvancedButton(0, 0, 0, 16, Locals.localize("helper.creator.audio.delete"), true, (press2) -> {
						this.closeMenu();
						PopupHandler.displayPopup(new FMYesNoPopup(300, new Color(0, 0, 0, 0), 240, (call) -> {
							if (call) {
								this.parent.audio.remove(m.getKey());
								SoundHandler.stopSound(m.getKey());
								MenuCustomization.unregisterSound(m.getKey());
							}
						}, "§c§l" + Locals.localize("helper.creator.messages.sure"), "", "", Locals.localize("helper.creator.audio.delete.msg"), "", ""));
					});
					actionsMenu.addContent(deleteButton);
					
					String lab = Locals.localize("helper.editor.ui.element.manageaudio.loop.off");
					if (m.getValue()) {
						lab = Locals.localize("helper.editor.ui.element.manageaudio.loop.on");
					}
					AdvancedButton toggleLoopButton = new AdvancedButton(0, 0, 0, 16, lab, true, (press2) -> {
						if (((AdvancedButton)press2).getMessage().equals(Locals.localize("helper.editor.ui.element.manageaudio.loop.off"))) {
							SoundHandler.setLooped(m.getKey(), true);
							this.parent.audio.put(m.getKey(), true);
							((AdvancedButton)press2).setMessage(Locals.localize("helper.editor.ui.element.manageaudio.loop.on"));;
						} else {
							SoundHandler.setLooped(m.getKey(), false);
							this.parent.audio.put(m.getKey(), false);
							((AdvancedButton)press2).setMessage(Locals.localize("helper.editor.ui.element.manageaudio.loop.off"));;
						}
					});
					actionsMenu.addContent(toggleLoopButton);
					
					AdvancedButton actionsButton = new AdvancedButton(0, 0, 0, 16, label, true, (press) -> {
						actionsMenu.setParentButton((AdvancedButton) press);
						actionsMenu.openMenuAt(0, press.y, screenWidth, screenHeight);
					});
					this.addContent(actionsButton);
					
				}
			}
			
			
			super.openMenuAt(x, y, screenWidth, screenHeight);
		}
		
	}

	public static class MultiselectContextMenu extends FMContextMenu {

		private LayoutEditorScreen parent;

		public MultiselectContextMenu(LayoutEditorScreen parent) {
			this.parent = parent;
		}
		
		@Override
		public void openMenuAt(int x, int y, int screenWidth, int screenHeight) {
			
			this.content.clear();
			
			if (this.parent.isObjectFocused()) {

				this.parent.focusedObjectsCache = this.parent.getFocusedObjects();
				
				this.parent.multiselectStretchedX = false;
				this.parent.multiselectStretchedY = false;
				
				/** DELETE ALL **/
				AdvancedButton deleteBtn = new AdvancedButton(0, 0, 0, 16, Locals.localize("helper.creator.multiselect.object.deleteall"), true, (press) -> {
					this.parent.deleteFocusedObjects();
				});
				deleteBtn.setDescription(StringUtils.splitLines(Locals.localize("helper.creator.multiselect.object.deleteall.btndesc"), "%n%"));
				this.addContent(deleteBtn);
				
				/** STRETCH ALL **/
				FMContextMenu stretchMenu = new FMContextMenu();
				stretchMenu.setAutoclose(true);
				this.addChild(stretchMenu);

				AdvancedButton stretchXBtn = new AdvancedButton(0, 0, 0, 16, Locals.localize("helper.creator.object.stretch.x"), true, (press) -> {
					this.parent.history.saveSnapshot(this.parent.history.createSnapshot());
					
					for (LayoutElement o : this.parent.focusedObjectsCache) {
						if (o.isStretchable()) {
							o.setStretchedX(!this.parent.multiselectStretchedX, false);
						}
					}
					
					this.parent.multiselectStretchedX = !this.parent.multiselectStretchedX;
					
					if (!this.parent.multiselectStretchedX) {
						press.setMessage(Locals.localize("helper.creator.object.stretch.x"));
					} else {
						press.setMessage("§a" + Locals.localize("helper.creator.object.stretch.x"));
					}

				});
				stretchMenu.addContent(stretchXBtn);
				
				AdvancedButton stretchYBtn = new AdvancedButton(0, 0, 0, 16, Locals.localize("helper.creator.object.stretch.y"), true, (press) -> {
					this.parent.history.saveSnapshot(this.parent.history.createSnapshot());
					
					for (LayoutElement o : this.parent.focusedObjectsCache) {
						if (o.isStretchable()) {
							o.setStretchedY(!this.parent.multiselectStretchedY, false);
						}
					}
					
					this.parent.multiselectStretchedY = !this.parent.multiselectStretchedY;
					
					if (!this.parent.multiselectStretchedY) {
						press.setMessage(Locals.localize("helper.creator.object.stretch.y"));
					} else {
						press.setMessage("§a" + Locals.localize("helper.creator.object.stretch.y"));
					}
					
				});
				stretchMenu.addContent(stretchYBtn);
				
				AdvancedButton stretchBtn = new AdvancedButton(0, 0, 0, 16, Locals.localize("helper.creator.multiselect.object.stretchall"), true, (press) -> {
					stretchMenu.setParentButton((AdvancedButton) press);
					stretchMenu.openMenuAt(0, press.y, screenWidth, screenHeight);
				});
				stretchBtn.setDescription(StringUtils.splitLines(Locals.localize("helper.creator.multiselect.object.stretchall.btndesc"), "%n%"));
				this.addContent(stretchBtn);
				
				/** COPY **/
				AdvancedButton copyButton = new AdvancedButton(0, 0, 0, 16, Locals.localize("helper.editor.ui.edit.copy"), (press) -> {
					this.parent.copySelectedElements();
				});
				this.addContent(copyButton);
				
				/** PASTE **/
				AdvancedButton pasteButton = new AdvancedButton(0, 0, 0, 16, Locals.localize("helper.editor.ui.edit.paste"), (press) -> {
					this.parent.pasteElements();
				});
				this.addContent(pasteButton);

				boolean allVanillaBtns = true;
				boolean allBtns = true;
				for (LayoutElement o : this.parent.focusedObjectsCache) {
					if (!(o instanceof LayoutVanillaButton)) {
						allVanillaBtns = false;
					}
					if (!(o instanceof LayoutVanillaButton) && !(o instanceof LayoutButton)) {
						allBtns = false;
					}
				}
				if (this.parent.focusedObjectsCache.isEmpty()) {
					allVanillaBtns = false;
					allBtns = false;
				}
				
				if (allVanillaBtns) {
					
					/** VANILLA: RESET ORIENTATION **/
					AdvancedButton resetOriBtn = new AdvancedButton(0, 0, 0, 16, Locals.localize("helper.creator.multiselect.vanillabutton.resetorientation"), true, (press) -> {
						this.parent.history.saveSnapshot(this.parent.history.createSnapshot());
						
						for (LayoutElement o : this.parent.focusedObjectsCache) {
							if (o instanceof LayoutVanillaButton) {
								LayoutVanillaButton vb = (LayoutVanillaButton) o;
								vb.object.orientation = "original";
								vb.object.posX = vb.button.x;
								vb.object.posY = vb.button.y;
								vb.object.width = vb.button.width;
								vb.object.height = vb.button.height;
							}
						}
						this.closeMenu();
						Minecraft.getInstance().displayGuiScreen(this.parent);
					});
					resetOriBtn.setDescription(StringUtils.splitLines(Locals.localize("helper.creator.multiselect.vanillabutton.resetorientation.btndesc"), "%n%"));
					this.addContent(resetOriBtn);

					/** VANILLA: DELETE **/
					AdvancedButton hideAllBtn = new AdvancedButton(0, 0, 0, 16, Locals.localize("helper.creator.multiselect.vanillabutton.hideall"), true, (press) -> {
						this.parent.history.saveSnapshot(this.parent.history.createSnapshot());
						this.parent.history.setPreventSnapshotSaving(true);
						
						for (LayoutElement o : this.parent.focusedObjectsCache) {
							if (o instanceof LayoutVanillaButton) {
								LayoutVanillaButton vb = (LayoutVanillaButton) o;
								this.parent.hideVanillaButton(vb);
							}
						}
						
						this.parent.focusedObjects.clear();
						this.parent.focusedObjectsCache.clear();
						this.parent.multiselectRightclickMenu.closeMenu();
						
						this.parent.history.setPreventSnapshotSaving(false);
					});
					hideAllBtn.setDescription(StringUtils.splitLines(Locals.localize("helper.creator.multiselect.vanillabutton.hideall.btndesc"), "%n%"));
					this.addContent(hideAllBtn);
					
				}
				
				if (allBtns) {
					
					/** BUTTONS: TEXTURE **/
					FMContextMenu textureMenu = new FMContextMenu();
					textureMenu.setAutoclose(true);
					this.addChild(textureMenu);

					AdvancedButton normalTextureBtn = new AdvancedButton(0, 0, 0, 16, Locals.localize("helper.creator.custombutton.config.texture.normal"), true, (press) -> {
						this.parent.setButtonTexturesForFocusedObjects(false);
					});
					textureMenu.addContent(normalTextureBtn);

					AdvancedButton hoverTextureBtn = new AdvancedButton(0, 0, 0, 16, Locals.localize("helper.creator.custombutton.config.texture.hovered"), true, (press) -> {
						this.parent.setButtonTexturesForFocusedObjects(true);
					});
					textureMenu.addContent(hoverTextureBtn);
					
					AdvancedButton resetTextureBtn = new AdvancedButton(0, 0, 0, 16, Locals.localize("helper.creator.custombutton.config.texture.reset"), true, (press) -> {
						this.parent.history.saveSnapshot(this.parent.history.createSnapshot());
						this.parent.history.setPreventSnapshotSaving(true);
						
						for (LayoutElement o : this.parent.focusedObjectsCache) {
							if (o instanceof LayoutVanillaButton) {
								LayoutVanillaButton vb = (LayoutVanillaButton) o;
								vb.backHovered = null;
								vb.backNormal = null;
								((LayoutButtonDummyCustomizationItem)o.object).setTexture(null);
								this.parent.setVanillaTexture(vb, null, null);
							} else if (o instanceof LayoutButton) {
								LayoutButton lb = (LayoutButton) o;
								lb.backHovered = null;
								lb.backNormal = null;
								((LayoutButtonDummyCustomizationItem)o.object).setTexture(null);
							}
						}
						
						this.parent.history.setPreventSnapshotSaving(false);
					});
					resetTextureBtn.setDescription(StringUtils.splitLines(Locals.localize("helper.creator.multiselect.button.buttontexture.reset.btndesc"), "%n%"));
					textureMenu.addContent(resetTextureBtn);
					
					AdvancedButton buttonTextureBtn = new AdvancedButton(0, 0, 0, 16, Locals.localize("helper.creator.custombutton.config.texture"), true, (press) -> {
						textureMenu.setParentButton((AdvancedButton) press);
						textureMenu.openMenuAt(0, press.y, screenWidth, screenHeight);
					});
					buttonTextureBtn.setDescription(StringUtils.splitLines(Locals.localize("helper.creator.multiselect.button.buttontexture.btndesc"), "%n%"));
					this.addContent(buttonTextureBtn);

					/** BUTTONS: CLICK SOUND **/
					AdvancedButton clickSoundBtn = new AdvancedButton(0, 0, 0, 16, Locals.localize("helper.creator.items.button.clicksound"), true, (press) -> {
						ChooseFilePopup cf = new ChooseFilePopup((call) -> {
							if (call != null) {
								File f = new File(call);
								if (f.exists() && f.isFile() && f.getName().endsWith(".wav")) {
									this.parent.history.saveSnapshot(this.parent.history.createSnapshot());
									this.parent.history.setPreventSnapshotSaving(true);
									
									for (LayoutElement o : this.parent.focusedObjectsCache) {
										if (o instanceof LayoutVanillaButton) {
											LayoutVanillaButton vb = (LayoutVanillaButton) o;
											vb.clicksound = call;
											this.parent.setVanillaClickSound(vb, call);
										} else if (o instanceof LayoutButton) {
											LayoutButton lb = (LayoutButton) o;
											lb.clicksound = call;
										}
									}
									
									this.parent.history.setPreventSnapshotSaving(false);
								} else {
									LayoutEditorScreen.displayNotification("§c§l" + Locals.localize("helper.creator.invalidaudio.title"), "", Locals.localize("helper.creator.invalidaudio.desc"), "", "", "", "", "", "");
								}
							}
						}, "wav");

						PopupHandler.displayPopup(cf);
					});
					clickSoundBtn.setDescription(StringUtils.splitLines(Locals.localize("helper.creator.multiselect.button.clicksound.btndesc"), "%n%"));
					this.addContent(clickSoundBtn);

					/** BUTTONS: RESET CLICK SOUND **/
					AdvancedButton resetClickSoundBtn = new AdvancedButton(0, 0, 0, 16, Locals.localize("helper.creator.items.button.clicksound.reset"), true, (press) -> {
						
						this.parent.history.saveSnapshot(this.parent.history.createSnapshot());
						this.parent.history.setPreventSnapshotSaving(true);
						
						for (LayoutElement o : this.parent.focusedObjectsCache) {
							if (o instanceof LayoutVanillaButton) {
								LayoutVanillaButton vb = (LayoutVanillaButton) o;
								vb.clicksound = null;
								this.parent.setVanillaClickSound(vb, null);
							} else if (o instanceof LayoutButton) {
								LayoutButton lb = (LayoutButton) o;
								lb.clicksound = null;
							}
						}
						
						this.parent.history.setPreventSnapshotSaving(false);
						
					});
					resetClickSoundBtn.setDescription(StringUtils.splitLines(Locals.localize("helper.creator.multiselect.button.clicksound.reset.btndesc"), "%n%"));
					this.addContent(resetClickSoundBtn);
					
					/** BUTTONS: HOVER SOUND **/
					AdvancedButton hoverSoundBtn = new AdvancedButton(0, 0, 0, 16, Locals.localize("helper.creator.items.button.hoversound"), true, (press) -> {
						ChooseFilePopup cf = new ChooseFilePopup((call) -> {
							if (call != null) {
								File f = new File(call);
								if (f.exists() && f.isFile() && f.getName().endsWith(".wav")) {
									this.parent.history.saveSnapshot(this.parent.history.createSnapshot());
									this.parent.history.setPreventSnapshotSaving(true);
									
									for (LayoutElement o : this.parent.focusedObjectsCache) {
										if (o instanceof LayoutVanillaButton) {
											LayoutVanillaButton vb = (LayoutVanillaButton) o;
											vb.hoverSound = call;
											this.parent.setVanillaHoverSound(vb, call);
										} else if (o instanceof LayoutButton) {
											LayoutButton lb = (LayoutButton) o;
											lb.hoverSound = call;
										}
									}
									
									this.parent.history.setPreventSnapshotSaving(false);
								} else {
									LayoutEditorScreen.displayNotification("§c§l" + Locals.localize("helper.creator.invalidaudio.title"), "", Locals.localize("helper.creator.invalidaudio.desc"), "", "", "", "", "", "");
								}
							}
						}, "wav");

						PopupHandler.displayPopup(cf);
					});
					hoverSoundBtn.setDescription(StringUtils.splitLines(Locals.localize("helper.creator.multiselect.button.hoversound.btndesc"), "%n%"));
					this.addContent(hoverSoundBtn);

					/** BUTTONS: RESET HOVERSOUND **/
					AdvancedButton resetHoverSoundBtn = new AdvancedButton(0, 0, 0, 16, Locals.localize("helper.creator.items.button.hoversound.reset"), true, (press) -> {
						
						this.parent.history.saveSnapshot(this.parent.history.createSnapshot());
						this.parent.history.setPreventSnapshotSaving(true);
						
						for (LayoutElement o : this.parent.focusedObjectsCache) {
							if (o instanceof LayoutVanillaButton) {
								LayoutVanillaButton vb = (LayoutVanillaButton) o;
								vb.hoverSound = null;
								this.parent.setVanillaHoverSound(vb, null);
							} else if (o instanceof LayoutButton) {
								LayoutButton lb = (LayoutButton) o;
								lb.hoverSound = null;
							}
						}
						
						this.parent.history.setPreventSnapshotSaving(false);
						
					});
					resetHoverSoundBtn.setDescription(StringUtils.splitLines(Locals.localize("helper.creator.multiselect.button.hoversound.reset.btndesc"), "%n%"));
					this.addContent(resetHoverSoundBtn);
				}
				
			}
			
			
			super.openMenuAt(x, y, screenWidth, screenHeight);
		}

	}
	
	public static class HiddenVanillaButtonContextMenu extends FMContextMenu {

		private LayoutEditorScreen parent;

		public HiddenVanillaButtonContextMenu(LayoutEditorScreen parent) {
			this.parent = parent;
		}
		
		@Override
		public void openMenuAt(int x, int y, int screenWidth, int screenHeight) {
			
			this.content.clear();
			this.separators.clear();
			
			if (this.parent.hidden.isEmpty()) {
				AdvancedButton emptyButton = new AdvancedButton(0, 0, 0, 16, Locals.localize("helper.creator.empty"), true, (press) -> {});
				this.addContent(emptyButton);
			} else {
				for (LayoutVanillaButton b : this.parent.hidden) {
					
					String name = b.button.getButton().getMessage();
					
					AdvancedButton hiddenButton = new AdvancedButton(0, 0, 0, 0, name, true, (press) -> {
						this.parent.showVanillaButton(b);
						this.closeMenu();
					});
					hiddenButton.setDescription(StringUtils.splitLines(Locals.localize("helper.editor.ui.element.deletedvanillabuttons.entry.desc"), "%n%"));
					this.addContent(hiddenButton);
					
				}
			}
			
			super.openMenuAt(x, y, screenWidth, screenHeight);
		}

	}

}