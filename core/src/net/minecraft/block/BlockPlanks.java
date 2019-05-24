package net.minecraft.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

import java.util.List;

public class BlockPlanks extends Block {

	public static final PropertyEnum<BlockPlanks.EnumType> VARIANT = PropertyEnum.create("variant", BlockPlanks.EnumType.class);

	public BlockPlanks() {
		super(Material.wood);
		this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, BlockPlanks.EnumType.OAK));
		this.setCreativeTab(CreativeTabs.tabBlock);
	}

	/**
	 * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
	 * returns the metadata of the dropped item based on the old metadata of the block.
	 */
	public int damageDropped(IBlockState state) {
		return state.getValue(VARIANT).getMetadata();
	}

	/**
	 * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
	 */
	public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
		for (BlockPlanks.EnumType blockplanks$enumtype : BlockPlanks.EnumType.values()) {
			list.add(new ItemStack(itemIn, 1, blockplanks$enumtype.getMetadata()));
		}
	}

	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(VARIANT, BlockPlanks.EnumType.byMetadata(meta));
	}

	/**
	 * Get the MapColor for this Block and the given BlockState
	 */
	public MapColor getMapColor(IBlockState state) {
		return state.getValue(VARIANT).getMapColor();
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	public int getMetaFromState(IBlockState state) {
		return state.getValue(VARIANT).getMetadata();
	}

	protected BlockState createBlockState() {
		return new BlockState(this, VARIANT);
	}

	public enum EnumType implements IStringSerializable {
		OAK(0, "oak", MapColor.woodColor),
		SPRUCE(1, "spruce", MapColor.obsidianColor),
		BIRCH(2, "birch", MapColor.sandColor),
		JUNGLE(3, "jungle", MapColor.dirtColor),
		ACACIA(4, "acacia", MapColor.adobeColor),
		DARK_OAK(5, "dark_oak", "big_oak", MapColor.brownColor);

		private static final BlockPlanks.EnumType[] META_LOOKUP = new BlockPlanks.EnumType[values().length];
		private final int meta;
		private final String name;
		private final String unlocalizedName;
		private final MapColor mapColor;

		EnumType(int meta, String name, MapColor mapColor) {
			this(meta, name, name, mapColor);
		}

		EnumType(int meta, String name, String unlocalizedName, MapColor mapColor) {
			this.meta = meta;
			this.name = name;
			this.unlocalizedName = unlocalizedName;
			this.mapColor = mapColor;
		}

		public int getMetadata() {
			return this.meta;
		}

		public MapColor getMapColor() {
			return this.mapColor;
		}

		public String toString() {
			return this.name;
		}

		public static BlockPlanks.EnumType byMetadata(int meta) {
			if (meta < 0 || meta >= META_LOOKUP.length) meta = 0;
			return META_LOOKUP[meta];
		}

		public String getName() {
			return this.name;
		}

		public String getUnlocalizedName() {
			return this.unlocalizedName;
		}

		static {
			for (BlockPlanks.EnumType blockplanks$enumtype : values())
				META_LOOKUP[blockplanks$enumtype.getMetadata()] = blockplanks$enumtype;
		}
	}

}