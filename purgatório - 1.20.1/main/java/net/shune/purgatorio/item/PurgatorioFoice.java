package net.shune.purgatorio.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

public class PurgatorioFoice extends SwordItem {
    public static final Tier PURGATORIO_TIER = new Tier() {
        @Override
        public int getUses() {
            return 0;
        }

        @Override
        public float getSpeed() {
            return 10.0F;
        }

        @Override
        public float getAttackDamageBonus() {
            return 29.0F;
        }

        @Override
        public int getLevel() {
            return 5;
        }

        @Override
        public int getEnchantmentValue() {
            return 30;
        }

        @Override
        @NotNull
        public Ingredient getRepairIngredient() {
            return Ingredient.EMPTY;
        }
    };

    private static final int BLINDNESS_DURATION = 100;
    private static final int BLINDNESS_AMPLIFIER = 0;

    public PurgatorioFoice(Item.Properties properties) {
        super(PURGATORIO_TIER, 0, -2.0F, properties);
    }

    @Override
    public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, BLINDNESS_DURATION, BLINDNESS_AMPLIFIER, false, true, true));
        return true;
    }

    public boolean isDamageable() {
        return false;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public int getMaxDamage(@NotNull ItemStack stack) {
        return 0;
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return false;
    }
}