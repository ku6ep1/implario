package net.minecraft.server.network;

import net.minecraft.network.protocol.Protocol;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.INetHandlerHandshakeServer;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.server.S00PacketDisconnect;
import net.minecraft.network.protocol.Protocols;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.chat.ChatComponentText;
import net.minecraft.util.IChatComponent;

public class NetHandlerHandshakeTCP implements INetHandlerHandshakeServer {

	private final MinecraftServer server;
	private final NetworkManager networkManager;

	public NetHandlerHandshakeTCP(MinecraftServer serverIn, NetworkManager netManager) {
		this.server = serverIn;
		this.networkManager = netManager;
	}

	/**
	 * There are two recognized intentions for initiating a handshake: logging in and acquiring server status. The
	 * NetworkManager's protocol will be reconfigured according to the specified intention, although a login-intention
	 * must pass a versioncheck or receive a disconnect otherwise
	 */
	public void processHandshake(C00Handshake packetIn) {
		Protocol r = packetIn.getRequestedState();
		if (r == Protocols.LOGIN) {
			this.networkManager.setConnectionState(Protocols.LOGIN);

			if (packetIn.getProtocolVersion() != 47) {
				ChatComponentText chatcomponenttext = new ChatComponentText("Этот сервер использует протокол §eNotchian 47§f (версия 1.8.8)\n" +
						"§fВаш клиент использует протокол §eNotchian " + packetIn.getProtocolVersion() + "§f, который несовместим с Notchian 47.\n§f\n" +
						"Используйте клиент §eImplario§f для входа на этот сервер.\n§7github.com/DelfikPro/Implario");
				this.networkManager.sendPacket(new S00PacketDisconnect(chatcomponenttext));
				this.networkManager.closeChannel(chatcomponenttext);
			} else {
				this.networkManager.setNetHandler(new NetHandlerLoginServer(this.server, this.networkManager));
			}
		} else if (r == Protocols.STATUS) {
			this.networkManager.setConnectionState(Protocols.STATUS);
			this.networkManager.setNetHandler(new NetHandlerStatusServer(this.server, this.networkManager));
		} else {
			ChatComponentText chatcomponenttext = new ChatComponentText("§4Критическая ошибка:\n§cОтправленный статус хендшейка невозможно обработать (§f" + r + "§c)");
			this.networkManager.sendPacket(new S00PacketDisconnect(chatcomponenttext));
			this.networkManager.closeChannel(chatcomponenttext);
			throw new UnsupportedOperationException("Invalid intention " + r);
		}
	}

	/**
	 * Invoked when disconnecting, the parameter is a ChatComponent describing the reason for termination
	 */
	public void onDisconnect(IChatComponent reason) {
	}

}
