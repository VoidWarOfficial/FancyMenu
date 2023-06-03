//package de.keksuccino.fancymenu.customization.element.v1;
//
//import java.awt.Color;
//import java.io.File;
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import com.mojang.blaze3d.systems.RenderSystem;
//
//import com.mojang.blaze3d.vertex.PoseStack;
//import com.mojang.math.Axis;
//import de.keksuccino.fancymenu.customization.element.AbstractElement;
//import de.keksuccino.fancymenu.customization.placeholder.PlaceholderParser;
//import de.keksuccino.fancymenu.event.acara.EventHandler;
//import de.keksuccino.fancymenu.event.acara.EventListener;
//import de.keksuccino.fancymenu.event.events.screen.InitOrResizeScreenEvent;
//import de.keksuccino.fancymenu.event.events.ModReloadEvent;
//import de.keksuccino.konkrete.file.FileUtils;
//import de.keksuccino.fancymenu.utils.LocalizationUtils;
//import de.keksuccino.konkrete.math.MathUtils;
//import de.keksuccino.fancymenu.properties.PropertyContainer;
//import de.keksuccino.konkrete.rendering.RenderUtils;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.Font;
//import net.minecraft.client.gui.screens.Screen;
//import net.minecraft.util.Mth;
//
//public class SplashTextCustomizationItem extends AbstractElement {
//
//	protected static Map<String, String> splashCache = new HashMap<String, String>();
//	protected static boolean init = false;
//
//	public float scale = 1.0F;
//	public boolean shadow = true;
//	public boolean bounce = true;
//	public float rotation = 20.0F;
//	public Color basecolor = new Color(255, 255, 0);
//	public String basecolorString = "#ffff00";
//	public boolean refreshOnMenuReload = false;
//	public File splashfile;
//	public String text = null;
//	public boolean vanillaLike = false;
//
//	protected float basescale = 1.8F;
//
//	protected static boolean isNewMenu = false;
//	protected boolean isNewMenuThis = false;
//	protected static Screen lastScreen = null;
//
//	protected static Map<String, String> vanillaLikeCache = new HashMap<>();
//
//	public SplashTextCustomizationItem(PropertyContainer item) {
//		super(item);
//
//		if (!init) {
//			EventHandler.INSTANCE.registerListenersOf(SplashTextCustomizationItem.class);
//			init = true;
//		}
//
//		if ((this.elementType != null) && this.elementType.equalsIgnoreCase("addsplash")) {
//
//			String vanillaLikeString = item.getValue("vanilla-like");
//			if ((vanillaLikeString != null) && vanillaLikeString.equals("true")) {
//				this.vanillaLike = true;
//			}
//
//			String filepath = fixBackslashPath(item.getValue("splashfilepath"));
//			if (filepath != null) {
//				this.splashfile = new File(filepath);
//				if (!this.splashfile.exists() || !this.splashfile.getAbsolutePath().replace("\\", "/").startsWith(Minecraft.getInstance().gameDirectory.getAbsolutePath().replace("\\", "/"))) {
//					filepath = Minecraft.getInstance().gameDirectory.getAbsolutePath().replace("\\", "/") + "/" + filepath;
//					this.splashfile = new File(filepath);
//				}
//				if (!this.splashfile.exists() || !this.splashfile.getPath().toLowerCase().endsWith(".txt")) {
//					this.splashfile = null;
//				}
//			}
//
//			if (!this.vanillaLike) {
//				this.text = item.getValue("text");
//			} else if (vanillaLikeCache.containsKey(this.instanceIdentifier)) {
//				this.text = vanillaLikeCache.get(this.instanceIdentifier);
//			}
//
//			String ro = item.getValue("rotation");
//			if ((ro != null) && MathUtils.isFloat(ro)) {
//				this.rotation = Float.parseFloat(ro);
//			}
//
//			String re = item.getValue("refresh");
//			if ((re != null) && re.equalsIgnoreCase("true")) {
//				this.refreshOnMenuReload = true;
//			}
//
//			String co = item.getValue("basecolor");
//			if (co != null) {
//				Color c = RenderUtils.getColorFromHexString(co);
//				if (c != null) {
//					this.basecolor = c;
//					this.basecolorString = co;
//				}
//			}
//
//			String sh = item.getValue("shadow");
//			if ((sh != null)) {
//				if (sh.equalsIgnoreCase("false")) {
//					this.shadow = false;
//				}
//			}
//
//			String sc = item.getValue("scale");
//			if ((sc != null) && MathUtils.isFloat(sc)) {
//				this.scale = Float.parseFloat(sc);
//			}
//
//			String b = item.getValue("bouncing");
//			if ((b != null) && b.equalsIgnoreCase("false")) {
//				this.bounce = false;
//			}
//
//			this.value = "splash text";
//
//			this.setWidth((int) (30 * basescale * this.scale));
//			this.setHeight((int) (10 * basescale * this.scale));
//
//		}
//	}
//
//	public void render(PoseStack matrix, Screen menu) throws IOException {
//
//		if (this.isNewMenuThis) {
//			isNewMenu = false;
//		}
//		this.isNewMenuThis = isNewMenu;
//
//		this.setWidth((int) (30 * basescale * this.scale));
//		this.setHeight((int) (10 * basescale * this.scale));
//
//		if (this.shouldRender()) {
//
//			this.renderSplash(matrix, Minecraft.getInstance().font, menu);
//
//		}
//
//	}
//
//	protected void renderSplash(PoseStack matrix, Font font, Screen s) {
//
//		String splash = null;
//
//		if (this.vanillaLike && (this.text == null)) {
//			this.text = Minecraft.getInstance().getSplashManager().getSplash();
//			vanillaLikeCache.put(this.instanceIdentifier, this.text);
//		}
//
//		if ((this.splashfile != null) && (this.text == null)) {
//
//			if (isNewMenu && this.refreshOnMenuReload) {
//				splashCache.remove(this.getInstanceIdentifier());
//			}
//
//			if (!splashCache.containsKey(this.getInstanceIdentifier())) {
//				List<String> l = FileUtils.getFileLines(this.splashfile);
//				if (!l.isEmpty()) {
//					int i = MathUtils.getRandomNumberInRange(0, l.size()-1);
//					splashCache.put(this.getInstanceIdentifier(), l.get(i));
//				}
//			}
//
//			if (splashCache.containsKey(this.getInstanceIdentifier())) {
//				splash = splashCache.get(this.getInstanceIdentifier());
//			}
//
//		}
//
//		if (this.text != null) {
//			splash = this.text;
//		}
//
//		if (splash != null) {
//
//			if (this.value != null) {
//				if (!isEditor()) {
//					splash = PlaceholderParser.replacePlaceholders(splash);
//				} else {
//					splash = StringUtils.convertFormatCodes(splash, "&", "§");
//				}
//			}
//
//			this.value = splash;
//
//			float f = basescale;
//			if (this.bounce) {
//				f = f - Mth.abs(Mth.sin((float) (System.currentTimeMillis() % 1000L) / 1000.0F * ((float) Math.PI * 2F)) * 0.1F);
//			}
//			f = f * 100.0F / (float) (font.width(splash) + 32);
//
//			RenderSystem.enableBlend();
//
//			matrix.pushPose();
//			matrix.scale(this.scale, this.scale, this.scale);
//
//			matrix.pushPose();
//			matrix.translate(((this.getX(s) + (this.getWidth() / 2)) / this.scale), this.getY(s) / this.scale, 0.0F);
//			matrix.mulPose(Axis.ZP.rotationDegrees(this.rotation));
//			matrix.scale(f, f, f);
//
//			int alpha = this.basecolor.getAlpha();
//			int i = Mth.ceil(this.opacity * 255.0F);
//			if (i < alpha) {
//				alpha = i;
//			}
//			Color c = new Color(this.basecolor.getRed(), this.basecolor.getGreen(), this.basecolor.getBlue(), alpha);
//
//			if (this.shadow) {
//				font.drawShadow(matrix, splash, -(font.width(splash) / 2), 0, c.getRGB());
//			} else {
//				font.draw(matrix, splash, -(font.width(splash) / 2), 0, c.getRGB());
//			}
//
//			matrix.popPose();
//			matrix.popPose();
//
//		}
//
//	}
//
//
//	@EventListener
//	public static void onInitScreenPre(InitOrResizeScreenEvent.Pre e) {
//		Screen s = Minecraft.getInstance().screen;
//		if (s != null) {
//			if ((lastScreen == null) || !lastScreen.getClass().getName().equals(s.getClass().getName())) {
//				isNewMenu = true;
//			}
//		}
//		lastScreen = s;
//	}
//
//	@EventListener
//	public static void onMenuReloaded(ModReloadEvent e) {
//		splashCache.clear();
//	}
//
//}