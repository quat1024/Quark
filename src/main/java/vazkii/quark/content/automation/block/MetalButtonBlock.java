package vazkii.quark.content.automation.block;

import javax.annotation.Nonnull;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import vazkii.zeta.block.ZetaButtonBlock;
import vazkii.zeta.block.ZetaBlockProps;
import vazkii.zeta.event.ZRegister;

/**
 * @author WireSegal
 * Created at 9:14 PM on 10/8/19.
 */
public class MetalButtonBlock extends ZetaButtonBlock {

	private final int speed;

	public MetalButtonBlock(int speed, ZetaBlockProps props) {
		super(props
			.material(Material.METAL)
			.sound(SoundType.METAL)
			.tab(CreativeModeTab.TAB_REDSTONE)
			.noCollission()
			.strength(0.5f));
		this.speed = speed;
	}

	public MetalButtonBlock(ZRegister event, int speed, ZetaBlockProps props) {
		this(speed, props);
		props.register(this, event);
	}

	@Override
	public int getPressDuration() {
		return speed;
	}

	@Nonnull
	@Override
	protected SoundEvent getSound(boolean powered) {
		return powered ? SoundEvents.STONE_BUTTON_CLICK_ON : SoundEvents.STONE_BUTTON_CLICK_OFF;
	}
}
