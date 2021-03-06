/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 *
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 *
 * File Created @ [26/03/2016, 21:31:04 (GMT)]
 */
package vazkii.quark.base.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.LogManager;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

public class ClassTransformer implements IClassTransformer, Opcodes {

	private static final String ASM_HOOKS = "vazkii/quark/base/asm/ASMHooks";

	private static final Map<String, Transformer> transformers = new HashMap<>();

	static {
		// For Emotes
		transformers.put("net.minecraft.client.model.ModelBiped", ClassTransformer::transformModelBiped);

		// For Color Runes
		transformers.put("net.minecraft.client.renderer.RenderItem", ClassTransformer::transformRenderItem);
		transformers.put("net.minecraft.client.renderer.entity.layers.LayerArmorBase", ClassTransformer::transformLayerArmorBase);

		// For Boat Sails
		transformers.put("net.minecraft.client.renderer.entity.RenderBoat", ClassTransformer::transformRenderBoat);
		transformers.put("net.minecraft.entity.item.EntityBoat", ClassTransformer::transformEntityBoat);

		// For Piston Block Breakers and Pistons Move TEs
		transformers.put("net.minecraft.block.BlockPistonBase", ClassTransformer::transformBlockPistonBase);

		// For Better Craft Shifting
		transformers.put("net.minecraft.inventory.ContainerWorkbench", ClassTransformer::transformContainerWorkbench);
		transformers.put("net.minecraft.inventory.ContainerMerchant", ClassTransformer::transformContainerMerchant);

		// For Pistons Move TEs
		transformers.put("net.minecraft.tileentity.TileEntityPiston", ClassTransformer::transformTileEntityPiston);
		transformers.put("net.minecraft.client.renderer.tileentity.TileEntityPistonRenderer", ClassTransformer::transformTileEntityPistonRenderer);

		// For Imrpoved Sleeping
		transformers.put("net.minecraft.world.WorldServer", ClassTransformer::transformWorldServer);

		// For Colored Lights
		transformers.put("net.minecraft.client.renderer.BlockModelRenderer", ClassTransformer::transformBlockModelRenderer);

		// For More Banner Layers
		transformers.put("net.minecraft.item.crafting.RecipesBanners$RecipeAddPattern", ClassTransformer::transformRecipeAddPattern);
		transformers.put("net.minecraft.item.ItemBanner", ClassTransformer::transformItemBanner);

		// Better Fire Effect
		transformers.put("net.minecraft.client.renderer.entity.Render", ClassTransformer::transformRender);
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (transformers.containsKey(transformedName)) {
			log("Transforming " + transformedName);
			return transformers.get(transformedName).apply(basicClass);
		}

		return basicClass;
	}

	private static byte[] transformModelBiped(byte[] basicClass) {
		MethodSignature sig = new MethodSignature("setRotationAngles", "func_78087_a", "(FFFFFFLnet/minecraft/entity/Entity;)V");

		return transform(basicClass, forMethod(sig, combine(
				(AbstractInsnNode node) -> { // Filter
					return node.getOpcode() == RETURN;
				},
				(MethodNode method, AbstractInsnNode node) -> { // Action
					InsnList newInstructions = new InsnList();

					newInstructions.add(new VarInsnNode(ALOAD, 7));
					newInstructions.add(new MethodInsnNode(INVOKESTATIC, ASM_HOOKS, "updateEmotes", "(Lnet/minecraft/entity/Entity;)V", false));

					method.instructions.insertBefore(node, newInstructions);
					return true;
				})));
	}

	private static byte[] transformRenderItem(byte[] basicClass) {
		MethodSignature sig1 = new MethodSignature("renderItem", "func_180454_a", "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/block/model/IBakedModel;)V");
		MethodSignature sig2 = new MethodSignature("renderEffect", "func_191966_a", "(Lnet/minecraft/client/renderer/block/model/IBakedModel;)V");

		byte[] transClass = basicClass;

		transClass = transform(transClass, forMethod(sig1,
				(MethodNode method) -> { // Action
					InsnList newInstructions = new InsnList();

					newInstructions.add(new VarInsnNode(ALOAD, 1));
					newInstructions.add(new MethodInsnNode(INVOKESTATIC, ASM_HOOKS, "setColorRuneTargetStack", "(Lnet/minecraft/item/ItemStack;)V", false));

					method.instructions.insertBefore(method.instructions.getFirst(), newInstructions);
					return true;
				}), forMethod(sig2, combine(
				(AbstractInsnNode node) -> { // Filter
					return node.getOpcode() == LDC && ((LdcInsnNode) node).cst.equals(-8372020);
				}, (MethodNode method, AbstractInsnNode node) -> { // Action
					InsnList newInstructions = new InsnList();

					newInstructions.add(new MethodInsnNode(INVOKESTATIC, ASM_HOOKS, "getRuneColor", "(I)I", false));

					method.instructions.insert(node, newInstructions);
					return false;
				})));

		return transClass;
	}

	private static byte[] transformLayerArmorBase(byte[] basicClass) {
		MethodSignature sig1 = new MethodSignature("renderArmorLayer", "func_188361_a", "(Lnet/minecraft/entity/EntityLivingBase;FFFFFFFLnet/minecraft/inventory/EntityEquipmentSlot;)V");
		MethodSignature sig2 = new MethodSignature("renderEnchantedGlint", "func_188364_a", "(Lnet/minecraft/client/renderer/entity/RenderLivingBase;Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/client/model/ModelBase;FFFFFFF)V");

		MethodSignature target = new MethodSignature("color", "", "(FFFF)V");

		byte[] transClass = basicClass;

		transClass = transform(transClass, forMethod(sig1, (MethodNode method) -> { // Action
			InsnList newInstructions = new InsnList();

			newInstructions.add(new VarInsnNode(ALOAD, 1));
			newInstructions.add(new VarInsnNode(ALOAD, 9));
			newInstructions.add(new MethodInsnNode(INVOKESTATIC, ASM_HOOKS, "setColorRuneTargetStack", "(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/inventory/EntityEquipmentSlot;)V", false));

			method.instructions.insertBefore(method.instructions.getFirst(), newInstructions);
			return true;
		}));

		if (!hasOptifine(sig2.toString())) {
			transClass = transform(transClass, forMethod(sig2, combine(
					(AbstractInsnNode node) -> { // Filter
						return node.getOpcode() == INVOKESTATIC && target.matches((MethodInsnNode) node);
					},
					(MethodNode method, AbstractInsnNode node) -> { // Action

						InsnList newInstructions = new InsnList();

						newInstructions.add(new MethodInsnNode(INVOKESTATIC, ASM_HOOKS, "applyRuneColor", "()V", false));

						method.instructions.insert(node, newInstructions);
						return false;
					})));
		}

		return transClass;
	}

	private static byte[] transformEntityBoat(byte[] basicClass) {
		MethodSignature sig1 = new MethodSignature("attackEntityFrom", "func_70097_a", "(Lnet/minecraft/util/DamageSource;F)Z");
		MethodSignature sig2 = new MethodSignature("onUpdate", "func_70071_h_", "()V");

		MethodSignature target = new MethodSignature("dropItemWithOffset", "func_145778_a", "(Lnet/minecraft/item/Item;IF)Lnet/minecraft/entity/item/EntityItem;");

		byte[] transClass = transform(basicClass, forMethod(sig1, combine(
				(AbstractInsnNode node) -> { // Filter
					return node.getOpcode() == INVOKEVIRTUAL && target.matches((MethodInsnNode) node);
				},
				(MethodNode method, AbstractInsnNode node) -> { // Action
					InsnList newInstructions = new InsnList();

					newInstructions.add(new VarInsnNode(ALOAD, 0));
					newInstructions.add(new MethodInsnNode(INVOKESTATIC, ASM_HOOKS, "dropBoatBanner", "(Lnet/minecraft/entity/item/EntityBoat;)V", false));

					method.instructions.insert(node, newInstructions);
					return true;
				})));

		transClass = transform(transClass, forMethod(sig2, (MethodNode method) -> { // Action
			InsnList newInstructions = new InsnList();

			newInstructions.add(new VarInsnNode(ALOAD, 0));
			newInstructions.add(new MethodInsnNode(INVOKESTATIC, ASM_HOOKS, "onBoatUpdate", "(Lnet/minecraft/entity/item/EntityBoat;)V", false));

			method.instructions.insertBefore(method.instructions.getFirst(), newInstructions);
			return true;
		}));

		return transClass;
	}

	private static byte[] transformRenderBoat(byte[] basicClass) {
		MethodSignature sig = new MethodSignature("doRender", "func_188300_b", "(Lnet/minecraft/entity/item/EntityBoat;DDDFF)V");

		MethodSignature target = new MethodSignature("render", "func_78088_a", "(Lnet/minecraft/entity/Entity;FFFFFF)V");

		return transform(basicClass, forMethod(sig, combine(
				(AbstractInsnNode node) -> { // Filter
					return node.getOpcode() == INVOKEVIRTUAL && target.matches((MethodInsnNode) node);
				},
				(MethodNode method, AbstractInsnNode node) -> { // Action
					InsnList newInstructions = new InsnList();

					newInstructions.add(new VarInsnNode(ALOAD, 1));
					newInstructions.add(new VarInsnNode(FLOAD, 9));
					newInstructions.add(new MethodInsnNode(INVOKESTATIC, ASM_HOOKS, "renderBannerOnBoat", "(Lnet/minecraft/entity/item/EntityBoat;F)V", false));

					method.instructions.insert(node, newInstructions);
					return true;
				})));
	}

	private static byte[] transformBlockPistonBase(byte[] basicClass) {
		MethodSignature sig1 = new MethodSignature("doMove", "func_176319_a", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;Z)Z");
		MethodSignature sig2 = new MethodSignature("canPush", "func_185646_a", "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;ZLnet/minecraft/util/EnumFacing;)Z");

		MethodSignature target = new MethodSignature("hasTileEntity", "", "(Lnet/minecraft/block/state/IBlockState;)Z");
		MethodSignature target2 = new MethodSignature("canMove", "func_177253_a", "()Z");

		byte[] transClass = transform(basicClass, forMethod(sig2, combine(
				(AbstractInsnNode node) -> { // Filter
					return node.getOpcode() == INVOKEVIRTUAL && target.matches((MethodInsnNode) node);
				},
				(MethodNode method, AbstractInsnNode node) -> { // Action
					InsnList newInstructions = new InsnList();

					newInstructions.add(new VarInsnNode(ALOAD, 0));
					newInstructions.add(new MethodInsnNode(INVOKESTATIC, ASM_HOOKS, "shouldPistonMoveTE", "(ZLnet/minecraft/block/state/IBlockState;)Z", false));

					method.instructions.insert(node, newInstructions);
					return true;
				})));

		return transform(transClass, forMethod(sig1, combine(
				(AbstractInsnNode node) -> { // Filter
					return node.getOpcode() == INVOKEVIRTUAL && target2.matches((MethodInsnNode) node);
				},
				(MethodNode method, AbstractInsnNode node) -> { // Action
					InsnList newInstructions = new InsnList();

					newInstructions.add(new VarInsnNode(ALOAD, 1));
					newInstructions.add(new VarInsnNode(ALOAD, 2));
					newInstructions.add(new VarInsnNode(ALOAD, 5));
					newInstructions.add(new VarInsnNode(ALOAD, 3));
					newInstructions.add(new VarInsnNode(ILOAD, 4));
					newInstructions.add(new MethodInsnNode(INVOKESTATIC, ASM_HOOKS, "onPistonMove", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/BlockPistonStructureHelper;Lnet/minecraft/util/EnumFacing;Z)V", false));

					method.instructions.insert(node, newInstructions);
					return true;
				})));
	}

	private static byte[] transformContainerWorkbench(byte[] basicClass) {
		return transformTransferStackInSlot(basicClass, "getMinInventoryBoundaryCrafting", "getMaxInventoryBoundaryCrafting");
	}

	private static byte[] transformContainerMerchant(byte[] basicClass) {
		return transformTransferStackInSlot(basicClass, "getMinInventoryBoundaryVillager", "getMaxInventoryBoundaryVillager");
	}

	private static byte[] transformTransferStackInSlot(byte[] basicClass, String firstHook, String secondHook) {
		MethodSignature sig = new MethodSignature("transferStackInSlot", "func_82846_b", "(Lnet/minecraft/entity/player/EntityPlayer;I)Lnet/minecraft/item/ItemStack;");

		MethodSignature target = new MethodSignature("mergeItemStack", "func_75135_a", "(Lnet/minecraft/item/ItemStack;IIZ)Z");

		return transform(basicClass, forMethod(sig, combine(
				(AbstractInsnNode node) -> { // Filter
					return node.getOpcode() == INVOKEVIRTUAL && target.matches((MethodInsnNode) node);
				},
				(MethodNode method, AbstractInsnNode node) -> { // Action
					InsnList newInstructions = new InsnList();

					// Stack is at
					// IiZ
					// We need to modify I, i, while preserving Z
					// We also need both I as input for the methods
					// 1 will refer to the output of the first hook, and 2 to the second hook.
					// Our stack needs to end as 12Z

					// Stack state: IiZ
					newInstructions.add(new InsnNode(DUP_X2));
					newInstructions.add(new InsnNode(POP));
					// Stack state: ZIi
					newInstructions.add(new InsnNode(DUP2));
					// Stack state: ZIiIi
					newInstructions.add(new MethodInsnNode(INVOKESTATIC, ASM_HOOKS, firstHook, "(II)I", false));
					// Stack state: ZIi1
					newInstructions.add(new InsnNode(DUP_X2));
					newInstructions.add(new InsnNode(POP));
					// Stack state: Z1Ii
					newInstructions.add(new MethodInsnNode(INVOKESTATIC, ASM_HOOKS, secondHook, "(II)I", false));
					// Stack state: Z12
					newInstructions.add(new InsnNode(DUP_X2));
					newInstructions.add(new InsnNode(POP));
					// Stack state: 2Z1
					newInstructions.add(new InsnNode(DUP_X2));
					newInstructions.add(new InsnNode(POP));
					// Stack state: 12Z

					method.instructions.insertBefore(node, newInstructions);
					return false;
				})));
	}

	private static byte[] transformTileEntityPiston(byte[] basicClass) {
		MethodSignature clearPistonTileEntitySig = new MethodSignature("clearPistonTileEntity", "func_145866_f", "()V");
		MethodSignature updateSig = new MethodSignature("update", "func_73660_a", "()V");

		MethodSignature target = new MethodSignature("setBlockState", "func_180501_a", "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z");

		MethodAction setPistonBlockAction = combine(
				(AbstractInsnNode node) -> { // Filter
					return node.getOpcode() == INVOKEVIRTUAL && target.matches((MethodInsnNode) node);
				},
				(MethodNode method, AbstractInsnNode node) -> { // Action
					InsnList newInstructions = new InsnList();

					newInstructions.add(new MethodInsnNode(INVOKESTATIC, ASM_HOOKS, "setPistonBlock", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z", false));

					method.instructions.insert(node, newInstructions);
					method.instructions.remove(node);

					return true;
				});

		MethodAction onUpdateAction = (MethodNode method) -> { // Action
			InsnList newInstructions = new InsnList();

			newInstructions.add(new VarInsnNode(ALOAD, 0));
			newInstructions.add(new MethodInsnNode(INVOKESTATIC, ASM_HOOKS, "onPistonUpdate", "(Lnet/minecraft/tileentity/TileEntityPiston;)V", false));

			method.instructions.insertBefore(method.instructions.getFirst(), newInstructions);

			return true;
		};

		byte[] transClass = basicClass;
		transClass = transform(transClass, forMethod(updateSig, onUpdateAction));
		transClass = transform(transClass, forMethod(clearPistonTileEntitySig, setPistonBlockAction));
		transClass = transform(transClass, forMethod(updateSig, setPistonBlockAction));

		return transClass;
	}

	private static byte[] transformTileEntityPistonRenderer(byte[] basicClass) {
		MethodSignature sig = new MethodSignature("renderStateModel", "func_188186_a", "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/client/renderer/BufferBuilder;Lnet/minecraft/world/World;Z)Z");

		return transform(basicClass, forMethod(sig, (MethodNode method) -> { // Action
			InsnList newInstructions = new InsnList();

			newInstructions.add(new VarInsnNode(ALOAD, 1));
			newInstructions.add(new VarInsnNode(ALOAD, 2));
			newInstructions.add(new VarInsnNode(ALOAD, 3));
			newInstructions.add(new VarInsnNode(ALOAD, 4));
			newInstructions.add(new VarInsnNode(ILOAD, 5));
			newInstructions.add(new MethodInsnNode(INVOKESTATIC, ASM_HOOKS, "renderPistonBlock", "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/client/renderer/BufferBuilder;Lnet/minecraft/world/World;Z)Z", false));
			newInstructions.add(new InsnNode(IRETURN));

			method.instructions = newInstructions;
			return true;
		}));
	}

	private static byte[] transformWorldServer(byte[] basicClass) {
		MethodSignature sig = new MethodSignature("areAllPlayersAsleep", "func_73056_e", "()Z");

		return transform(basicClass, forMethod(sig, (MethodNode method) -> { // Action
			InsnList newInstructions = new InsnList();

			newInstructions.add(new VarInsnNode(ALOAD, 0));
			newInstructions.add(new MethodInsnNode(INVOKESTATIC, ASM_HOOKS, "isEveryoneAsleep", "(Lnet/minecraft/world/World;)I", false));
			newInstructions.add(new InsnNode(DUP));
			LabelNode label = new LabelNode();
			newInstructions.add(new JumpInsnNode(IFEQ, label));
			newInstructions.add(new InsnNode(ICONST_1));
			newInstructions.add(new InsnNode(ISUB));
			newInstructions.add(new InsnNode(IRETURN));
			newInstructions.add(label);
			newInstructions.add(new InsnNode(POP));

			method.instructions.insertBefore(method.instructions.getFirst(), newInstructions);
			return true;
		}));
	}

	private static byte[] transformBlockModelRenderer(byte[] basicClass) {
		MethodSignature sig1 = new MethodSignature("renderQuadsFlat", "func_187496_a", "(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;IZLnet/minecraft/client/renderer/BufferBuilder;Ljava/util/List;Ljava/util/BitSet;)V");

		MethodSignature target = new MethodSignature("putPosition", "func_178987_a", "(DDD)V");

		if (hasOptifine(sig1.toString()))
			return basicClass;

		return transform(basicClass, forMethod(sig1, combine(
				(AbstractInsnNode node) -> { // Filter
					return node.getOpcode() == INVOKEVIRTUAL && target.matches((MethodInsnNode) node);
				},
				(MethodNode method, AbstractInsnNode node) -> { // Action
					InsnList newInstructions = new InsnList();

					newInstructions.add(new VarInsnNode(ALOAD, 1));
					newInstructions.add(new VarInsnNode(ALOAD, 2));
					newInstructions.add(new VarInsnNode(ALOAD, 3));
					newInstructions.add(new VarInsnNode(ALOAD, 6));
					newInstructions.add(new VarInsnNode(ALOAD, 18));
					newInstructions.add(new VarInsnNode(ILOAD, 4));
					newInstructions.add(new MethodInsnNode(INVOKESTATIC, ASM_HOOKS, "putColorsFlat", "(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/renderer/BufferBuilder;Lnet/minecraft/client/renderer/block/model/BakedQuad;I)V", false));

					method.instructions.insertBefore(node, newInstructions);
					return true;
				})));
	}

	private static MethodSignature layerCountIndex = new MethodSignature("getPatterns", "func_175113_c", "(Lnet/minecraft/item/ItemStack;)I");

	private static MethodAction layerCountTransformer = combine(
			(AbstractInsnNode node) -> { // Filter
				return node.getOpcode() == INVOKESTATIC && layerCountIndex.matches((MethodInsnNode) node);
			},
			(MethodNode method, AbstractInsnNode node) -> { // Action
				InsnList newInstructions = new InsnList();
				newInstructions.add(new MethodInsnNode(INVOKESTATIC, ASM_HOOKS, "shiftLayerCount", "(I)I", false));

				method.instructions.insert(node, newInstructions);
				return true;
			});

	private static byte[] transformRecipeAddPattern(byte[] basicClass) {
		MethodSignature sig = new MethodSignature("matches", "func_77569_a", "(Lnet/minecraft/inventory/InventoryCrafting;Lnet/minecraft/world/World;)Z");
		return transform(basicClass, forMethod(sig, layerCountTransformer));
	}

	private static byte[] transformItemBanner(byte[] basicClass) {
		MethodSignature sig = new MethodSignature("appendHoverTextFromTileEntityTag", "func_185054_a", "(Lnet/minecraft/item/ItemStack;Ljava/util/List;)V");
		return transform(basicClass, forMethod(sig, layerCountTransformer));
	}

	private static byte[] transformRender(byte[] basicClass) {
		MethodSignature sig = new MethodSignature("renderEntityOnFire", "func_76977_a", "(Lnet/minecraft/entity/Entity;DDDF)V");

		return transform(basicClass, forMethod(sig, (MethodNode method) -> { // Action
			InsnList newInstructions = new InsnList();

			newInstructions.add(new VarInsnNode(ALOAD, 1));
			newInstructions.add(new VarInsnNode(DLOAD, 2));
			newInstructions.add(new VarInsnNode(DLOAD, 4));
			newInstructions.add(new VarInsnNode(DLOAD, 6));
			newInstructions.add(new VarInsnNode(FLOAD, 8));
			newInstructions.add(new MethodInsnNode(INVOKESTATIC, ASM_HOOKS, "renderFire", "(Lnet/minecraft/entity/Entity;DDDF)Z", false));
			LabelNode label = new LabelNode();
			newInstructions.add(new JumpInsnNode(IFEQ, label));
			newInstructions.add(new InsnNode(RETURN));
			newInstructions.add(label);

			method.instructions.insertBefore(method.instructions.getFirst(), newInstructions);
			return true;
		}));
	}

	// BOILERPLATE BELOW ==========================================================================================================================================

	private static byte[] transform(byte[] basicClass, TransformerAction... methods) {
		ClassReader reader = new ClassReader(basicClass);
		ClassNode node = new ClassNode();
		reader.accept(node, 0);

		boolean didAnything = false;

		for (TransformerAction pair : methods) {
			log("Applying Transformation to method (" + pair.sig + ")");
			didAnything |= findMethodAndTransform(node, pair.sig, pair.action);
		}

		if (didAnything) {
			ClassWriter writer = new SafeClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
			node.accept(writer);
			return writer.toByteArray();
		}

		return basicClass;
	}

	public static boolean findMethodAndTransform(ClassNode node, MethodSignature sig, MethodAction pred) {
		for (MethodNode method : node.methods) {
			if (sig.matches(method)) {
				log("Located Method, patching...");

				boolean finish = pred.test(method);
				log("Patch result: " + finish);

				return finish;
			}
		}

		log("Failed to locate the method!");
		return false;
	}

	public static MethodAction combine(NodeFilter filter, NodeAction action) {
		return (MethodNode mnode) -> applyOnNode(mnode, filter, action);
	}

	public static boolean applyOnNode(MethodNode method, NodeFilter filter, NodeAction action) {
		Iterator<AbstractInsnNode> iterator = method.instructions.iterator();

		boolean didAny = false;
		while (iterator.hasNext()) {
			AbstractInsnNode anode = iterator.next();
			if (filter.test(anode)) {
				log("Located patch target node " + getNodeString(anode));
				didAny = true;
				if (action.test(method, anode))
					break;
			}
		}

		return didAny;
	}

	private static void log(String str) {
		LogManager.getLogger("Quark ASM").info(str);
	}

	private static String getNodeString(AbstractInsnNode node) {
		Printer printer = new Textifier();

		TraceMethodVisitor visitor = new TraceMethodVisitor(printer);
		node.accept(visitor);

		StringWriter sw = new StringWriter();
		printer.print(new PrintWriter(sw));
		printer.getText().clear();

		return sw.toString().replaceAll("\n", "").trim();
	}

	private static boolean hasOptifine(String msg) {
		try {
			if (Class.forName("optifine.OptiFineTweaker") != null) {
				log("Optifine Detected. Disabling Patch for " + msg);
				return true;
			}
		} catch (ClassNotFoundException ignored) {
		}
		return false;
	}

	public static class MethodSignature {
		private final String funcName, srgName, funcDesc;

		public MethodSignature(String funcName, String srgName, String funcDesc) {
			this.funcName = funcName;
			this.srgName = srgName;
			this.funcDesc = funcDesc;
		}

		@Override
		public String toString() {
			return "Names [" + funcName + ", " + srgName + "] Descriptor " + funcDesc;
		}

		public boolean matches(String methodName, String methodDesc) {
			return (methodName.equals(funcName) || methodName.equals(srgName))
					&& (methodDesc.equals(funcDesc));
		}

		public boolean matches(MethodNode method) {
			return matches(method.name, method.desc);
		}

		public boolean matches(MethodInsnNode method) {
			return matches(method.name, method.desc);
		}

	}

	/**
	 * Safe class writer.
	 * The way COMPUTE_FRAMES works may require loading additional classes. This can cause ClassCircularityErrors.
	 * The override for getCommonSuperClass will ensure that COMPUTE_FRAMES works properly by using the right ClassLoader.
	 * <p>
	 * Code from: https://github.com/JamiesWhiteShirt/clothesline/blob/master/src/core/java/com/jamieswhiteshirt/clothesline/core/SafeClassWriter.java
	 */
	public static class SafeClassWriter extends ClassWriter {
		public SafeClassWriter(int flags) {
			super(flags);
		}

		@Override
		protected String getCommonSuperClass(String type1, String type2) {
			Class<?> c, d;
			ClassLoader classLoader = Launch.classLoader;
			try {
				c = Class.forName(type1.replace('/', '.'), false, classLoader);
				d = Class.forName(type2.replace('/', '.'), false, classLoader);
			} catch (Exception e) {
				throw new RuntimeException(e.toString());
			}
			if (c.isAssignableFrom(d)) {
				return type1;
			}
			if (d.isAssignableFrom(c)) {
				return type2;
			}
			if (c.isInterface() || d.isInterface()) {
				return "java/lang/Object";
			} else {
				do {
					c = c.getSuperclass();
				} while (!c.isAssignableFrom(d));
				return c.getName().replace('.', '/');
			}
		}
	}

	// Basic interface aliases to not have to clutter up the code with generics over and over again
	private interface Transformer extends Function<byte[], byte[]> {
		// NO-OP
	}

	private interface MethodAction extends Predicate<MethodNode> {
		// NO-OP
	}

	private interface NodeFilter extends Predicate<AbstractInsnNode> {
		// NO-OP
	}

	private interface NodeAction extends BiPredicate<MethodNode, AbstractInsnNode> {
		// NO-OP
	}

	private static TransformerAction forMethod(MethodSignature sig, MethodAction action) {
		return new TransformerAction(sig, action);
	}

	private final static class TransformerAction {
		private final MethodSignature sig;
		private final MethodAction action;

		public TransformerAction(MethodSignature sig, MethodAction action) {
			this.sig = sig;
			this.action = action;
		}
	}

}
