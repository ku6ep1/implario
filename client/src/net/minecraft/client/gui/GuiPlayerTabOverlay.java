package net.minecraft.client.gui;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.mojang.authlib.GameProfile;
import net.minecraft.Utils;
import net.minecraft.client.MC;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ingame.GuiIngame;
import net.minecraft.client.network.protocol.minecraft_47.NetHandlerPlayClient;
import net.minecraft.client.network.protocol.minecraft_47.NetworkPlayerInfo;
import net.minecraft.client.renderer.G;
import net.minecraft.entity.player.Player;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldSettings;

import java.util.Comparator;
import java.util.List;

public class GuiPlayerTabOverlay extends Gui {

	private static final Ordering<NetworkPlayerInfo> players = Ordering.from(new GuiPlayerTabOverlay.PlayerComparator());
	private final Minecraft mc;
	private final GuiIngame guiIngame;
	private IChatComponent footer;
	private IChatComponent header;
	private long lastTimeOpened;
	private boolean isBeingRendered;

	public GuiPlayerTabOverlay(Minecraft mcIn, GuiIngame guiIngameIn) {
		this.mc = mcIn;
		this.guiIngame = guiIngameIn;
	}

	public String getPlayerName(NetworkPlayerInfo networkPlayerInfoIn) {
		return networkPlayerInfoIn.getDisplayName() != null ? networkPlayerInfoIn.getDisplayName().getFormattedText() : ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(),
				networkPlayerInfoIn.getGameProfile().getName());
	}

	public void updatePlayerList(boolean willBeRendered) {
		if (willBeRendered && !this.isBeingRendered) this.lastTimeOpened = Minecraft.getSystemTime();
		this.isBeingRendered = willBeRendered;
	}

	public void renderPlayerlist(int width, Scoreboard sb, ScoreObjective ob) {
		if (Utils.implarioServer) drawCenteredString(MC.FR, "implario-server", width / 2, 1, -1);
		NetHandlerPlayClient nhpc = this.mc.thePlayer.sendQueue;
		List<NetworkPlayerInfo> list = players.sortedCopy(nhpc.getPlayerInfoMap());
		int i = 0;
		int j = 0;

		for (NetworkPlayerInfo networkplayerinfo : list) {
			int k = this.mc.fontRenderer.getStringWidth(this.getPlayerName(networkplayerinfo));
			i = Math.max(i, k);

			if (ob != null && ob.getRenderType() != IScoreObjectiveCriteria.EnumRenderType.HEARTS) {
				k = this.mc.fontRenderer.getStringWidth(" " + sb.getValueFromObjective(networkplayerinfo.getGameProfile().getName(), ob).getScorePoints());
				j = Math.max(j, k);
			}
		}

		list = list.subList(0, Math.min(list.size(), 80));
		int l3 = list.size();
		int i4 = l3;
		int j4;

		for (j4 = 1; i4 > 20; i4 = (l3 + j4 - 1) / j4) ++j4;

		boolean flag = true;//this.mc.isIntegratedServerRunning() || this.mc.getNetHandler().getNetworkManager().getIsencrypted();
		int l;

		if (ob != null)
			if (ob.getRenderType() == IScoreObjectiveCriteria.EnumRenderType.HEARTS) l = 90;
			else l = j;
		else l = 0;

		int i1 = Math.min(j4 * ((flag ? 9 : 0) + i + l + 13), width - 50) / j4;
		int j1 = width / 2 - (i1 * j4 + (j4 - 1) * 5) / 2;
		int k1 = 10;
		int l1 = i1 * j4 + (j4 - 1) * 5;
		List<String> list1 = null;
		List<String> list2 = null;

		if (this.header != null) {
			list1 = this.mc.fontRenderer.listFormattedStringToWidth(this.header.getFormattedText(), width - 50);

			for (String s : list1) l1 = Math.max(l1, this.mc.fontRenderer.getStringWidth(s));
		}

		if (this.footer != null) {
			list2 = this.mc.fontRenderer.listFormattedStringToWidth(this.footer.getFormattedText(), width - 50);

			for (String s2 : list2) l1 = Math.max(l1, this.mc.fontRenderer.getStringWidth(s2));
		}

		if (list1 != null) {
			drawRect(width / 2 - l1 / 2 - 1, k1 - 1, width / 2 + l1 / 2 + 1, k1 + list1.size() * this.mc.fontRenderer.getFontHeight(), Integer.MIN_VALUE);

			for (String s3 : list1) {
				int i2 = this.mc.fontRenderer.getStringWidth(s3);
				this.mc.fontRenderer.drawStringWithShadow(s3, (float) (width / 2 - i2 / 2), (float) k1, -1);
				k1 += this.mc.fontRenderer.getFontHeight();
			}

			++k1;
		}

		drawRect(width / 2 - l1 / 2 - 1, k1 - 1, width / 2 + l1 / 2 + 1, k1 + i4 * 9, Integer.MIN_VALUE);

		for (int k4 = 0; k4 < l3; ++k4) {
			int l4 = k4 / i4;
			int i5 = k4 % i4;
			int j2 = j1 + l4 * i1 + l4 * 5;
			int k2 = k1 + i5 * 9;
			drawRect(j2, k2, j2 + i1, k2 + 8, 553648127);
			G.color(1.0F, 1.0F, 1.0F, 1.0F);
			G.enableAlpha();
			G.enableBlend();
			G.tryBlendFuncSeparate(770, 771, 1, 0);

			if (k4 < list.size()) {
				NetworkPlayerInfo networkplayerinfo1 = list.get(k4);
				String s1 = this.getPlayerName(networkplayerinfo1);
				GameProfile gameprofile = networkplayerinfo1.getGameProfile();

				if (flag) {
					Player entityplayer = this.mc.theWorld.getPlayerEntityByUUID(gameprofile.getId());
					boolean flag1 = entityplayer != null && entityplayer.isWearing(EnumPlayerModelParts.CAPE) && (gameprofile.getName().equals("Dinnerbone") || gameprofile.getName().equals("Grumm"));
					this.mc.getTextureManager().bindTexture(networkplayerinfo1.getLocationSkin());
					int l2 = 8 + (flag1 ? 8 : 0);
					int i3 = 8 * (flag1 ? -1 : 1);
					Gui.drawScaledCustomSizeModalRect(j2, k2, 8.0F, (float) l2, 8, i3, 8, 8, 64.0F, 64.0F);

					if (entityplayer != null && entityplayer.isWearing(EnumPlayerModelParts.HAT)) {
						int j3 = 8 + (flag1 ? 8 : 0);
						int k3 = 8 * (flag1 ? -1 : 1);
						Gui.drawScaledCustomSizeModalRect(j2, k2, 40.0F, (float) j3, 8, k3, 8, 8, 64.0F, 64.0F);
					}

					j2 += 9;
				}

				if (networkplayerinfo1.getGameType() == WorldSettings.GameType.SPECTATOR) {
					s1 = EnumChatFormatting.ITALIC + s1;
					this.mc.fontRenderer.drawStringWithShadow(s1, (float) j2, (float) k2, -1862270977);
				} else this.mc.fontRenderer.drawStringWithShadow(s1, (float) j2, (float) k2, -1);

				if (ob != null && networkplayerinfo1.getGameType() != WorldSettings.GameType.SPECTATOR) {
					int k5 = j2 + i + 1;
					int l5 = k5 + l;

					if (l5 - k5 > 5) this.drawScoreboardValues(ob, k2, gameprofile.getName(), k5, l5, networkplayerinfo1);
				}

				this.drawPing(i1, j2 - (flag ? 9 : 0), k2, networkplayerinfo1);
			}
		}

		if (list2 != null) {
			k1 = k1 + i4 * 9 + 1;
			drawRect(width / 2 - l1 / 2 - 1, k1 - 1, width / 2 + l1 / 2 + 1, k1 + list2.size() * this.mc.fontRenderer.getFontHeight(), Integer.MIN_VALUE);

			for (String s4 : list2) {
				int j5 = this.mc.fontRenderer.getStringWidth(s4);
				this.mc.fontRenderer.drawStringWithShadow(s4, (float) (width / 2 - j5 / 2), (float) k1, -1);
				k1 += this.mc.fontRenderer.getFontHeight();
			}
		}
	}

	protected void drawPing(int p_175245_1_, int p_175245_2_, int p_175245_3_, NetworkPlayerInfo networkPlayerInfoIn) {
		G.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(icons);
		int i = 0;
		int j;

		if (networkPlayerInfoIn.getResponseTime() < 0) j = 5;
		else if (networkPlayerInfoIn.getResponseTime() < 150) j = 0;
		else if (networkPlayerInfoIn.getResponseTime() < 300) j = 1;
		else if (networkPlayerInfoIn.getResponseTime() < 600) j = 2;
		else if (networkPlayerInfoIn.getResponseTime() < 1000) j = 3;
		else j = 4;

		this.zLevel += 100.0F;
		this.drawTexturedModalRect(p_175245_2_ + p_175245_1_ - 11, p_175245_3_, 0 + i * 10, 176 + j * 8, 10, 8);
		this.zLevel -= 100.0F;
	}

	private void drawScoreboardValues(ScoreObjective ob, int p_175247_2_, String p_175247_3_, int p_175247_4_, int p_175247_5_, NetworkPlayerInfo player) {
		int i = ob.getScoreboard().getValueFromObjective(p_175247_3_, ob).getScorePoints();

		if (ob.getRenderType() == IScoreObjectiveCriteria.EnumRenderType.HEARTS) {
			this.mc.getTextureManager().bindTexture(icons);

			if (this.lastTimeOpened == player.func_178855_p()) if (i < player.func_178835_l()) {
				player.func_178846_a(Minecraft.getSystemTime());
				player.func_178844_b((long) (this.guiIngame.getUpdateCounter() + 20));
			} else if (i > player.func_178835_l()) {
				player.func_178846_a(Minecraft.getSystemTime());
				player.func_178844_b((long) (this.guiIngame.getUpdateCounter() + 10));
			}

			if (Minecraft.getSystemTime() - player.func_178847_n() > 1000L || this.lastTimeOpened != player.func_178855_p()) {
				player.func_178836_b(i);
				player.func_178857_c(i);
				player.func_178846_a(Minecraft.getSystemTime());
			}

			player.func_178843_c(this.lastTimeOpened);
			player.func_178836_b(i);
			int j = MathHelper.ceiling_float_int((float) Math.max(i, player.func_178860_m()) / 2.0F);
			int k = Math.max(MathHelper.ceiling_float_int((float) (i / 2)), Math.max(MathHelper.ceiling_float_int((float) (player.func_178860_m() / 2)), 10));
			boolean flag = player.func_178858_o() > (long) this.guiIngame.getUpdateCounter() && (player.func_178858_o() - (long) this.guiIngame.getUpdateCounter()) / 3L % 2L == 1L;

			if (j > 0) {
				float f = Math.min((float) (p_175247_5_ - p_175247_4_ - 4) / (float) k, 9.0F);

				if (f > 3.0F) {
					for (int l = j; l < k; ++l) this.drawTexturedModalRect((float) p_175247_4_ + (float) l * f, (float) p_175247_2_, flag ? 25 : 16, 0, 9, 9);

					for (int j1 = 0; j1 < j; ++j1) {
						this.drawTexturedModalRect((float) p_175247_4_ + (float) j1 * f, (float) p_175247_2_, flag ? 25 : 16, 0, 9, 9);

						if (flag) {
							if (j1 * 2 + 1 < player.func_178860_m()) this.drawTexturedModalRect((float) p_175247_4_ + (float) j1 * f, (float) p_175247_2_, 70, 0, 9, 9);

							if (j1 * 2 + 1 == player.func_178860_m()) this.drawTexturedModalRect((float) p_175247_4_ + (float) j1 * f, (float) p_175247_2_, 79, 0, 9, 9);
						}

						if (j1 * 2 + 1 < i) this.drawTexturedModalRect((float) p_175247_4_ + (float) j1 * f, (float) p_175247_2_, j1 >= 10 ? 160 : 52, 0, 9, 9);

						if (j1 * 2 + 1 == i) this.drawTexturedModalRect((float) p_175247_4_ + (float) j1 * f, (float) p_175247_2_, j1 >= 10 ? 169 : 61, 0, 9, 9);
					}
				} else {
					float f1 = MathHelper.clamp_float((float) i / 20.0F, 0.0F, 1.0F);
					int i1 = (int) ((1.0F - f1) * 255.0F) << 16 | (int) (f1 * 255.0F) << 8;
					String s = "" + (float) i / 2.0F;

					if (p_175247_5_ - this.mc.fontRenderer.getStringWidth(s + "hp") >= p_175247_4_) s = s + "hp";

					this.mc.fontRenderer.drawStringWithShadow(s, (float) ((p_175247_5_ + p_175247_4_) / 2 - this.mc.fontRenderer.getStringWidth(s) / 2), (float) p_175247_2_, i1);
				}
			}
		} else {
			String s1 = EnumChatFormatting.YELLOW + "" + i;
			this.mc.fontRenderer.drawStringWithShadow(s1, (float) (p_175247_5_ - this.mc.fontRenderer.getStringWidth(s1)), (float) p_175247_2_, 16777215);
		}
	}

	public void setFooter(IChatComponent footerIn) {
		this.footer = footerIn;
	}

	public void setHeader(IChatComponent headerIn) {
		this.header = headerIn;
	}

	public void func_181030_a() {
		this.header = null;
		this.footer = null;
	}

	static class PlayerComparator implements Comparator<NetworkPlayerInfo> {

		private PlayerComparator() {
		}

		public int compare(NetworkPlayerInfo p_compare_1_, NetworkPlayerInfo p_compare_2_) {
			ScorePlayerTeam scoreplayerteam = p_compare_1_.getPlayerTeam();
			ScorePlayerTeam scoreplayerteam1 = p_compare_2_.getPlayerTeam();
			return ComparisonChain.start().compareTrueFirst(p_compare_1_.getGameType() != WorldSettings.GameType.SPECTATOR, p_compare_2_.getGameType() != WorldSettings.GameType.SPECTATOR).compare(
					scoreplayerteam != null ? scoreplayerteam.getRegisteredName() : "", scoreplayerteam1 != null ? scoreplayerteam1.getRegisteredName() : "").compare(
					p_compare_1_.getGameProfile().getName(), p_compare_2_.getGameProfile().getName()).result();
		}

	}

}
