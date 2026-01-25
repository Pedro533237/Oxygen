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

package net.fabricmc.fabric.api.resource.v1.reloader;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.class_3302;

/**
 * A variant of {@link net.minecraft.class_4080}
 * which passes the shared state store instead of the resource manager in its methods.
 *
 * <p>In essence, there are two stages:
 *
 * <ul><li>prepare: create an instance of your data object containing all loaded and
 * processed information,
 * <li>apply: apply the information from the data object to the game instance.</ul>
 *
 * <p>The prepare stage should be self-contained as it can run on any thread! However,
 * the apply stage is guaranteed to run on the game thread.
 *
 * <p>For a fully synchronous alternative, consider using
 * {@link net.minecraft.class_4013}.
 *
 * @param <T> the data object
 */
public abstract class SimpleResourceReloader<T> implements class_3302 {
	public final CompletableFuture<Void> method_25931(class_11558 store, Executor prepareExecutor, class_4045 reloadSynchronizer, Executor applyExecutor) {
		CompletableFuture<T> prepareStep = CompletableFuture.supplyAsync(() -> this.prepare(store), prepareExecutor);
		Objects.requireNonNull(reloadSynchronizer);
		return prepareStep.thenCompose(reloadSynchronizer::method_18352)
				.thenAcceptAsync((prepared) -> this.apply(prepared, store), applyExecutor);
	}

	/**
	 * Asynchronously processes and prepares resource-based data.
	 * The code must be thread-safe and not modify game state!
	 *
	 * @param store the data store used for sharing state between resource reloaders
	 * @return the prepared data
	 */
	protected abstract T prepare(class_11558 store);

	/**
	 * Synchronously applies prepared data to the game state.
	 *
	 * @param prepared the prepared data
	 * @param store the data store used for sharing state between resource reloaders
	 */
	protected abstract void apply(T prepared, class_11558 store);
}
