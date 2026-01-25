/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.api.resource.v1;

import org.jetbrains.annotations.ApiStatus;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.fabric.impl.resource.ResourceLoaderImpl;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_3264;
import net.minecraft.class_3302;
import net.minecraft.class_7225;
import net.minecraft.class_7699;

/**
 * Provides various hooks into the resource loader.
 */
@ApiStatus.NonExtendable
public interface ResourceLoader {
	/**
	 * The resource reloader store key for the registry lookup.
	 *
	 * @apiNote The registry lookup is only available in {@linkplain class_3264#field_14190 server data} resource reloaders.
	 */
	class_3302.class_11559<class_7225.class_7874> RELOADER_REGISTRY_LOOKUP_KEY = new class_3302.class_11559<>();
	/**
	 * The resource reloader store key for the currently enabled feature set.
	 *
	 * @apiNote The feature set is only available in {@linkplain class_3264#field_14190 server data} resource reloaders.
	 */
	class_3302.class_11559<class_7699> RELOADER_FEATURE_SET_KEY = new class_3302.class_11559<>();

	static ResourceLoader get(class_3264 type) {
		return ResourceLoaderImpl.get(type);
	}

	/**
	 * Registers a resource reloader for a given resource manager type.
	 *
	 * @param id the identifier of the resource reloader
	 * @param reloader the resource reloader
	 * @see #addReloaderOrdering(class_2960, class_2960)
	 */
	void registerReloader(class_2960 id, class_3302 reloader);

	/**
	 * Requests that resource reloaders registered as the first identifier is applied before the other referenced resource reloader.
	 *
	 * <p>Incompatible ordering constraints such as cycles will lead to inconsistent behavior:
	 * some constraints will be respected and some will be ignored. If this happens, a warning will be logged.
	 *
	 * <p>Please keep in mind that this only takes effect during the application stage!
	 *
	 * @param firstReloader  the identifier of the resource reloader that should run before the other
	 * @param secondReloader the identifier of the resource reloader that should run after the other
	 * @see net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys identifiers of Vanilla resource reloaders
	 * @see #registerReloader(class_2960, class_3302) register a new resource reloader
	 */
	void addReloaderOrdering(class_2960 firstReloader, class_2960 secondReloader);

	/**
	 * Registers a built-in resource pack.
	 *
	 * <p>A built-in resource pack is an extra resource pack provided by your mod which is not always active, it's similar to the "Programmer Art" resource pack.
	 *
	 * <p>Why and when to use it? A built-in resource pack should be used to provide extra assets/data that should be optional with your mod but still directly provided by it.
	 * For example, it could provide textures of your mod in another resolution, or could allow to provide different styles of your assets.
	 *
	 * <p>The path in which the resource pack is located is in the mod JAR file under the {@code "resourcepacks/<id path>"} directory. {@code id path} being the path specified
	 * in the identifier of this built-in resource pack.
	 *
	 * @param id             the identifier of the resource pack
	 * @param container      the mod container
	 * @param activationType the activation type of the resource pack
	 * @return {@code true} if successfully registered the resource pack, or {@code false} otherwise
	 * @see #registerBuiltinPack(class_2960, ModContainer, class_2561, PackActivationType)
	 */
	static boolean registerBuiltinPack(class_2960 id, ModContainer container, PackActivationType activationType) {
		return ResourceLoaderImpl.registerBuiltinPack(id, "resourcepacks/" + id.method_12832(), container, activationType);
	}

	/**
	 * Registers a built-in resource pack.
	 *
	 * <p>A built-in resource pack is an extra resource pack provided by your mod which is not always active, it's similar to the "Programmer Art" resource pack.
	 *
	 * <p>Why and when to use it? A built-in resource pack should be used to provide extra assets/data that should be optional with your mod but still directly provided by it.
	 * For example, it could provide textures of your mod in another resolution, or could allow to provide different styles of your assets.
	 *
	 * <p>The path in which the resource pack is located is in the mod JAR file under the {@code "resourcepacks/<id path>"} directory. {@code id path} being the path specified
	 * in the identifier of this built-in resource pack.
	 *
	 * @param id             the identifier of the resource pack
	 * @param container      the mod container
	 * @param displayName    the display name of the resource pack, should include mod name for clarity
	 * @param activationType the activation type of the resource pack
	 * @return {@code true} if successfully registered the resource pack, or {@code false} otherwise
	 * @see #registerBuiltinPack(class_2960, ModContainer, PackActivationType)
	 */
	static boolean registerBuiltinPack(class_2960 id, ModContainer container, class_2561 displayName, PackActivationType activationType) {
		return ResourceLoaderImpl.registerBuiltinPack(id, "resourcepacks/" + id.method_12832(), container, displayName, activationType);
	}
}
