package com.wh0oo.portalframewhitelist;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import net.fabricmc.api.ModInitializer;

public class PortalFrameWhitelistInitializer implements ModInitializer {
    @Override
    public void onInitialize() {
        MixinExtrasBootstrap.init();
        PortalFrameWhitelistConfig.load();
    }
}
