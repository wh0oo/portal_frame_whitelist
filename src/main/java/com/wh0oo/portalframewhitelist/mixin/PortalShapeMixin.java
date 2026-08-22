package com.wh0oo.portalframewhitelist.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.wh0oo.portalframewhitelist.PortalFrameWhitelistConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PortalShape.class)
public abstract class PortalShapeMixin {

    @ModifyReturnValue(
        method = "lambda$static$0",
        at = @At("RETURN")
    )
    private static boolean portalFrameWhitelist$allowConfiguredBlock(
        boolean original,
        BlockState state,
        BlockGetter level,
        BlockPos pos
    ) {
        return original || PortalFrameWhitelistConfig.isWhitelisted(state);
    }
}
