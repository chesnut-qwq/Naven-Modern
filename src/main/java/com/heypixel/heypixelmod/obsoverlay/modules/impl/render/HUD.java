package com.heypixel.heypixelmod.obsoverlay.modules.impl.render;

import com.heypixel.heypixelmod.obsoverlay.Naven;
import com.heypixel.heypixelmod.obsoverlay.Version;
import com.heypixel.heypixelmod.obsoverlay.events.api.EventTarget;
import com.heypixel.heypixelmod.obsoverlay.events.api.types.EventType;
import com.heypixel.heypixelmod.obsoverlay.events.impl.EventRender2D;
import com.heypixel.heypixelmod.obsoverlay.events.impl.EventShader;
import com.heypixel.heypixelmod.obsoverlay.modules.Category;
import com.heypixel.heypixelmod.obsoverlay.modules.Module;
import com.heypixel.heypixelmod.obsoverlay.modules.ModuleInfo;
import com.heypixel.heypixelmod.obsoverlay.modules.ModuleManager;
import com.heypixel.heypixelmod.obsoverlay.utils.RenderUtils;
import com.heypixel.heypixelmod.obsoverlay.utils.SmoothAnimationTimer;
import com.heypixel.heypixelmod.obsoverlay.utils.StencilUtils;
import com.heypixel.heypixelmod.obsoverlay.utils.renderer.Fonts;
import com.heypixel.heypixelmod.obsoverlay.utils.renderer.text.CustomTextRenderer;
import com.heypixel.heypixelmod.obsoverlay.values.ValueBuilder;
import com.heypixel.heypixelmod.obsoverlay.values.impl.BooleanValue;
import com.heypixel.heypixelmod.obsoverlay.values.impl.FloatValue;
import com.heypixel.heypixelmod.obsoverlay.values.impl.ModeValue;
import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.joml.Vector4f;

@ModuleInfo(
   name = "HUD",
   description = "Displays information on your screen",
   category = Category.RENDER
)
public class HUD extends Module {
   public static final int headerColor = new Color(150, 45, 45, 255).getRGB();
   public static final int bodyColor = new Color(0, 0, 0, 120).getRGB();
   public static final int backgroundColor = new Color(0, 0, 0, 40).getRGB();
   private static final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
   public BooleanValue waterMark = ValueBuilder.create(this, "Water Mark").setDefaultBooleanValue(true).build().getBooleanValue();
   public FloatValue watermarkSize = ValueBuilder.create(this, "Watermark Size")
      .setVisibility(this.waterMark::getCurrentValue)
      .setDefaultFloatValue(0.4F)
      .setFloatStep(0.01F)
      .setMinFloatValue(0.1F)
      .setMaxFloatValue(1.0F)
      .build()
      .getFloatValue();
   public BooleanValue moduleToggleSound = ValueBuilder.create(this, "Module Toggle Sound").setDefaultBooleanValue(true).build().getBooleanValue();
   public BooleanValue notification = ValueBuilder.create(this, "Notification").setDefaultBooleanValue(true).build().getBooleanValue();
   public BooleanValue arrayList = ValueBuilder.create(this, "Array List").setDefaultBooleanValue(true).build().getBooleanValue();
   public BooleanValue prettyModuleName = ValueBuilder.create(this, "Pretty Module Name")
      .setOnUpdate(value -> Module.update = true)
      .setVisibility(this.arrayList::getCurrentValue)
      .setDefaultBooleanValue(false)
      .build()
      .getBooleanValue();
   public BooleanValue hideRenderModules = ValueBuilder.create(this, "Hide Render Modules")
      .setOnUpdate(value -> Module.update = true)
      .setVisibility(this.arrayList::getCurrentValue)
      .setDefaultBooleanValue(false)
      .build()
      .getBooleanValue();
   public BooleanValue rainbow = ValueBuilder.create(this, "Rainbow")
      .setDefaultBooleanValue(true)
      .setVisibility(this.arrayList::getCurrentValue)
      .build()
      .getBooleanValue();
   public FloatValue rainbowSpeed = ValueBuilder.create(this, "Rainbow Speed")
      .setVisibility(this.arrayList::getCurrentValue)
      .setMinFloatValue(1.0F)
      .setMaxFloatValue(20.0F)
      .setDefaultFloatValue(10.0F)
      .setFloatStep(0.1F)
      .build()
      .getFloatValue();
   public FloatValue rainbowOffset = ValueBuilder.create(this, "Rainbow Offset")
      .setVisibility(this.arrayList::getCurrentValue)
      .setMinFloatValue(1.0F)
      .setMaxFloatValue(20.0F)
      .setDefaultFloatValue(10.0F)
      .setFloatStep(0.1F)
      .build()
      .getFloatValue();
   public ModeValue arrayListDirection = ValueBuilder.create(this, "ArrayList Direction")
      .setVisibility(this.arrayList::getCurrentValue)
      .setDefaultModeIndex(0)
      .setModes("Right", "Left")
      .build()
      .getModeValue();
   public FloatValue xOffset = ValueBuilder.create(this, "X Offset")
      .setVisibility(this.arrayList::getCurrentValue)
      .setMinFloatValue(-100.0F)
      .setMaxFloatValue(100.0F)
      .setDefaultFloatValue(1.0F)
      .setFloatStep(1.0F)
      .build()
      .getFloatValue();
   public FloatValue yOffset = ValueBuilder.create(this, "Y Offset")
      .setVisibility(this.arrayList::getCurrentValue)
      .setMinFloatValue(1.0F)
      .setMaxFloatValue(100.0F)
      .setDefaultFloatValue(1.0F)
      .setFloatStep(1.0F)
      .build()
      .getFloatValue();
   public FloatValue arrayListSize = ValueBuilder.create(this, "ArrayList Size")
      .setVisibility(this.arrayList::getCurrentValue)
      .setDefaultFloatValue(0.4F)
      .setFloatStep(0.01F)
      .setMinFloatValue(0.1F)
      .setMaxFloatValue(1.0F)
      .build()
      .getFloatValue();
   List<Module> renderModules;
   float width;
   float watermarkHeight;
   List<Vector4f> blurMatrices = new ArrayList<>();

   public String getModuleDisplayName(Module module) {
      String name = this.prettyModuleName.getCurrentValue() ? module.getPrettyName() : module.getName();
      return name + (module.getSuffix() == null ? "" : " §7" + module.getSuffix());
   }

   @EventTarget
   public void notification(EventRender2D e) {
      if (this.notification.getCurrentValue()) {
         Naven.getInstance().getNotificationManager().onRender(e);
      }
   }

   @EventTarget
   public void onShader(EventShader e) {
      if (this.notification.getCurrentValue() && e.getType() == EventType.SHADOW) {
         Naven.getInstance().getNotificationManager().onRenderShadow(e);
      }

      if (this.waterMark.getCurrentValue()) {
         RenderUtils.drawRoundedRect(e.getStack(), 5.0F, 5.0F, this.width, this.watermarkHeight + 8.0F, 5.0F, Integer.MIN_VALUE);
      }

      if (this.arrayList.getCurrentValue()) {
         for (Vector4f blurMatrix : this.blurMatrices) {
            RenderUtils.fillBound(e.getStack(), blurMatrix.x(), blurMatrix.y(), blurMatrix.z(), blurMatrix.w(), 1073741824);
         }
      }
   }

   @EventTarget
   public void onRender(EventRender2D e) {
      CustomTextRenderer font = Fonts.opensans;
      if (this.waterMark.getCurrentValue()) {
         e.getStack().pushPose();
         String text = "Naven | " + Version.getVersion() + " | Chestnut_awa | " + StringUtils.split(mc.fpsString, " ")[0] + " FPS | " + format.format(new Date());
         this.width = font.getWidth(text, (double)this.watermarkSize.getCurrentValue()) + 14.0F;
         this.watermarkHeight = (float)font.getHeight(true, (double)this.watermarkSize.getCurrentValue());
         StencilUtils.write(false);
         RenderUtils.drawRoundedRect(e.getStack(), 5.0F, 5.0F, this.width, this.watermarkHeight + 8.0F, 5.0F, Integer.MIN_VALUE);
         StencilUtils.erase(true);
         RenderUtils.fill(e.getStack(), 5.0F, 5.0F, 9.0F + this.width, 8.0F, headerColor);
         RenderUtils.fill(e.getStack(), 5.0F, 8.0F, 9.0F + this.width, 16.0F + this.watermarkHeight, bodyColor);
         font.render(e.getStack(), text, 12.0, 10.0, Color.WHITE, true, (double)this.watermarkSize.getCurrentValue());
         StencilUtils.dispose();
         e.getStack().popPose();
      }

      this.blurMatrices.clear();
      if (this.arrayList.getCurrentValue()) {
         e.getStack().pushPose();
         ModuleManager moduleManager = Naven.getInstance().getModuleManager();
         if (update || this.renderModules == null) {
            this.renderModules = new ArrayList<>(moduleManager.getModules());
            if (this.hideRenderModules.getCurrentValue()) {
               this.renderModules.removeIf(modulex -> modulex.getCategory() == Category.RENDER);
            }

            this.renderModules.sort((o1, o2) -> {
               float o1Width = font.getWidth(this.getModuleDisplayName(o1), (double)this.arrayListSize.getCurrentValue());
               float o2Width = font.getWidth(this.getModuleDisplayName(o2), (double)this.arrayListSize.getCurrentValue());
               return Float.compare(o2Width, o1Width);
            });
         }

         float maxWidth = this.renderModules.isEmpty()
            ? 0.0F
            : font.getWidth(this.getModuleDisplayName(this.renderModules.get(0)), (double)this.arrayListSize.getCurrentValue());
         float arrayListX = this.arrayListDirection.isCurrentMode("Right")
            ? (float)mc.getWindow().getGuiScaledWidth() - maxWidth - 6.0F + this.xOffset.getCurrentValue()
            : 3.0F + this.xOffset.getCurrentValue();
         float arrayListY = this.yOffset.getCurrentValue();
         float height = 0.0F;
         double fontHeight = font.getHeight(true, (double)this.arrayListSize.getCurrentValue());

         for (Module module : this.renderModules) {
            SmoothAnimationTimer animation = module.getAnimation();
            if (module.isEnabled()) {
               animation.target = 100.0F;
            } else {
               animation.target = 0.0F;
            }

            animation.update(true);
            if (animation.value > 0.0F) {
               String displayName = this.getModuleDisplayName(module);
               float stringWidth = font.getWidth(displayName, (double)this.arrayListSize.getCurrentValue());
               float left = -stringWidth * (1.0F - animation.value / 100.0F);
               float right = maxWidth - stringWidth * (animation.value / 100.0F);
               float innerX = this.arrayListDirection.isCurrentMode("Left") ? left : right;
               RenderUtils.fillBound(
                  e.getStack(),
                  arrayListX + innerX,
                  arrayListY + height + 2.0F,
                  stringWidth + 3.0F,
                  (float)((double)(animation.value / 100.0F) * fontHeight),
                  backgroundColor
               );
               this.blurMatrices
                  .add(
                     new Vector4f(arrayListX + innerX, arrayListY + height + 2.0F, stringWidth + 3.0F, (float)((double)(animation.value / 100.0F) * fontHeight))
                  );
               int color = -1;
               if (this.rainbow.getCurrentValue()) {
                  color = RenderUtils.getRainbowOpaque(
                     (int)(-height * this.rainbowOffset.getCurrentValue()), 1.0F, 1.0F, (21.0F - this.rainbowSpeed.getCurrentValue()) * 1000.0F
                  );
               }

               float alpha = animation.value / 100.0F;
               font.setAlpha(alpha);
               font.render(
                  e.getStack(),
                  displayName,
                  (double)(arrayListX + innerX + 1.5F),
                  (double)(arrayListY + height + 1.0F),
                  new Color(color),
                  true,
                  (double)this.arrayListSize.getCurrentValue()
               );
               height += (float)((double)(animation.value / 100.0F) * fontHeight);
            }
         }

         font.setAlpha(1.0F);
         e.getStack().popPose();
      }
   }
}
