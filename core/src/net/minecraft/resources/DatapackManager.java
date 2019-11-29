package net.minecraft.resources;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.minecraft.entity.player.Module;
import net.minecraft.entity.player.ModuleManager;
import net.minecraft.entity.player.Player;
import net.minecraft.logging.Log;
import net.minecraft.resources.load.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Tree;
import net.minecraft.util.byteable.Decoder;
import net.minecraft.util.byteable.Encoder;
import net.minecraft.util.byteable.FastDecoder;
import net.minecraft.util.byteable.FastEncoder;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@UtilityClass
public class DatapackManager {

	public static final String MINECRAFT = "minecraft";
	public final DatapackLoader ROOT = new SimpleDatapackLoader(new Datapack(MINECRAFT) {}, DatapackInfo.builder().domain(MINECRAFT).build());

	private final Map<String, DatapackLoader> map = new HashMap<>();

	@Getter
	private final Tree<DatapackLoader> tree = new Tree<>(ROOT);

	private final Set<Datapack> datapacks = new HashSet<>();

	public void prepare(DatapackLoader loader) throws DatapackLoadException {
		DatapackInfo properties = loader.prepareReader();
		map.put(properties.getDomain(), loader);
		for (String dependency : properties.getDependencies()) {
			map.get(dependency).growBranch(loader);
		}
		tree.getRootElement().growBranch(loader);
	}

	public DatapackLoader importJar(File jar) throws DatapackLoadException {
		DatapackLoader loader = new JarDatapackLoader(jar);
		prepare(loader);
		tree.rebuild();
		return loader;
	}

	public void importDir(File dir) throws DatapackLoadException {
		if (!dir.isDirectory()) return;
		for (File file : dir.listFiles()) {
			if (file.isDirectory() || !file.getAbsolutePath().endsWith(".jar")) continue;
			DatapackLoader loader = new JarDatapackLoader(file);
			prepare(loader);
		}
		tree.rebuild();
	}

	public void loadDir(File dir){
		try {
			importDir(dir);
			for (DatapackLoader loader : DatapackManager.getTree().loadingOrder()) {
				Log.MAIN.info("Instantiating datapack" + loader);
				try {
					DatapackManager.load(loader);
				}catch (DatapackLoadException ex){
					ex.printStackTrace();
				}
			}
		} catch (DatapackLoadException e) {
			e.printStackTrace();
		}
		initializeModules();
	}

	public Datapack load(DatapackLoader loader) throws DatapackLoadException {
		Datapack datapack = loader.createInstance();
		datapacks.add(datapack);
		return datapack;
	}

	public DatapackLoader getLoaderByName(String name) {
		return map.get(name);
	}

	public Datapack getDatapack(String name){
		DatapackLoader loader = getLoaderByName(name);
		return loader == null ? null : loader.getInstance();
	}

	public void init(DatapackLoader loader) {
		loader.getInstance().init();
	}

	public void shutdownBranch(DatapackLoader loader) {
		List<DatapackLoader> dependents = tree.buildUnloadingFrom(loader);
		for (DatapackLoader dependent : dependents) {
			if (dependent == loader) continue;
			Log.MAIN.info("Releasing " + dependent + "...");
			shutdownBranch(dependent);
		}
		map.remove(loader.getProperties().getDomain());
		loader.getInstance().unload();
		loader.getInstance().disable();
		loader.close();
	}

	public void loadBranch(DatapackLoader loader) throws DatapackLoadException{
		List<DatapackLoader> load = tree.buildLoadingFrom(loader);
		prepare(loader);
		for (DatapackLoader dependent : load) {
			if (dependent == loader) continue;
			Log.MAIN.info("Loading " + dependent + "...");
			loadBranch(dependent);
		}
		loader.createInstance();
		loader.getInstance().preinit();
		loader.getInstance().init();
	}

	public Iterable<DatapackLoader> getLoaders(){
		return map.values();
	}

	public void initializeModules(){
		List<String> modules = new ArrayList<>();
		for(Datapack datapack : datapacks)
			if(datapack.moduleManager() != null){
				datapack.moduleManager().writeID(modules.size());
				modules.add(datapack.getDomain());
			}
		DatapackManager.modules = modules.toArray(new String[]{});
	}

	public int getModulesSize(){
		return modules.length;
	}

	public String getDatapackByModuleID(int id){
		return modules[id];
	}

	private String[] modules;

	public static byte[] removePlayerInfo(Datapack datapack) {
		if (MinecraftServer.mcServer == null) return null;
		ModuleManager manager = datapack.moduleManager();
		if (manager == null) return null;
		String domain = manager.getDomain();
		Encoder encoder = new FastEncoder();
		int players = 0;
		for (Player player : MinecraftServer.mcServer.getConfigurationManager().getPlayers()) {
			Module module = manager.getModule(player);
			if (module == null) continue;
			players++;
		}
		encoder.writeInt(players);
		for (Player player : MinecraftServer.mcServer.getConfigurationManager().getPlayers()) {
			Module module = manager.getModule(player);
			if (module == null) continue;
			try {
				encoder.writeString(player.getName());
				if (manager.supportedWorld())
					encodeSecure(() -> manager.encodeWorld(module), encoder, domain);
				if (manager.supportedGlobal())
					encodeSecure(() -> manager.encodeGlobal(module), encoder, domain);
				if (manager.supportedMemory())
					encodeSecure(() -> manager.encodeMemory(module), encoder, domain);
			} catch (Throwable throwable) {
				Log.MAIN.error("Error on write nbt data, domain " + datapack.getDomain() + " module manager " + module.manager(), throwable);
			}
			manager.clearModule(player);
		}
		return encoder.generate();
	}

	private static void encodeSecure(Supplier<byte[]> supplier, Encoder encoder, String domain){
		byte array[] = null;
		try{
			array = supplier.get();
		}catch (Throwable error){
			Log.MAIN.error("Error on write nbt data domain: " + domain, error);
		}
		encoder.writeBoolean(array != null);
		if(array != null)encoder.writeBytes(array);
	}

	public static void loadPlayerInfo(Datapack datapack, byte array[]) {
		if (MinecraftServer.mcServer == null) return;
		ModuleManager manager = datapack.moduleManager();
		if (manager == null) {
			Log.MAIN.warn("ModuleManager on datapack " + datapack.moduleManager() + " not found, but nbt data founded");
			return;
		}
		Decoder decoder = new FastDecoder(array);
		int size = decoder.readInt();
		String domain = manager.getDomain();
		for (int i = 0; i < size; i++) {
			try {
				String player = decoder.readStr();
				Module module = manager.createEmptyModule();
				if (manager.supportedWorld())
					decodeSecure(() -> manager.decodeWorld(module, decoder.readBytes()), decoder, domain);
				if (manager.supportedGlobal())
					decodeSecure(() -> manager.decodeGlobal(module, decoder.readBytes()), decoder, domain);
				if (manager.supportedMemory())
					decodeSecure(() -> manager.decodeMemory(module, decoder.readBytes()), decoder, domain);
				Player mplayer = MinecraftServer.mcServer.getConfigurationManager().getPlayerByUsername(player);
				if (mplayer == null) continue;
				manager.setModule(mplayer, module);
			} catch (Throwable throwable) {
				Log.MAIN.error("Error on read nbt data, domain " + datapack.getDomain() + " module manager " + datapack.moduleManager(), throwable);
			}
		}
	}

	private static void decodeSecure(Runnable runnable, Decoder decoder, String domain){
		if(!decoder.readBoolean())return;
		try{
			runnable.run();
		}catch (Throwable error){
			Log.MAIN.error("Error on write nbt data domain: " + domain, error);
		}
	}
}
