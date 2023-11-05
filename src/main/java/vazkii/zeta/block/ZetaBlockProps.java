package vazkii.zeta.block;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import org.jetbrains.annotations.Nullable;
import vazkii.zeta.event.ZRegister;
import vazkii.zeta.module.ZetaModule;
import vazkii.zeta.registry.RenderLayerRegistry;

public class ZetaBlockProps {
	//To construct a BlockBehavior.Properties, you already need a block to copy, or already
	//need the material and color. So to allow setting properties in any order, we might need to
	//buffer modifications to the BlockBehavior.Properties until we are able to actually make it.
	private BlockBehaviour.Properties vanilla;
	private Material mat;
	private Object color;
	private BlockBehaviour toCopy; //TODO: allow copying the Zeta-specific props too?
	private List<Consumer<BlockBehaviour.Properties>> delayedVanilla = new ArrayList<>();

	//More props for Zeta.
	public final ZetaModule module;
	public @Nullable CreativeModeTab tab;
	public BooleanSupplier condition = () -> true;
	public boolean hasBlockItem = true;
	public @Nullable RenderLayerRegistry.Layer layer;
	public @Nullable String id;
	public @Nullable IZetaBlock parent; //TODO use this for stuff

	public ZetaBlockProps(ZetaModule module) {
		this.module = module;
	}

	//convenience
	public ZetaBlockProps(ZetaModule module, String id) {
		this(module);
		id(id);
	}

	public BlockBehaviour.Properties getVanilla() {
		if(vanilla == null) {

			if(toCopy != null)
				vanilla = BlockBehaviour.Properties.copy(toCopy);
			else {
				if(mat == null)
					throw new IllegalStateException("Need to call .material() on the ZetaProps"); //TODO: maybe just pick a default lol?

				if(color instanceof DyeColor dyec)
					vanilla = BlockBehaviour.Properties.of(mat, dyec);
				else if(color instanceof MaterialColor matc)
					vanilla = BlockBehaviour.Properties.of(mat, matc);
				else if(color instanceof Function<?, ?> func)
					vanilla = BlockBehaviour.Properties.of(mat, (Function<BlockState, MaterialColor>) func);
				else
					vanilla = BlockBehaviour.Properties.of(mat);
			}

			for(Consumer<BlockBehaviour.Properties> delay : delayedVanilla)
				delay.accept(vanilla);

			delayedVanilla = null; //good riddance!
		}

		return vanilla;
	}

	private ZetaBlockProps runOnVanilla(Consumer<BlockBehaviour.Properties> vanillaAction) {
		if(vanilla == null)
			delayedVanilla.add(vanillaAction);
		else
			vanillaAction.accept(vanilla);
		return this;
	}

	/// functionality ///

	public void register(IZetaBlock block, @Nullable ZRegister event) {
		if(event == null)
			return;

		event.getRegistry().registerBlock(block.getBlock(), id, hasBlockItem);

		if(tab != null)
			event.getRegistry().setCreativeTab(block.getBlock(), tab, block::isEnabled);

		if(layer != null)
			event.getRenderLayerRegistry().put(block.getBlock(), layer);
	}

	/// additional zeta properties ///

	public ZetaBlockProps tab(CreativeModeTab tab) {
		this.tab = tab;
		return this;
	}

	public ZetaBlockProps condition(BooleanSupplier condition) {
		this.condition = condition;
		return this;
	}

	public ZetaBlockProps layer(RenderLayerRegistry.Layer renderLayer) {
		this.layer = renderLayer;
		return this;
	}

	public ZetaBlockProps id(ResourceLocation id) {
		this.id = id.toString();
		return this;
	}

	public ZetaBlockProps id(String id) {
		this.id = id;
		return this;
	}

	public ZetaBlockProps parent(IZetaBlock parent) {
		this.parent = parent;
		return this;
	}

	public ZetaBlockProps hasBlockItem(boolean hasBlockItem) {
		this.hasBlockItem = hasBlockItem;
		return this;
	}

	/// special vanilla props ///

	private void checkVanillaNotConstructedYet() {
		if(vanilla != null)
			throw new IllegalStateException("Can't set that, the vanilla BlockBehaviour.Properties was already constructed");
	}

	public ZetaBlockProps material(Material mat) {
		checkVanillaNotConstructedYet();
		this.mat = mat;
		return this;
	}

	public ZetaBlockProps color(DyeColor color) {
		checkVanillaNotConstructedYet();
		this.color = color;
		return this;
	}

	public ZetaBlockProps color(MaterialColor color) {
		checkVanillaNotConstructedYet();
		this.color = color;
		return this;
	}

	public ZetaBlockProps color(Function<BlockState, MaterialColor> color) {
		checkVanillaNotConstructedYet();
		this.color = color;
		return this;
	}

	public ZetaBlockProps copy(BlockBehaviour toCopy) {
		checkVanillaNotConstructedYet();
		this.toCopy = toCopy;
		return this;
	}

	/// and the rest is delegation to vanilla properties. TODO sort ///

	public ZetaBlockProps noCollission() {
		return runOnVanilla(BlockBehaviour.Properties::noCollission);
	}

	public ZetaBlockProps noOcclusion() {
		return runOnVanilla(BlockBehaviour.Properties::noOcclusion);
	}

	public ZetaBlockProps strength(float str) {
		return runOnVanilla(p -> p.strength(str));
	}

	public ZetaBlockProps sound(SoundType type) {
		return runOnVanilla(p -> p.sound(type));
	}

	public ZetaBlockProps randomTicks() {
		return runOnVanilla(BlockBehaviour.Properties::randomTicks);
	}

	public ZetaBlockProps lightLevel(ToIntFunction<BlockState> emission) {
		return runOnVanilla(p -> p.lightLevel(emission));
	}

	//convenience
	public ZetaBlockProps lightLevel(int constLevel) {
		return lightLevel(state -> constLevel);
	}

	public ZetaBlockProps hasPostProcess(BlockBehaviour.StatePredicate blah) {
		return runOnVanilla(p -> p.hasPostProcess(blah));
	}

	//convenience
	public ZetaBlockProps hasPostProcess() {
		return hasPostProcess((a, b, c) -> true);
	}

	public ZetaBlockProps emissiveRendering(BlockBehaviour.StatePredicate blah) {
		return runOnVanilla(p -> p.emissiveRendering(blah));
	}

	//convenience
	public ZetaBlockProps emissiveRendering() {
		return emissiveRendering((a, b, c) -> true);
	}
}
