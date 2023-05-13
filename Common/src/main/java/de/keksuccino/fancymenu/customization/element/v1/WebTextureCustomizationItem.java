package de.keksuccino.fancymenu.customization.element.v1;

import java.awt.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.systems.RenderSystem;

import com.mojang.blaze3d.vertex.PoseStack;
import de.keksuccino.fancymenu.customization.element.AbstractElement;
import de.keksuccino.fancymenu.customization.placeholder.v2.PlaceholderParser;
import de.keksuccino.fancymenu.threading.MainThreadTaskExecutor;
import de.keksuccino.konkrete.annotations.OptifineFix;
import de.keksuccino.konkrete.input.StringUtils;
import de.keksuccino.fancymenu.properties.PropertyContainer;
import de.keksuccino.konkrete.rendering.RenderUtils;
import de.keksuccino.fancymenu.rendering.texture.ExternalTextureHandler;
import de.keksuccino.konkrete.resources.WebTextureResourceLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WebTextureCustomizationItem extends AbstractElement {

	private static final Logger LOGGER = LogManager.getLogger();

	public static Map<String, WebTextureResourceLocation> cachedWebImages = new HashMap<>();

	public volatile WebTextureResourceLocation texture;
	public String rawURL = "";
	public volatile boolean ready = false;

	@OptifineFix
	//FIX: Web textures need to be loaded in the main thread if OF is installed
	public WebTextureCustomizationItem(PropertyContainer item) {
		super(item);

		if ((this.elementType != null) && this.elementType.equalsIgnoreCase("addwebtexture")) {
			this.value = item.getValue("url");
			if (this.value != null) {
				this.rawURL = this.value;
				this.value = StringUtils.convertFormatCodes(PlaceholderParser.replacePlaceholders(this.value), "§", "&");

				if ((this.getWidth() <= 0) && (this.getHeight() <= 0)) {
					this.setWidth(100);
				}

				if (cachedWebImages.containsKey(this.instanceIdentifier)) {
					this.texture = cachedWebImages.get(this.instanceIdentifier);
					this.calculateAspectRatio();
					
					if ((this.texture != null) && (this.texture.getResourceLocation() != null) && (this.texture.getURL() != null) && this.texture.getURL().equals(this.value)) {
						this.ready = true;
					} else {
						this.texture = null;
					}
				}

				if (this.texture == null) {
					new Thread(() -> {
						try {

							if (isValidUrl(this.value)) {

								this.texture = ExternalTextureHandler.INSTANCE.getWebTexture(this.value, false);
								MainThreadTaskExecutor.executeInMainThread(() -> {
									try {
										texture.loadTexture();
										cachedWebImages.put(this.instanceIdentifier, this.texture);
									} catch (Exception e) {
										e.printStackTrace();
									}
								}, MainThreadTaskExecutor.ExecuteTiming.POST_CLIENT_TICK);

								//Wait for the texture to load
								long startTime = System.currentTimeMillis();
								while (true) {
									long currentTime = System.currentTimeMillis();
									if ((startTime+15000) < currentTime) {
										break;
									}
									if (texture.isReady()) {
										if (texture.getResourceLocation() != null) {
											break;
										}
									}
									try {
										Thread.sleep(100);
									} catch (Exception e) {}
								}

								if ((this.texture != null) && (texture.getResourceLocation() == null)) {
									this.texture = null;
									LOGGER.error("[FANCYMENU] Web texture loaded but resource location was NULL! Unable to use web texture!");
								}

								this.calculateAspectRatio();
							}

							this.ready = true;

						} catch (Exception e) {
							e.printStackTrace();
						}
					}).start();
				}
				
			}
		}

	}

	protected void calculateAspectRatio() {
		if ((this.texture == null) || !this.texture.isReady()) {
			if (this.getWidth() <= 0) {
				this.setWidth(100);
			}
			if (this.getHeight() <= 0) {
				this.setHeight(100);
			}
			this.ready = true;
			return;
		}
		int w = this.texture.getWidth();
		int h = this.texture.getHeight();
		double ratio = (double) w / (double) h;
		//Calculate missing width
		if ((this.getWidth() < 0) && (this.getHeight() >= 0)) {
			this.setWidth((int)(this.getHeight() * ratio));
		}
		//Calculate missing height
		if ((this.getHeight() < 0) && (this.getWidth() >= 0)) {
			this.setHeight((int)(this.getWidth() / ratio));
		}
	}

	@Override
	public void render(PoseStack matrix, Screen menu) throws IOException {
		if (this.shouldRender() || isEditor()) {

			int x = this.getX(menu);
			int y = this.getY(menu);

			if (this.isTextureReady()) {
				RenderUtils.bindTexture(this.texture.getResourceLocation());
				RenderSystem.enableBlend();
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.opacity);
				blit(matrix, x, y, 0.0F, 0.0F, this.getWidth(), this.getHeight(), this.getWidth(), this.getHeight());
				RenderSystem.disableBlend();
			} else if (isEditor()) {
				fill(matrix, this.getX(menu), this.getY(menu), this.getX(menu) + this.getWidth(), this.getY(menu) + this.getHeight(), Color.MAGENTA.getRGB());
				if (this.ready) {
					drawCenteredString(matrix, Minecraft.getInstance().font, "§lMISSING", this.getX(menu) + (this.getWidth() / 2), this.getY(menu) + (this.getHeight() / 2) - (Minecraft.getInstance().font.lineHeight / 2), -1);
				}
			}

			if (!this.ready && isEditor()) {
				drawCenteredString(matrix, Minecraft.getInstance().font, "§lLOADING TEXTURE..", this.getX(menu) + (this.getWidth() / 2), this.getY(menu) + (this.getHeight() / 2) - (Minecraft.getInstance().font.lineHeight / 2), -1);
			}

		}
	}

	public boolean isTextureReady() {
		return ((this.texture != null) && (this.texture.isReady()) && (this.texture.getResourceLocation() != null) && this.ready);
	}

	@Override
	public boolean shouldRender() {
		if ((this.getWidth() < 0) || (this.getHeight() < 0)) {
			return false;
		}
		return super.shouldRender();
	}

	public static boolean isValidUrl(String url) {
		if ((url != null) && (url.startsWith("http://") || url.startsWith("https://"))) {
			try {
				URL u = new URL(url);
				HttpURLConnection c = (HttpURLConnection)u.openConnection();
				c.addRequestProperty("User-Agent", "Mozilla/4.0");
				c.setRequestMethod("HEAD");
				int r = c.getResponseCode();
				if (r == 200) {
					return true;
				}
			} catch (Exception e1) {
				try {
					URL u = new URL(url);
					HttpURLConnection c = (HttpURLConnection)u.openConnection();
					c.addRequestProperty("User-Agent", "Mozilla/4.0");
					int r = c.getResponseCode();
					if (r == 200) {
						return true;
					}
				} catch (Exception e2) {}
			}
			return false;
		}
		return false;
	}

}
