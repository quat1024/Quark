package vazkii.zeta.module;

import java.util.List;
import java.util.Set;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.loading.FMLEnvironment;
import vazkii.zeta.Zeta;
import vazkii.zeta.block.ZetaBlockProps;
import vazkii.zeta.event.ZGatherHints;
import vazkii.zeta.event.bus.PlayEvent;

public class ZetaModule {
	public Zeta zeta;
	public ZetaCategory category;

	public String displayName = "";
	public String lowercaseName = "";
	public String description = "";

	public Set<String> antiOverlap = Set.of();

	public boolean enabled = false;
	public boolean enabledByDefault = false;
	public boolean disabledByOverlap = false;
	public boolean ignoreAntiOverlap = false;

	//Purely for syntactic convenience
	public final ZetaBlockProps props() {
		return new ZetaBlockProps(this);
	}

	public final ZetaBlockProps props(String id) {
		return new ZetaBlockProps(this).id(id);
	}

	public void postConstruct() {
		// NO-OP
	}

	public final void setEnabled(Zeta z, boolean willEnable) {
		//TODO: is this the right approach for handling category enablement :woozy_face:
		if(z.configManager != null && !z.configManager.isCategoryEnabled(category))
			willEnable = false;

		if(category != null && !category.requiredModsLoaded(z))
			willEnable = false;

		if(!ignoreAntiOverlap && antiOverlap.stream().anyMatch(z::isModLoaded)) {
			disabledByOverlap = true;
			willEnable = false;
		} else
			disabledByOverlap = false;

		setEnabledAndManageSubscriptions(z, willEnable);
	}

	private void setEnabledAndManageSubscriptions(Zeta z, boolean nowEnabled) {
		if(this.enabled == nowEnabled)
			return;
		this.enabled = nowEnabled;

		//TODO: Cheap hacks to keep non-Zeta Quark modules on life support.
		// When all the Forge events are removed, this "if" can be unwrapped.
		if(LEGACY_doForgeEventBusSubscription(nowEnabled)) {
			if(nowEnabled)
				z.playBus.subscribe(this.getClass()).subscribe(this);
			else
				z.playBus.unsubscribe(this.getClass()).unsubscribe(this);
		}
	}

	@PlayEvent
	public final void addAnnotationHints(ZGatherHints event) {
		event.gatherHintsFromModule(this, zeta.configManager.getConfigFlagManager());
	}

	@Deprecated public boolean LEGACY_hasSubscriptions = false;
	@Deprecated public List<Dist> LEGACY_subscriptionTarget = null;

	private boolean LEGACY_doForgeEventBusSubscription(boolean nowEnabled) {
		if(LEGACY_subscriptionTarget == null) return true;

		if(LEGACY_hasSubscriptions && LEGACY_subscriptionTarget.contains(FMLEnvironment.dist)) {
			if(nowEnabled)
				MinecraftForge.EVENT_BUS.register(this);
			else
				MinecraftForge.EVENT_BUS.unregister(this);
		}

		return LEGACY_subscriptionTarget.contains(FMLEnvironment.dist);
	}
}
