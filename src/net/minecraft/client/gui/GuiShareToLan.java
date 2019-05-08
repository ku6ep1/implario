package net.minecraft.client.gui;

import net.minecraft.client.gui.element.GuiButton;
import net.minecraft.client.resources.Lang;
import net.minecraft.util.chat.ChatComponentText;
import net.minecraft.util.chat.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.WorldSettings;

import java.io.IOException;

public class GuiShareToLan extends GuiScreen {

	private final GuiScreen field_146598_a;
	private GuiButton field_146596_f;
	private GuiButton field_146597_g;
	private String field_146599_h = "survival";
	private boolean field_146600_i;

	public GuiShareToLan(GuiScreen p_i1055_1_) {
		this.field_146598_a = p_i1055_1_;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void initGui() {
		this.buttonList.clear();
		this.buttonList.add(new GuiButton(101, this.width / 2 - 155, this.height - 28, 150, 20, Lang.format("lanServer.start")));
		this.buttonList.add(new GuiButton(102, this.width / 2 + 5, this.height - 28, 150, 20, Lang.format("gui.cancel")));
		this.buttonList.add(this.field_146597_g = new GuiButton(104, this.width / 2 - 155, 100, 150, 20, Lang.format("selectWorld.gameMode")));
		this.buttonList.add(this.field_146596_f = new GuiButton(103, this.width / 2 + 5, 100, 150, 20, Lang.format("selectWorld.allowCommands")));
		this.func_146595_g();
	}

	private void func_146595_g() {
		this.field_146597_g.displayString = Lang.format("selectWorld.gameMode") + " " + Lang.format("selectWorld.gameMode." + this.field_146599_h);
		this.field_146596_f.displayString = Lang.format("selectWorld.allowCommands") + " ";

		if (this.field_146600_i) {
			this.field_146596_f.displayString = this.field_146596_f.displayString + Lang.format("options.on");
		} else {
			this.field_146596_f.displayString = this.field_146596_f.displayString + Lang.format("options.off");
		}
	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
	 */
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 102) {
			this.mc.displayGuiScreen(this.field_146598_a);
		} else if (button.id == 104) {
			switch (this.field_146599_h) {
				case "spectator":
					this.field_146599_h = "creative";
					break;
				case "creative":
					this.field_146599_h = "adventure";
					break;
				case "adventure":
					this.field_146599_h = "survival";
					break;
				default:
					this.field_146599_h = "spectator";
					break;
			}

			this.func_146595_g();
		} else if (button.id == 103) {
			this.field_146600_i = !this.field_146600_i;
			this.func_146595_g();
		} else if (button.id == 101) {
			this.mc.displayGuiScreen(null);
			String s = this.mc.getIntegratedServer().shareToLAN(WorldSettings.GameType.getByName(this.field_146599_h), this.field_146600_i);
			IChatComponent ichatcomponent;

			if (s != null) {
				ichatcomponent = new ChatComponentTranslation("commands.publish.started", s);
			} else {
				ichatcomponent = new ChatComponentText("commands.publish.failed");
			}

			this.mc.ingameGUI.getChatGUI().printChatMessage(ichatcomponent);
		}
	}

	/**
	 * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
	 */
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRendererObj, Lang.format("lanServer.title"), this.width / 2, 50, 16777215);
		this.drawCenteredString(this.fontRendererObj, Lang.format("lanServer.otherPlayers"), this.width / 2, 82, 16777215);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

}
