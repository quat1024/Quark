package vazkii.zeta.event;

import vazkii.zeta.registry.BrewingRegistry;
import vazkii.zeta.Zeta;
import vazkii.zeta.event.bus.IZetaLoadEvent;
import vazkii.zeta.registry.CraftingExtensionsRegistry;
import vazkii.zeta.registry.RenderLayerRegistry;
import vazkii.zeta.registry.ZetaRegistry;

@SuppressWarnings("ClassCanBeRecord")
public class ZRegister implements IZetaLoadEvent {
	public final Zeta zeta;

	@Deprecated
	public static ZRegister SHITTY_SINGLETON_TODO_THREAD_THE_EVENT_THROUGH;

	public ZRegister(Zeta zeta) {
		SHITTY_SINGLETON_TODO_THREAD_THE_EVENT_THROUGH = this;
		this.zeta = zeta;
	}

	public ZetaRegistry getRegistry() {
		return zeta.registry;
	}

	public CraftingExtensionsRegistry getCraftingExtensionsRegistry() {
		return zeta.craftingExtensions;
	}

	public BrewingRegistry getBrewingRegistry() {
		return zeta.brewingRegistry;
	}

	public RenderLayerRegistry getRenderLayerRegistry() {
		return zeta.renderLayerRegistry;
	}

	public static class Post implements IZetaLoadEvent { }
}
