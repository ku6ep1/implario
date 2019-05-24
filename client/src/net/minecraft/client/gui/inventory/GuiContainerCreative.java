package net.minecraft.client.gui.inventory;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.achievement.GuiAchievements;
import net.minecraft.client.gui.achievement.GuiStats;
import net.minecraft.client.gui.element.GuiButton;
import net.minecraft.client.gui.element.GuiTextField;
import net.minecraft.client.renderer.G;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.Lang;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.settings.Settings;
import net.minecraft.inventory.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static net.minecraft.inventory.ClickType.*;

public class GuiContainerCreative extends InventoryEffectRenderer {

	/**
	 * The location of the creative inventory tabs texture
	 */
	private static final ResourceLocation creativeInventoryTabs = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");
	private static InventoryBasic field_147060_v = new InventoryBasic("tmp", true, 45);

	/**
	 * Currently selected creative inventory tab index.
	 */
	private static int selectedTabIndex = CreativeTabs.tabBlock.getTabIndex();

	/**
	 * Amount scrolled in Creative mode inventory (0 = top, 1 = bottom)
	 */
	private float currentScroll;

	/**
	 * True if the scrollbar is being dragged
	 */
	private boolean isScrolling;

	/**
	 * True if the left mouse button was held down last time drawScreen was called.
	 */
	private boolean wasClicking;
	private GuiTextField searchField;
	private List<Slot> field_147063_B;
	private Slot recycleBin;
	private boolean succeedSearch;
	private CreativeCrafting crafting;

	public GuiContainerCreative(EntityPlayer p_i1088_1_) {
		super(new GuiContainerCreative.ContainerCreative(p_i1088_1_));
		p_i1088_1_.openContainer = this.inventorySlots;
		this.allowUserInput = true;
		this.ySize = 136;
		this.xSize = 195;
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	public void updateScreen() {
		if (!this.mc.playerController.isInCreativeMode()) {
			this.mc.displayGuiScreen(new GuiInventory(this.mc.thePlayer));
		}

		this.updateActivePotionEffects();
	}

	/**
	 * Called when the mouse is clicked over a slot or outside the gui.
	 */
	protected void handleMouseClick(Slot slotIn, int slotId, int clickedButton, ClickType clickType) {
		this.succeedSearch = true;
		boolean shift = clickType == SHIFT;
		clickType = slotId == -999 && clickType == CLICK ? DROP : clickType;

		if (slotIn == null && selectedTabIndex != CreativeTabs.tabInventory.getTabIndex() && clickType != DRAG) {
			InventoryPlayer inventoryplayer1 = this.mc.thePlayer.inventory;

			if (inventoryplayer1.getItemStack() != null) {
				if (clickedButton == 0) {
					this.mc.thePlayer.dropPlayerItemWithRandomChoice(inventoryplayer1.getItemStack(), true);
					this.mc.playerController.sendPacketDropItem(inventoryplayer1.getItemStack());
					inventoryplayer1.setItemStack(null);
				}

				if (clickedButton == 1) {
					ItemStack itemstack5 = inventoryplayer1.getItemStack().splitStack(1);
					this.mc.thePlayer.dropPlayerItemWithRandomChoice(itemstack5, true);
					this.mc.playerController.sendPacketDropItem(itemstack5);

					if (inventoryplayer1.getItemStack().stackSize == 0) {
						inventoryplayer1.setItemStack(null);
					}
				}
			}
		} else if (slotIn == this.recycleBin && shift) {
			for (int j = 0; j < this.mc.thePlayer.inventoryContainer.getInventory().size(); ++j) {
				this.mc.playerController.sendSlotPacket(null, j);
			}
		} else if (selectedTabIndex == CreativeTabs.tabInventory.getTabIndex()) {
			if (slotIn == this.recycleBin) {
				this.mc.thePlayer.inventory.setItemStack(null);
			} else if (clickType == DROP && slotIn != null && slotIn.getHasStack()) {
				ItemStack itemstack = slotIn.decrStackSize(clickedButton == 0 ? 1 : slotIn.getStack().getMaxStackSize());
				this.mc.thePlayer.dropPlayerItemWithRandomChoice(itemstack, true);
				this.mc.playerController.sendPacketDropItem(itemstack);
			} else if (clickType == DROP && this.mc.thePlayer.inventory.getItemStack() != null) {
				this.mc.thePlayer.dropPlayerItemWithRandomChoice(this.mc.thePlayer.inventory.getItemStack(), true);
				this.mc.playerController.sendPacketDropItem(this.mc.thePlayer.inventory.getItemStack());
				this.mc.thePlayer.inventory.setItemStack(null);
			} else {
				this.mc.thePlayer.inventoryContainer.slotClick(slotIn == null ? slotId : ((GuiContainerCreative.CreativeSlot) slotIn).slot.slotNumber, clickedButton, clickType, this.mc.thePlayer);
				this.mc.thePlayer.inventoryContainer.detectAndSendChanges();
			}
		} else if (clickType != DRAG && slotIn.inventory == field_147060_v) {
			InventoryPlayer inventoryplayer = this.mc.thePlayer.inventory;
			ItemStack itemstack1 = inventoryplayer.getItemStack();
			ItemStack itemstack2 = slotIn.getStack();

			if (clickType == HOTBAR) {
				if (itemstack2 != null && clickedButton >= 0 && clickedButton < 9) {
					ItemStack itemstack7 = itemstack2.copy();
					itemstack7.stackSize = itemstack7.getMaxStackSize();
					this.mc.thePlayer.inventory.setInventorySlotContents(clickedButton, itemstack7);
					this.mc.thePlayer.inventoryContainer.detectAndSendChanges();
				}

				return;
			}

			if (clickType == PICK) {
				if (inventoryplayer.getItemStack() == null && slotIn.getHasStack()) {
					ItemStack itemstack6 = slotIn.getStack().copy();
					itemstack6.stackSize = itemstack6.getMaxStackSize();
					inventoryplayer.setItemStack(itemstack6);
				}

				return;
			}

			if (clickType == DROP) {
				if (itemstack2 != null) {
					ItemStack itemstack3 = itemstack2.copy();
					itemstack3.stackSize = clickedButton == 0 ? 1 : itemstack3.getMaxStackSize();
					this.mc.thePlayer.dropPlayerItemWithRandomChoice(itemstack3, true);
					this.mc.playerController.sendPacketDropItem(itemstack3);
				}

				return;
			}

			if (itemstack1 != null && itemstack1.isItemEqual(itemstack2)) {
				if (clickedButton == 0) {
					if (shift) {
						itemstack1.stackSize = itemstack1.getMaxStackSize();
					} else if (itemstack1.stackSize < itemstack1.getMaxStackSize()) {
						++itemstack1.stackSize;
					}
				} else if (itemstack1.stackSize <= 1) {
					inventoryplayer.setItemStack(null);
				} else {
					--itemstack1.stackSize;
				}
			} else if (itemstack2 != null && itemstack1 == null) {
				inventoryplayer.setItemStack(ItemStack.copyItemStack(itemstack2));
				itemstack1 = inventoryplayer.getItemStack();

				if (shift) {
					itemstack1.stackSize = itemstack1.getMaxStackSize();
				}
			} else {
				inventoryplayer.setItemStack(null);
			}
		} else {
			this.inventorySlots.slotClick(slotIn == null ? slotId : slotIn.slotNumber, clickedButton, clickType, this.mc.thePlayer);

			if (Container.getDragEvent(clickedButton) == 2) {
				for (int i = 0; i < 9; ++i) {
					this.mc.playerController.sendSlotPacket(this.inventorySlots.getSlot(45 + i).getStack(), 36 + i);
				}
			} else if (slotIn != null) {
				ItemStack itemstack4 = this.inventorySlots.getSlot(slotIn.slotNumber).getStack();
				this.mc.playerController.sendSlotPacket(itemstack4, slotIn.slotNumber - this.inventorySlots.inventorySlots.size() + 9 + 36);
			}
		}
	}

	protected void updateActivePotionEffects() {
		int i = this.guiLeft;
		super.updateActivePotionEffects();

		if (this.searchField != null && this.guiLeft != i) {
			this.searchField.xPosition = this.guiLeft + 82;
		}
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void initGui() {
		if (this.mc.playerController.isInCreativeMode()) {
			super.initGui();
			this.buttonList.clear();
			Keyboard.enableRepeatEvents(true);
			this.searchField = new GuiTextField(0, this.fontRendererObj, this.guiLeft + 82, this.guiTop + 6, 89, this.fontRendererObj.getFontHeight());
			this.searchField.setMaxStringLength(15);
			this.searchField.setEnableBackgroundDrawing(false);
			this.searchField.setVisible(false);
			this.searchField.setTextColor(16777215);
			int i = selectedTabIndex;
			selectedTabIndex = -1;
			this.setCurrentCreativeTab(CreativeTabs.creativeTabArray[i]);
			this.crafting = new CreativeCrafting(this.mc);
			this.mc.thePlayer.inventoryContainer.onCraftGuiOpened(this.crafting);
		} else {
			this.mc.displayGuiScreen(new GuiInventory(this.mc.thePlayer));
		}
	}

	/**
	 * Called when the screen is unloaded. Used to disable keyboard repeat events
	 */
	public void onGuiClosed() {
		super.onGuiClosed();

		if (this.mc.thePlayer != null && this.mc.thePlayer.inventory != null) {
			this.mc.thePlayer.inventoryContainer.removeCraftingFromCrafters(this.crafting);
		}

		Keyboard.enableRepeatEvents(false);
	}

	/**
	 * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
	 */
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (selectedTabIndex != CreativeTabs.tabAllSearch.getTabIndex()) {
			if (KeyBinding.CHAT.isClicked()) {
				this.setCurrentCreativeTab(CreativeTabs.tabAllSearch);
			} else {
				super.keyTyped(typedChar, keyCode);
			}
		} else {
			if (this.succeedSearch) {
				this.succeedSearch = false;
				this.searchField.setText("");
			}

			if (!this.checkHotbarKeys(keyCode)) {
				if (this.searchField.textboxKeyTyped(typedChar, keyCode)) {
					this.updateCreativeSearch();
				} else {
					super.keyTyped(typedChar, keyCode);
				}
			}
		}
	}

	private void updateCreativeSearch() {
		GuiContainerCreative.ContainerCreative guicontainercreative$containercreative = (GuiContainerCreative.ContainerCreative) this.inventorySlots;
		guicontainercreative$containercreative.itemList.clear();

		for (Item item : Item.itemRegistry) {
			if (item != null && item.getCreativeTab() != null) {
				item.getSubItems(item, null, guicontainercreative$containercreative.itemList);
			}
		}

		for (Enchantment enchantment : Enchantment.enchantmentsBookList) {
			if (enchantment != null && enchantment.type != null) {
				Items.enchanted_book.getAll(enchantment, guicontainercreative$containercreative.itemList);
			}
		}

		Iterator<ItemStack> iterator = guicontainercreative$containercreative.itemList.iterator();
		String s1 = this.searchField.getText().toLowerCase();

		while (iterator.hasNext()) {
			ItemStack itemstack = iterator.next();
			boolean flag = false;

			for (String s : itemstack.getTooltip(this.mc.thePlayer, Settings.ITEM_TOOLTIPS.b())) {
				if (EnumChatFormatting.getTextWithoutFormattingCodes(s).toLowerCase().contains(s1)) {
					flag = true;
					break;
				}
			}

			if (!flag) {
				iterator.remove();
			}
		}

		this.currentScroll = 0.0F;
		guicontainercreative$containercreative.scrollTo(0.0F);
	}

	/**
	 * Draw the foreground layer for the GuiContainer (everything in front of the items). Args : mouseX, mouseY
	 */
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		CreativeTabs creativetabs = CreativeTabs.creativeTabArray[selectedTabIndex];

		if (creativetabs.drawInForegroundOfTab()) {
			G.disableBlend();
			this.fontRendererObj.drawString(Lang.format(creativetabs.getTranslatedTabLabel()), 8, 6, 4210752);
		}
	}

	/**
	 * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
	 */
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (mouseButton == 0) {
			int i = mouseX - this.guiLeft;
			int j = mouseY - this.guiTop;

			for (CreativeTabs creativetabs : CreativeTabs.creativeTabArray) {
				if (this.func_147049_a(creativetabs, i, j)) {
					return;
				}
			}
		}

		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	/**
	 * Called when a mouse button is released.  Args : mouseX, mouseY, releaseButton
	 */
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		if (state == 0) {
			int i = mouseX - this.guiLeft;
			int j = mouseY - this.guiTop;

			for (CreativeTabs creativetabs : CreativeTabs.creativeTabArray) {
				if (this.func_147049_a(creativetabs, i, j)) {
					this.setCurrentCreativeTab(creativetabs);
					return;
				}
			}
		}

		super.mouseReleased(mouseX, mouseY, state);
	}

	/**
	 * returns (if you are not on the inventoryTab) and (the flag isn't set) and (you have more than 1 page of items)
	 */
	private boolean needsScrollBars() {
		return selectedTabIndex != CreativeTabs.tabInventory.getTabIndex() && CreativeTabs.creativeTabArray[selectedTabIndex].shouldHidePlayerInventory() && ((GuiContainerCreative.ContainerCreative) this.inventorySlots).func_148328_e();
	}

	private void setCurrentCreativeTab(CreativeTabs p_147050_1_) {
		int i = selectedTabIndex;
		selectedTabIndex = p_147050_1_.getTabIndex();
		GuiContainerCreative.ContainerCreative guicontainercreative$containercreative = (GuiContainerCreative.ContainerCreative) this.inventorySlots;
		this.dragSplittingSlots.clear();
		guicontainercreative$containercreative.itemList.clear();
		p_147050_1_.displayAllReleventItems(guicontainercreative$containercreative.itemList);

		if (p_147050_1_ == CreativeTabs.tabInventory) {
			Container container = this.mc.thePlayer.inventoryContainer;

			if (this.field_147063_B == null) {
				this.field_147063_B = guicontainercreative$containercreative.inventorySlots;
			}

			guicontainercreative$containercreative.inventorySlots = Lists.newArrayList();

			for (int j = 0; j < container.inventorySlots.size(); ++j) {
				Slot slot = new GuiContainerCreative.CreativeSlot(container.inventorySlots.get(j), j);
				guicontainercreative$containercreative.inventorySlots.add(slot);

				if (j >= 5 && j < 9) {
					int j1 = j - 5;
					int k1 = j1 / 2;
					int l1 = j1 % 2;
					slot.xDisplayPosition = 9 + k1 * 54;
					slot.yDisplayPosition = 6 + l1 * 27;
				} else if (j >= 0 && j < 5) {
					slot.yDisplayPosition = -2000;
					slot.xDisplayPosition = -2000;
				} else if (j < container.inventorySlots.size()) {
					int k = j - 9;
					int l = k % 9;
					int i1 = k / 9;
					slot.xDisplayPosition = 9 + l * 18;

					if (j >= 36) {
						slot.yDisplayPosition = 112;
					} else {
						slot.yDisplayPosition = 54 + i1 * 18;
					}
				}
			}

			this.recycleBin = new Slot(field_147060_v, 0, 173, 112);
			guicontainercreative$containercreative.inventorySlots.add(this.recycleBin);
		} else if (i == CreativeTabs.tabInventory.getTabIndex()) {
			guicontainercreative$containercreative.inventorySlots = this.field_147063_B;
			this.field_147063_B = null;
		}

		if (this.searchField != null) {
			if (p_147050_1_ == CreativeTabs.tabAllSearch) {
				this.searchField.setVisible(true);
				this.searchField.setCanLoseFocus(false);
				this.searchField.setFocused(true);
				this.searchField.setText("");
				this.updateCreativeSearch();
			} else {
				this.searchField.setVisible(false);
				this.searchField.setCanLoseFocus(true);
				this.searchField.setFocused(false);
			}
		}

		this.currentScroll = 0.0F;
		guicontainercreative$containercreative.scrollTo(0.0F);
	}

	/**
	 * Handles mouse input.
	 */
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		int i = Mouse.getEventDWheel();

		if (i != 0 && this.needsScrollBars()) {
			int j = ((GuiContainerCreative.ContainerCreative) this.inventorySlots).itemList.size() / 9 - 5;

			if (i > 0) i = 1;
			if (i < 0) i = -1;

			this.currentScroll = (float) ((double) this.currentScroll - (double) i / (double) j);
			this.currentScroll = MathHelper.clamp_float(this.currentScroll, 0.0F, 1.0F);
			((GuiContainerCreative.ContainerCreative) this.inventorySlots).scrollTo(this.currentScroll);
		}
	}

	/**
	 * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
	 */
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		boolean flag = Mouse.isButtonDown(0);
		int x0 = this.guiLeft;
		int y0 = this.guiTop;
		int x1 = x0 + 175;
		int y1 = y0 + 18;
		int x2 = x1 + 14;
		int y2 = y1 + 112;

		if (!this.wasClicking && flag && mouseX >= x1 && mouseY >= y1 && mouseX < x2 && mouseY < y2)
			this.isScrolling = this.needsScrollBars();

		if (!flag) this.isScrolling = false;

		this.wasClicking = flag;

		if (this.isScrolling) {
			this.currentScroll = ((float) (mouseY - y1) - 7.5F) / ((float) (y2 - y1) - 15.0F);
			this.currentScroll = MathHelper.clamp_float(this.currentScroll, 0.0F, 1.0F);
			((GuiContainerCreative.ContainerCreative) this.inventorySlots).scrollTo(this.currentScroll);
		}

		super.drawScreen(mouseX, mouseY, partialTicks);

		for (CreativeTabs creativetabs : CreativeTabs.creativeTabArray)
			if (this.renderCreativeInventoryHoveringText(creativetabs, mouseX, mouseY)) break;

		if (this.recycleBin != null && selectedTabIndex == CreativeTabs.tabInventory.getTabIndex() && this.isPointInRegion(this.recycleBin.xDisplayPosition,
				this.recycleBin.yDisplayPosition, 16, 16, mouseX, mouseY)) {
			this.drawCreativeTabHoveringText(Lang.format("inventory.binSlot"), mouseX, mouseY);
		}

		G.color(1.0F, 1.0F, 1.0F, 1.0F);
		G.disableLighting();
	}

	protected void renderToolTip(ItemStack stack, int x, int y) {
		if (selectedTabIndex != CreativeTabs.tabAllSearch.getTabIndex()) {
			super.renderToolTip(stack, x, y);
			return;
		}
		List<String> list = stack.getTooltip(this.mc.thePlayer, Settings.ITEM_TOOLTIPS.b());
		CreativeTabs creativetabs = stack.getItem().getCreativeTab();

		if (creativetabs == null && stack.getItem() == Items.enchanted_book) {
			Map<Integer, Integer> map = EnchantmentHelper.getEnchantments(stack);

			if (map.size() == 1) {
				Enchantment enchantment = Enchantment.getEnchantmentById(map.keySet().iterator().next());

				for (CreativeTabs creativetabs1 : CreativeTabs.creativeTabArray) {
					if (creativetabs1.hasRelevantEnchantmentType(enchantment.type)) {
						creativetabs = creativetabs1;
						break;
					}
				}
			}
		}

		if (creativetabs != null)
			list.add(1, "" + EnumChatFormatting.BLUE + Lang.format(creativetabs.getTranslatedTabLabel()));

		for (int i = 0; i < list.size(); ++i)
			if (i == 0) list.set(i, stack.getRarity().rarityColor + list.get(i));
			else list.set(i, EnumChatFormatting.YELLOW + list.get(i));

		this.drawHoveringText(list, x, y);
	}

	/**
	 * Args : renderPartialTicks, mouseX, mouseY
	 */
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		G.color(1.0F, 1.0F, 1.0F, 1.0F);
		RenderHelper.enableGUIStandardItemLighting();
		CreativeTabs creativetabs = CreativeTabs.creativeTabArray[selectedTabIndex];

		for (CreativeTabs creativetabs1 : CreativeTabs.creativeTabArray) {
			this.mc.getTextureManager().bindTexture(creativeInventoryTabs);

			if (creativetabs1.getTabIndex() != selectedTabIndex) {
				this.func_147051_a(creativetabs1);
			}
		}

		this.mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/creative_inventory/tab_" + creativetabs.getBackgroundImageName()));
		this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
		this.searchField.drawTextBox();
		G.color(1.0F, 1.0F, 1.0F, 1.0F);
		int i = this.guiLeft + 175;
		int j = this.guiTop + 18;
		int k = j + 112;
		this.mc.getTextureManager().bindTexture(creativeInventoryTabs);

		if (creativetabs.shouldHidePlayerInventory()) {
			this.drawTexturedModalRect(i, j + (int) ((float) (k - j - 17) * this.currentScroll), 232 + (this.needsScrollBars() ? 0 : 12), 0, 12, 15);
		}

		this.func_147051_a(creativetabs);

		if (creativetabs == CreativeTabs.tabInventory) {
			GuiInventory.drawEntityOnScreen(this.guiLeft + 43, this.guiTop + 45, 20, (float) (this.guiLeft + 43 - mouseX), (float) (this.guiTop + 45 - 30 - mouseY), this.mc.thePlayer);
		}
	}

	protected boolean func_147049_a(CreativeTabs p_147049_1_, int p_147049_2_, int p_147049_3_) {
		int i = p_147049_1_.getTabColumn();
		int j = 28 * i;
		int k = 0;

		if (i == 5) {
			j = this.xSize - 28 + 2;
		} else if (i > 0) {
			j += i;
		}

		if (p_147049_1_.isTabInFirstRow()) {
			k = k - 32;
		} else {
			k = k + this.ySize;
		}

		return p_147049_2_ >= j && p_147049_2_ <= j + 28 && p_147049_3_ >= k && p_147049_3_ <= k + 32;
	}

	/**
	 * Renders the creative inventory hovering text if mouse is over it. Returns true if did render or false otherwise.
	 * Params: current creative tab to be checked, current mouse x position, current mouse y position.
	 */
	protected boolean renderCreativeInventoryHoveringText(CreativeTabs p_147052_1_, int p_147052_2_, int p_147052_3_) {
		int i = p_147052_1_.getTabColumn();
		int j = 28 * i;
		int k = 0;

		if (i == 5) {
			j = this.xSize - 28 + 2;
		} else if (i > 0) {
			j += i;
		}

		if (p_147052_1_.isTabInFirstRow()) {
			k = k - 32;
		} else {
			k = k + this.ySize;
		}

		if (this.isPointInRegion(j + 3, k + 3, 23, 27, p_147052_2_, p_147052_3_)) {
			this.drawCreativeTabHoveringText(Lang.format(p_147052_1_.getTranslatedTabLabel()), p_147052_2_, p_147052_3_);
			return true;
		}
		return false;
	}

	protected void func_147051_a(CreativeTabs p_147051_1_) {
		boolean flag = p_147051_1_.getTabIndex() == selectedTabIndex;
		boolean flag1 = p_147051_1_.isTabInFirstRow();
		int i = p_147051_1_.getTabColumn();
		int j = i * 28;
		int k = 0;
		int l = this.guiLeft + 28 * i;
		int i1 = this.guiTop;
		int j1 = 32;

		if (flag) {
			k += 32;
		}

		if (i == 5) {
			l = this.guiLeft + this.xSize - 28;
		} else if (i > 0) {
			l += i;
		}

		if (flag1) {
			i1 = i1 - 28;
		} else {
			k += 64;
			i1 = i1 + this.ySize - 4;
		}

		G.disableLighting();
		this.drawTexturedModalRect(l, i1, j, k, 28, j1);
		this.zLevel = 100.0F;
		this.itemRender.zLevel = 100.0F;
		l = l + 6;
		i1 = i1 + 8 + (flag1 ? 1 : -1);
		G.enableLighting();
		G.enableRescaleNormal();
		ItemStack itemstack = p_147051_1_.getIconItemStack();
		this.itemRender.renderItemAndEffectIntoGUI(itemstack, l, i1);
		this.itemRender.renderItemOverlays(this.fontRendererObj, itemstack, l, i1);
		G.disableLighting();
		this.itemRender.zLevel = 0.0F;
		this.zLevel = 0.0F;
	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
	 */
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 0) {
			this.mc.displayGuiScreen(new GuiAchievements(this, this.mc.thePlayer.getStatFileWriter()));
		}

		if (button.id == 1) {
			this.mc.displayGuiScreen(new GuiStats(this, this.mc.thePlayer.getStatFileWriter()));
		}
	}

	public int getSelectedTabIndex() {
		return selectedTabIndex;
	}

	static class ContainerCreative extends Container {

		public List<ItemStack> itemList = Lists.newArrayList();

		public ContainerCreative(EntityPlayer p_i1086_1_) {
			InventoryPlayer inventoryplayer = p_i1086_1_.inventory;

			for (int i = 0; i < 5; ++i) {
				for (int j = 0; j < 9; ++j) {
					this.addSlotToContainer(new Slot(GuiContainerCreative.field_147060_v, i * 9 + j, 9 + j * 18, 18 + i * 18));
				}
			}

			for (int k = 0; k < 9; ++k) {
				this.addSlotToContainer(new Slot(inventoryplayer, k, 9 + k * 18, 112));
			}

			this.scrollTo(0.0F);
		}

		public boolean canInteractWith(EntityPlayer playerIn) {
			return true;
		}

		public void scrollTo(float p_148329_1_) {
			int i = (this.itemList.size() + 9 - 1) / 9 - 5;
			int j = (int) ((double) (p_148329_1_ * (float) i) + 0.5D);

			if (j < 0) {
				j = 0;
			}

			for (int k = 0; k < 5; ++k) {
				for (int l = 0; l < 9; ++l) {
					int i1 = l + (k + j) * 9;

					if (i1 >= 0 && i1 < this.itemList.size()) {
						GuiContainerCreative.field_147060_v.setInventorySlotContents(l + k * 9, this.itemList.get(i1));
					} else {
						GuiContainerCreative.field_147060_v.setInventorySlotContents(l + k * 9, null);
					}
				}
			}
		}

		public boolean func_148328_e() {
			return this.itemList.size() > 45;
		}

		protected void retrySlotClick(int slotId, int clickedButton, boolean mode, EntityPlayer playerIn) {
		}

		public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
			if (index >= this.inventorySlots.size() - 9 && index < this.inventorySlots.size()) {
				Slot slot = this.inventorySlots.get(index);

				if (slot != null && slot.getHasStack()) {
					slot.putStack(null);
				}
			}

			return null;
		}

		public boolean canMergeSlot(ItemStack stack, Slot p_94530_2_) {
			return p_94530_2_.yDisplayPosition > 90;
		}

		public boolean canDragIntoSlot(Slot p_94531_1_) {
			return p_94531_1_.inventory instanceof InventoryPlayer || p_94531_1_.yDisplayPosition > 90 && p_94531_1_.xDisplayPosition <= 162;
		}

	}

	class CreativeSlot extends Slot {

		private final Slot slot;

		public CreativeSlot(Slot p_i46313_2_, int p_i46313_3_) {
			super(p_i46313_2_.inventory, p_i46313_3_, 0, 0);
			this.slot = p_i46313_2_;
		}

		public void onPickupFromSlot(EntityPlayer playerIn, ItemStack stack) {
			this.slot.onPickupFromSlot(playerIn, stack);
		}

		public boolean isItemValid(ItemStack stack) {
			return this.slot.isItemValid(stack);
		}

		public ItemStack getStack() {
			return this.slot.getStack();
		}

		public boolean getHasStack() {
			return this.slot.getHasStack();
		}

		public void putStack(ItemStack stack) {
			this.slot.putStack(stack);
		}

		public void onSlotChanged() {
			this.slot.onSlotChanged();
		}

		public int getSlotStackLimit() {
			return this.slot.getSlotStackLimit();
		}

		public int getItemStackLimit(ItemStack stack) {
			return this.slot.getItemStackLimit(stack);
		}

		public String getSlotTexture() {
			return this.slot.getSlotTexture();
		}

		public ItemStack decrStackSize(int amount) {
			return this.slot.decrStackSize(amount);
		}

		public boolean isHere(IInventory inv, int slotIn) {
			return this.slot.isHere(inv, slotIn);
		}

	}

}