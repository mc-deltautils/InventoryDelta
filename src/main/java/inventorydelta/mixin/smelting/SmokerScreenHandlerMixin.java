package inventorydelta.mixin.smelting;

import inventorydelta.delta.smelt.AutoSmeltSlotDelta;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SmokerScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SmokerScreenHandler.class)
abstract class SmokerScreenHandlerMixin extends ScreenHandler {
    protected SmokerScreenHandlerMixin(ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Inject(
        method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/screen/PropertyDelegate;)V",
        at = @At("TAIL"),
        require = 0
    )
    private void inventorydelta$autoSmeltOnOpen(
        int syncId,
        PlayerInventory playerInventory,
        Inventory inventory,
        PropertyDelegate propertyDelegate,
        CallbackInfo ci
    ) {
        AutoSmeltSlotDelta.INSTANCE.onOpened(playerInventory.player, inventory);
    }
}
