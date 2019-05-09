package net.minecraft.server.integrated;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import net.minecraft.Logger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ThreadLanServerPing;
import net.minecraft.client.settings.Settings;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Profiler;
import net.minecraft.util.CryptManager;
import net.minecraft.util.HttpUtil;
import net.minecraft.util.Util;
import net.minecraft.world.*;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import optifine.WorldServerOF;

import java.io.File;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import static net.minecraft.logging.Log.MAIN;

public class IntegratedServer extends MinecraftServer {

	private static final Logger logger = Logger.getInstance();

	/**
	 * The Minecraft instance.
	 */
	private final Minecraft mc;
	private final WorldSettings theWorldSettings;
	private boolean isGamePaused;
	private boolean isPublic;
	private ThreadLanServerPing lanServerPing;


	public IntegratedServer(Minecraft mcIn) {
		super(mcIn.getProxy(), USER_CACHE_FILE);
		this.mc = mcIn;
		this.theWorldSettings = null;
	}

	public IntegratedServer(Minecraft mcIn, String folderName, String worldName, WorldSettings settings) {
		super(new File(mcIn.mcDataDir, "saves"), mcIn.getProxy(), USER_CACHE_FILE);
		this.setServerOwner(mcIn.getSession().getUsername());
		this.setFolderName(folderName);
		this.setWorldName(worldName);
		this.canCreateBonusChest(settings.isBonusChestEnabled());
		this.setBuildLimit(256);
		this.setConfigManager(new IntegratedPlayerList(this));
		this.mc = mcIn;
		this.theWorldSettings = settings;
	}

	protected ServerCommandManager createNewCommandManager() {
		return new IntegratedServerCommandManager();
	}

	protected void loadAllWorlds(String p_71247_1_, String p_71247_2_, long seed, WorldType type, String p_71247_6_) {
		this.convertMapIfNeeded(p_71247_1_);
		ISaveHandler isavehandler = this.getActiveAnvilConverter().getSaveLoader(p_71247_1_, true);
		this.setResourcePackFromWorld(this.getFolderName(), isavehandler);
		WorldInfo worldinfo = isavehandler.loadWorldInfo();
		this.worldServers = new WorldServer[3];
		this.timeOfLastDimensionTick = new long[this.worldServers.length][100];
		this.setResourcePackFromWorld(this.getFolderName(), isavehandler);

		if (worldinfo == null) {
			worldinfo = new WorldInfo(this.theWorldSettings, p_71247_2_);
		} else {
			worldinfo.setWorldName(p_71247_2_);
		}

		for (int l = 0; l < this.worldServers.length; ++l) {
			byte b0 = 0;

			if (l == 1) {
				b0 = -1;
			}

			if (l == 2) {
				b0 = 1;
			}

			if (l == 0) {
				this.worldServers[l] = (WorldServer) new WorldServerOF(this, isavehandler, worldinfo, b0, Profiler.in).init();

				this.worldServers[l].initialize(this.theWorldSettings);
			} else {
				this.worldServers[l] = (WorldServer) new WorldServerMulti(this, isavehandler, b0, this.worldServers[0], Profiler.in).init();
			}

			this.worldServers[l].addWorldAccess(new WorldManager(this, this.worldServers[l]));
		}

		this.getConfigurationManager().setPlayerManager(this.worldServers);

		if (this.worldServers[0].getWorldInfo().getDifficulty() == null) {
			this.setDifficultyForAllWorlds(Settings.difficulty);
		}


		this.initialWorldChunkLoad();
	}

	/**
	 * Initialises the server and starts it.
	 */
	protected boolean startServer() {
		logger.info("Запуск виртуального сервера...");
		this.setOnlineMode(true);
		this.setCanSpawnAnimals(true);
		this.setCanSpawnNPCs(true);
		this.setAllowPvp(true);
		this.setAllowFlight(true);
		logger.info("Генерация ключей шифрования...");
		this.setKeyPair(CryptManager.generateKeyPair());


		this.loadAllWorlds(this.getFolderName(), this.getWorldName(), this.theWorldSettings.getSeed(), this.theWorldSettings.getTerrainType(), this.theWorldSettings.getWorldName());
		this.setMOTD(this.getServerOwner() + " - " + this.worldServers[0].getWorldInfo().getWorldName());


		return true;
	}

	/**
	 * Main function called by run() every loop.
	 */
	public void tick() {
		boolean flag = this.isGamePaused;
		this.isGamePaused = Minecraft.getMinecraft().getNetHandler() != null && Minecraft.getMinecraft().isGamePaused();

		if (!flag && this.isGamePaused) {
			logger.info("Сохраняем мир и ставим игру на паузу...");
			this.getConfigurationManager().saveAllPlayerData();
			this.saveAllWorlds(false);
		}

		if (this.isGamePaused) {
			Queue var3 = this.futureTaskQueue;

			synchronized (this.futureTaskQueue) {
				while (!this.futureTaskQueue.isEmpty()) {
					Util.schedule((FutureTask) this.futureTaskQueue.poll(), MAIN);
				}
			}
		} else {
			super.tick();

			if (Settings.RENDER_DISTANCE.f() != this.getConfigurationManager().getViewDistance()) {
				logger.info("Изменяем дальность прорисовки с " + getConfigurationManager().getViewDistance() + " на " + Settings.RENDER_DISTANCE.f());
				this.getConfigurationManager().setViewDistance((int) Settings.RENDER_DISTANCE.f());
			}

			if (this.mc.theWorld != null) {
				WorldInfo server = this.worldServers[0].getWorldInfo();
				WorldInfo client = this.mc.theWorld.getWorldInfo();

				if (!server.isDifficultyLocked() && client.getDifficulty() != server.getDifficulty()) {
					MAIN.info("Изменяем сложность с " + server.getDifficulty() + " на " + client.getDifficulty());
					this.setDifficultyForAllWorlds(client.getDifficulty());
				} else if (client.isDifficultyLocked() && !server.isDifficultyLocked()) {
					MAIN.info("Закрепляем сложность " + client.getDifficulty());
					for (WorldServer worldserver : this.worldServers)
						if (worldserver != null) worldserver.getWorldInfo().setDifficultyLocked(true);
				}
			}
		}
	}

	public boolean canStructuresSpawn() {
		return false;
	}

	public WorldSettings.GameType getGameType() {
		return this.theWorldSettings.getGameType();
	}

	/**
	 * Get the server's difficulty
	 */
	public EnumDifficulty getDifficulty() {
		return this.mc.theWorld == null ? Settings.difficulty : this.mc.theWorld.getWorldInfo().getDifficulty();
	}

	/**
	 * Defaults to false.
	 */
	public boolean isHardcore() {
		return this.theWorldSettings.getHardcoreEnabled();
	}

	public boolean func_181034_q() {
		return true;
	}

	public boolean opsSeeConsole() {
		return true;
	}

	public File getDataDirectory() {
		return this.mc.mcDataDir;
	}

	public boolean useEpoll() {
		return false;
	}

	public boolean isDedicatedServer() {
		return false;
	}

	/**
	 * Called on exit from the main run() loop.
	 */
	protected void finalTick(CrashReport report) {
		this.mc.crashed(report);
	}

	/**
	 * Adds the server info, including from theWorldServer, to the crash report.
	 */
	public CrashReport addServerInfoToCrashReport(CrashReport report) {
		report = super.addServerInfoToCrashReport(report);
		report.getCategory().addCrashSectionCallable("Type", (Callable) () -> "Integrated Server (map_client.txt)");
		return report;
	}

	public void setDifficultyForAllWorlds(EnumDifficulty difficulty) {
		super.setDifficultyForAllWorlds(difficulty);

		if (this.mc.theWorld != null) {
			this.mc.theWorld.getWorldInfo().setDifficulty(difficulty);
		}
	}

	/**
	 * On dedicated does nothing. On integrated, sets commandsAllowedForAll, gameType and allows external connections.
	 */
	public String shareToLAN(WorldSettings.GameType type, boolean allowCheats) {
		try {
			int i = -1;

			try {
				i = HttpUtil.getSuitableLanPort();
			} catch (IOException ignored) {}

			if (i <= 0) i = 25564;

			this.getNetworkSystem().addLanEndpoint(null, i);
			logger.info("Started on " + i);
			this.isPublic = true;
			this.lanServerPing = new ThreadLanServerPing(this.getMOTD(), i + "");
			this.lanServerPing.start();
			this.getConfigurationManager().setGameType(type);
			this.getConfigurationManager().setCommandsAllowedForAll(allowCheats);
			return i + "";
		} catch (IOException var6) {
			return null;
		}
	}

	/**
	 * Saves all necessary data as preparation for stopping the server.
	 */
	public void stopServer() {
		super.stopServer();

		if (this.lanServerPing != null) {
			this.lanServerPing.interrupt();
			this.lanServerPing = null;
		}
	}

	/**
	 * Sets the serverRunning variable to false, in order to get the server to shut down.
	 */
	public void initiateShutdown() {
		Futures.getUnchecked(this.addScheduledTask(() -> {
			for (EntityPlayerMP entityplayermp : Lists.newArrayList(IntegratedServer.this.getConfigurationManager().func_181057_v())) {
				IntegratedServer.this.getConfigurationManager().playerLoggedOut(entityplayermp);
			}
		}));
		super.initiateShutdown();

		if (this.lanServerPing != null) {
			this.lanServerPing.interrupt();
			this.lanServerPing = null;
		}
	}

	public void setStaticInstance() {
		this.setInstance();
	}

	/**
	 * Returns true if this integrated server is open to LAN
	 */
	public boolean getPublic() {
		return this.isPublic;
	}

	/**
	 * Sets the game type for all worlds.
	 */
	public void setGameType(WorldSettings.GameType gameMode) {
		this.getConfigurationManager().setGameType(gameMode);
	}

	/**
	 * Return whether command blocks are enabled.
	 */
	public boolean isCommandBlockEnabled() {
		return true;
	}

	public int getOpPermissionLevel() {
		return 4;
	}

}
