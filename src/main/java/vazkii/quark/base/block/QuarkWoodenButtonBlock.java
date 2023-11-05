package vazkii.quark.base.block;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import vazkii.zeta.block.ZetaBlockProps;
import vazkii.zeta.block.ZetaButtonBlock;
import vazkii.zeta.event.ZRegister;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class QuarkWoodenButtonBlock extends ZetaButtonBlock {

	public QuarkWoodenButtonBlock(@Nullable ZRegister event, ZetaBlockProps props) {
		super(event, props.tab(CreativeModeTab.TAB_REDSTONE)
			.material(Material.DECORATION)
			.noCollission()
			.strength(0.5f)
			.sound(SoundType.WOOD));
	}

	public QuarkWoodenButtonBlock(ZetaBlockProps props) {
		this(ZRegister.SHITTY_SINGLETON_TODO_THREAD_THE_EVENT_THROUGH, props);
	}

	@Nonnull
	@Override
	protected SoundEvent getSound(boolean powered) {
		return powered ? SoundEvents.WOODEN_BUTTON_CLICK_ON : SoundEvents.WOODEN_BUTTON_CLICK_OFF;
	}

	@Override
	public int getPressDuration() {
		return 30;
	}

}
