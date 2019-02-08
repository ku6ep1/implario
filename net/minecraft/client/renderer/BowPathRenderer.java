package net.minecraft.client.renderer;

import net.minecraft.client.MC;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.simulant.Simulant;
import net.minecraft.entity.projectile.simulant.SimulantArrow;
import net.minecraft.entity.projectile.simulant.SimulantSimpleProjectile;
import net.minecraft.init.Items;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;

public class BowPathRenderer {

	private static volatile Simulant lastParsed;

	public static void render(float partialTicks) {
		EntityPlayer player = MC.getPlayer();
		if (player.getHeldItem() == null) {
			lastParsed = null;
			return;
		}

		Simulant g, s;
		float f = 1;

		if (player.getHeldItem().getItem() == Items.bow) {

			int ik = 72000 - player.itemInUseCount;
			f = (float) ik / 20.0F;
			f = (f * f + f * 2.0F) / 3.0F;

			if ((double) f < 0.1D) {
				lastParsed = null;
				return;
			}

			if (f > 1.0F) f = 1.0F;

			g = new SimulantArrow(player.worldObj, player, f * 2, 0);
			s = new SimulantArrow(player.worldObj, player, f * 2, 1);

		} else if (player.getHeldItem().getItem() == Items.snowball ||
					player.getHeldItem().getItem() == Items.ender_pearl ||
					player.getHeldItem().getItem() == Items.egg) {
			g = new SimulantSimpleProjectile(player.worldObj, player, 0);
			s = new SimulantSimpleProjectile(player.worldObj, player, 1);
			f = 0.3F;
		} else {
			lastParsed = null;
			return;
		}


		double x = player.posX;
		double y = player.posY;
		double z = player.posZ;

		if (player == null) {
			lastParsed = null;
			return;
		}
		Vec3 vec3 = new Vec3(-0.36D, 0.03D, 0.35D);
		vec3 = vec3.rotatePitch(-(player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks) * (float) Math.PI / 180.0F);
		vec3 = vec3.rotateYaw(-(player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * partialTicks) * (float) Math.PI / 180.0F);
		double d0 = player.prevPosX + (player.posX - player.prevPosX) * (double) partialTicks + vec3.xCoord;
		double d1 = player.prevPosY + (player.posY - player.prevPosY) * (double) partialTicks + vec3.yCoord;
		double d2 = player.prevPosZ + (player.posZ - player.prevPosZ) * (double) partialTicks + vec3.zCoord;
		double d3 = (double) player.getEyeHeight();

		GlStateManager.disableTexture2D();
		GlStateManager.disableLighting();

		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		worldrenderer.begin(3, DefaultVertexFormats.POSITION_COLOR);



		for (int i = 0; i < 300; i++) {
			double aX = g.posX, aY = g.posY, aZ = g.posZ;
			worldrenderer.pos(aX - x, aY - y, aZ - z).color((int) (200 * f), (int) (200 * (1 - f)), 0, 255).endVertex();
			g.onUpdate();
			s.onUpdate();
			if (g.destinated) break;
		}

		double dx = g.posX - s.posX;
		double dy = g.posY - s.posY;
		double dz = g.posZ - s.posZ;
		g.inacc = (short) (Math.sqrt(dx * dx + dy * dy + dz * dz) * 10);

		tessellator.draw();
		Entity e = g.entityHit;
		if (e != null) {
			MC.i().getRenderManager().renderDebugBoundingBox(e, e.posX - x, e.posY - y, e.posZ - z, e.rotationYaw, partialTicks);
		} else {
			x = g.posX - x; y = g.posY - y; z = g.posZ - z;
			float ss = 0.3F;
			worldrenderer.begin(3, DefaultVertexFormats.POSITION_COLOR);
			worldrenderer.pos(x + ss, y + ss, z + ss).color(200, 0,0 , 255).endVertex();
			worldrenderer.pos(x - ss, y + ss, z + ss).color(200, 0,0 , 255).endVertex();
			worldrenderer.pos(x - ss, y + ss, z - ss).color(200, 0,0 , 255).endVertex();
			worldrenderer.pos(x + ss, y + ss, z - ss).color(200, 0,0 , 255).endVertex();
			worldrenderer.pos(x + ss, y + ss, z + ss).color(200, 0,0 , 255).endVertex();
			worldrenderer.pos(x + ss, y - ss, z + ss).color(200, 0,0 , 255).endVertex();
			worldrenderer.pos(x - ss, y - ss, z + ss).color(200, 0,0 , 255).endVertex();
			worldrenderer.pos(x - ss, y + ss, z + ss).color(200, 0,0 , 255).endVertex();
			worldrenderer.pos(x - ss, y - ss, z + ss).color(200, 0,0 , 255).endVertex();
			worldrenderer.pos(x - ss, y - ss, z - ss).color(200, 0,0 , 255).endVertex();
			worldrenderer.pos(x - ss, y + ss, z - ss).color(200, 0,0 , 255).endVertex();
			worldrenderer.pos(x - ss, y - ss, z - ss).color(200, 0,0 , 255).endVertex();
			worldrenderer.pos(x + ss, y - ss, z - ss).color(200, 0,0 , 255).endVertex();
			worldrenderer.pos(x + ss, y + ss, z - ss).color(200, 0,0 , 255).endVertex();
			worldrenderer.pos(x + ss, y - ss, z - ss).color(200, 0,0 , 255).endVertex();
			worldrenderer.pos(x + ss, y - ss, z + ss).color(200, 0,0 , 255).endVertex();
			tessellator.draw();
		}
		lastParsed = g;
		GlStateManager.enableLighting();
		GlStateManager.enableTexture2D();
	}

	public static void renderOverlay(int x, int y) {
		if (lastParsed == null) return;
		if (MC.getPlayer() == null) return;
		FontRenderer f = MC.getFontRenderer();
		BlockPos l = MC.getPlayer().getPosition();
		if (f == null || l == null) return;
		GlStateManager.scale(2, 2, 2);
		double sqrt = Math.sqrt(l.distanceSq(new Vec3i(lastParsed.posX, lastParsed.posY, lastParsed.posZ)));
		Gui.drawRect(x - 2, y - 1, x + 65, y + 15, 0x50202020);
		f.drawString("Расстояние: §a" + (double) (int) (sqrt * 10) / 10, x, y, 0xffffff);
		String target = lastParsed.entityHit == null ? lastParsed.inTile == null ? "§7-" : "§e" + lastParsed.inTile.getLocalizedName() : "§a" + lastParsed.entityHit.getName();
		GlStateManager.scale(0.5, 0.5, 0.5);
		f.drawString("Цель: " + target, x * 2, y * 2 + 19, 0xffffff);
		f.drawString("Точность: §e" + (float) lastParsed.inacc / 10F, x * 2, y * 2 + 28, 0xffffff);
	}

}
