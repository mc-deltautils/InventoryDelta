package inventorydelta.mixin.crafting;

import inventorydelta.delta.craft.AutoCraftSlotDelta;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingResultSlot.class)
abstract class CraftingResultSlotMixin extends net.minecraft.screen.slot.Slot {
    @Shadow
    @Final
    private RecipeInputInventory input;

    @Shadow
    @Final
    private PlayerEntity player;

    @Unique
    private DefaultedList<ItemStack> inventorydelta$inputSnapshot = DefaultedList.of();

    CraftingResultSlotMixin(RecipeInputInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Inject(method = "onTakeItem", at = @At("HEAD"))
    private void inventorydelta$cacheInputs(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        int size = this.input.size();
        DefaultedList<ItemStack> copy = DefaultedList.ofSize(size, ItemStack.EMPTY);
        for (int i = 0; i < size; i++) {
            copy.set(i, this.input.getStack(i).copy());
        }
        this.inventorydelta$inputSnapshot = copy;
    }

    @Inject(method = "onTakeItem", at = @At("TAIL"))
    private void inventorydelta$refillAfterCraft(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        AutoCraftSlotDelta.INSTANCE.onCrafted(player, this.input, this.inventorydelta$inputSnapshot);
    }
}
