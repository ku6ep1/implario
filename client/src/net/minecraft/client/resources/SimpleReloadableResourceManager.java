package net.minecraft.client.resources;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.Logger;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.util.ResourceLocation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SimpleReloadableResourceManager implements IReloadableResourceManager {

	private static final Logger logger = Logger.getInstance();
	private static final Joiner joinerResourcePacks = Joiner.on(", ");
	private final Map<String, FallbackResourceManager> domainResourceManagers = Maps.newHashMap();
	private final List<IResourceManagerReloadListener> reloadListeners = Lists.newArrayList();
	private final Set<String> setResourceDomains = Sets.newLinkedHashSet();
	private final IMetadataSerializer rmMetadataSerializer;

	public SimpleReloadableResourceManager(IMetadataSerializer rmMetadataSerializerIn) {
		this.rmMetadataSerializer = rmMetadataSerializerIn;
	}

	public void reloadResourcePack(IResourcePack resourcePack) {
		for (String s : resourcePack.getResourceDomains()) {
			this.setResourceDomains.add(s);
			FallbackResourceManager fallbackresourcemanager = this.domainResourceManagers.get(s);

			if (fallbackresourcemanager == null) {
				fallbackresourcemanager = new FallbackResourceManager(this.rmMetadataSerializer);
				this.domainResourceManagers.put(s, fallbackresourcemanager);
			}

			fallbackresourcemanager.addResourcePack(resourcePack);
		}
	}

	public Set<String> getResourceDomains() {
		return this.setResourceDomains;
	}

	public IResource getResource(ResourceLocation location) throws IOException {
		IResourceManager iresourcemanager = this.domainResourceManagers.get(location.getResourceDomain());

		if (iresourcemanager != null) {
			return iresourcemanager.getResource(location);
		}
		throw new FileNotFoundException(location.toString());
	}

	public List<IResource> getAllResources(ResourceLocation location) throws IOException {
		IResourceManager iresourcemanager = this.domainResourceManagers.get(location.getResourceDomain());

		if (iresourcemanager != null) {
			return iresourcemanager.getAllResources(location);
		}
		throw new FileNotFoundException(location.toString());
	}

	private void clearResources() {
		this.domainResourceManagers.clear();
		this.setResourceDomains.clear();
	}

	public void reloadResources(List<IResourcePack> p_110541_1_) {
		this.clearResources();
		logger.info("Загрузка ресурс-пака '" + joinerResourcePacks.join(Iterables.transform(p_110541_1_, IResourcePack::getPackName)) + "'");

		for (IResourcePack iresourcepack : p_110541_1_) {
			this.reloadResourcePack(iresourcepack);
		}

		this.notifyReloadListeners();
	}

	public void registerReloadListener(IResourceManagerReloadListener reloadListener) {
		this.reloadListeners.add(reloadListener);
		reloadListener.onResourceManagerReload(this);
	}

	private void notifyReloadListeners() {
		for (IResourceManagerReloadListener iresourcemanagerreloadlistener : this.reloadListeners) {
			iresourcemanagerreloadlistener.onResourceManagerReload(this);
		}
	}

}
