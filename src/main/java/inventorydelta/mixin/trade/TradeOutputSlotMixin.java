package inventorydelta.mixin.trade;

import inventorydelta.delta.transfer.TransferSlotRefillDelta;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.TradeOutputSlot;
import net.minecraft.village.Merchant;
import net.minecraft.village.MerchantInventory;
import net.minecraft.village.TradeOffer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(TradeOutputSlot.class)
abstract class TradeOutputSlotMixin extends net.minecraft.screen.slot.Slot {
    @Shadow
    @Final
    private MerchantInventory merchantInventory;

    @Shadow
    @Final
    private PlayerEntity player;

    @Shadow
    @Final
    private Merchant merchant;

    TradeOutputSlotMixin(MerchantInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Inject(method = "onTakeItem", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void inventorydelta$refillAfterTrade(PlayerEntity player, ItemStack stack, CallbackInfo ci, TradeOffer tradeOffer) {
        TransferSlotRefillDelta.INSTANCE.onTradeCompleted(this.merchantInventory, player, this.merchant, tradeOffer);
    }
}
