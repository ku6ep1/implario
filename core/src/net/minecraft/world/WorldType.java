package net.minecraft.world;

import net.minecraft.util.Util;
import net.minecraft.world.biome.BasicChunkManager;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.IChunkManager;
import net.minecraft.world.chunk.ChunkProviderDebug;
import net.minecraft.world.chunk.ChunkProviderVoid;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.datapacks.ChunkManagerFactory;
import net.minecraft.world.datapacks.ChunkProviderFactory;

public class WorldType {

	/**
	 * List of world types.
	 */
	public static final WorldType[] worldTypes = new WorldType[16];



//	public static final WorldType DEFAULT = new WorldType(0, "default").setVersioned();
//	public static final WorldType FLAT = new WorldType(1, "flat");
//	public static final WorldType LARGE_BIOMES = new WorldType(2, "largeBiomes");
//	public static final WorldType AMPLIFIED = new WorldType(3, "amplified").setNotificationData();
//	public static final WorldType CUSTOMIZED = new WorldType(4, "customized");
//	public static final WorldType DEBUG_WORLD = new WorldType(5, "debug_all_block_states");
	public static final WorldType VOID = new WorldType("empty",
		(p, s, v, c) -> new BasicChunkManager(Biome.VOID),
		p -> new ChunkProviderVoid(p.getWorld())).weakFog();
	private static final ChunkManagerFactory factoryDebugCM = (p, s, v, c) -> new BasicChunkManager(Biome.VOID);
	private static final ChunkProviderFactory factoryDebugCP = p -> new ChunkProviderDebug(p.getWorld());
	public static final WorldType DEBUG = new WorldType("debug_all_block_states", factoryDebugCM, factoryDebugCP).disableMobs().disallowModification();

	//	public static final WorldType DEFAULT_1_1 = new WorldType(8, "default_1_1").setCanBeCreated(false);

	/**
	 * ID for this world type.
	 */
	private final int id;
	private final String worldType;
	private final ChunkManagerFactory chunkManagerFactory;
	private final ChunkProviderFactory chunkProviderFactory;

	/**
	 * Whether this world type can be generated. Normally true; set to false for out-of-date generator versions.
	 */
	private boolean canBeCreated = true;

 	/**
	 * Whether this WorldType has a version or not.
	 */
	private boolean isWorldTypeVersioned;
	private boolean hasNotificationData;
	private boolean weakFog;
	private boolean doMobSpawning = true;
	private boolean modificationAllowed = true;

	public WorldType(String name, ChunkManagerFactory manager, ChunkProviderFactory provider) {
		this.worldType = name;
		this.id = Util.firstEmpty(worldTypes);
		this.chunkManagerFactory = manager;
		this.chunkProviderFactory = provider;
		worldTypes[id] = this;
	}

	public String getWorldTypeName() {
		return this.worldType;
	}

	/**
	 * Gets the translation key for the name of this world type.
	 */
	public String getTranslateName() {
		return "generator." + this.worldType;
	}

	public String func_151359_c() {
		return this.getTranslateName() + ".info";
	}

	/**
	 * Returns generatorVersion.
	 */
	public int getGeneratorVersion() {
		return isWorldTypeVersioned ? 1 : 0;
	}

	public boolean isWeakFog() {
		return weakFog;
	}

	public WorldType weakFog() {
		this.weakFog = true;
		return this;
	}


	/**
	 * Gets whether this WorldType can be used to generate a new world.
	 */
	public boolean getCanBeCreated() {
		return this.canBeCreated;
	}

	/**
	 * Flags this world type as having an associated version.
	 */
	private WorldType setVersioned() {

		this.isWorldTypeVersioned = true;
		return this;
	}

	/**
	 * Returns true if this world Type has a version associated with it.
	 */
	public boolean isVersioned() {
		return this.isWorldTypeVersioned;
	}

	public static WorldType parseWorldType(String type) {
		for (WorldType t : worldTypes)
			if (t != null && t.worldType.equalsIgnoreCase(type)) return t;

		return null;
	}

	public int getWorldTypeID() {
		return this.id;
	}

	/**
	 * returns true if selecting this worldtype from the customize menu should display the generator.[worldtype].info
	 * message
	 */
	public boolean showWorldInfoNotice() {
		return this.hasNotificationData;
	}

	/**
	 * enables the display of generator.[worldtype].info message on the customize world menu
	 */
	private WorldType setNotificationData() {
		this.hasNotificationData = true;
		return this;
	}

	public IChunkManager createChunkManager(WorldProvider w) {
		return chunkManagerFactory.generate(w, 0, this, w.getGeneratorSettings());
	}
	public IChunkManager createChunkManager(long seed, String settings) {
		return chunkManagerFactory.generate(null, seed, this, settings);
	}

	public WorldType invisible() {
		canBeCreated = false;
		return this;
	}

	public IChunkProvider createChunkProvider(WorldProvider worldProvider) {
		return chunkProviderFactory.generate(worldProvider);
	}

	public boolean doMobSpawning() {
		return doMobSpawning;
	}

	public WorldType disableMobs() {
		doMobSpawning = false;
		return this;
	}

	public boolean isModificationAllowed() {
		return modificationAllowed;
	}

	public WorldType disallowModification() {
		modificationAllowed = false;
		return this;
	}

}
