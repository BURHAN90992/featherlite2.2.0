package com.featherlite;

import com.featherlite.gui.MainMenuScreen;
import com.featherlite.gui.ModulesScreen;
import com.featherlite.module.Module;
import com.featherlite.module.Modules;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class FeatherLite implements ClientModInitializer {
    public static final String ID = "featherlite";
    public static final List<Module> MODULES = new ArrayList<>();
    private static KeyBinding openGui;
    private static Module menuModule;

    @Override
    public void onInitializeClient() {
        MODULES.add(new Modules.Keystrokes());
        MODULES.add(new Modules.Cps());
        MODULES.add(new Modules.Fps());
        MODULES.add(new Modules.Coords());
        MODULES.add(new Modules.Ping());
        MODULES.add(new Modules.Direction());
        MODULES.add(new Modules.Speed());
        MODULES.add(new Modules.Clock());
        MODULES.add(new Modules.Armor());
        MODULES.add(new Modules.Hitboxes());
        Features.freelook = new Modules.Freelook();
        Features.zoom = new Modules.Zoom();
        Features.sprint = new Modules.ToggleSprint();
        MODULES.add(Features.freelook);
        MODULES.add(Features.zoom);
        MODULES.add(Features.sprint);
        menuModule = new Modules.MainMenu();
        MODULES.add(menuModule);
        Config.load();

        openGui = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.featherlite.gui", GLFW.GLFW_KEY_RIGHT_SHIFT, KeyBinding.Category.MISC));

        ClientLifecycleEvents.CLIENT_STOPPING.register(Features::shutdown);

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            Features.tick(mc);
            while (openGui.wasPressed()) {
                if (mc.currentScreen == null) mc.setScreen(new ModulesScreen());
            }
            if (menuModule.enabled && mc.currentScreen != null
                    && mc.currentScreen.getClass() == TitleScreen.class) {
                mc.setScreen(new MainMenuScreen());
            }
        });

        HudElementRegistry.attachElementAfter(
                VanillaHudElements.MISC_OVERLAYS,
                Identifier.of(ID, "hud"),
                FeatherLite::renderHud);
    }

    private static void renderHud(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden) return;
        int screenW = ctx.getScaledWindowWidth();
        int screenH = ctx.getScaledWindowHeight();
        var matrices = ctx.getMatrices();
        for (int i = 0, n = MODULES.size(); i < n; i++) {
            Module m = MODULES.get(i);
            if (!m.enabled) continue;
            m.frame(mc);
            if (!m.hasHud()) continue;
            int x = m.screenX(mc, screenW), y = m.screenY(mc, screenH);
            if (m.scale == 1.0) {
                m.render(ctx, mc, x, y);
            } else {
                matrices.pushMatrix();
                matrices.translate(x, y);
                matrices.scale((float) m.scale, (float) m.scale);
                m.render(ctx, mc, 0, 0);
                matrices.popMatrix();
            }
        }
    }
}
