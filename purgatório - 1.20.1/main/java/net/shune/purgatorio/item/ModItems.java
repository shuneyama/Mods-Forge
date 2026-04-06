package net.shune.purgatorio.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.shune.purgatorio.PurgatorioMod;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, PurgatorioMod.MOD_ID);

    public static final RegistryObject<Item> PURGATORIO_FOICE = ITEMS.register("purgatorio_foice",
            () -> new PurgatorioFoice(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> LANCA_ANGELICAL = ITEMS.register("lanca_angelical",
            () -> new SwordItem(new Tier() {
                @Override public int getUses() { return 0; }
                @Override public float getSpeed() { return 0; }
                @Override public float getAttackDamageBonus() { return 332.0F; }
                @Override public int getLevel() { return 0; }
                @Override public int getEnchantmentValue() { return 0; }
                @Override public Ingredient getRepairIngredient() { return Ingredient.EMPTY; }
            }, 0, -3.0F, new Item.Properties().stacksTo(1)) {
                public boolean isDamageable() { return false; }
                @Override public boolean isBarVisible(ItemStack stack) { return false; }
            });

    public static final RegistryObject<Item> CAJADO_AMET = ITEMS.register("cajado_amet",
            () -> new SwordItem(new Tier() {
                @Override public int getUses() { return 0; }
                @Override public float getSpeed() { return 0; }
                @Override public float getAttackDamageBonus() { return 44.0F; }
                @Override public int getLevel() { return 0; }
                @Override public int getEnchantmentValue() { return 0; }
                @Override public Ingredient getRepairIngredient() { return Ingredient.EMPTY; }
            }, 0, -3.0F, new Item.Properties().stacksTo(1)) {
                public boolean isDamageable() { return false; }
                @Override public boolean isBarVisible(ItemStack stack) { return false; }
            });

    public static final RegistryObject<Item> CAJADO_LUCIFER = ITEMS.register("cajado_lucifer",
            () -> new SwordItem(new Tier() {
                @Override public int getUses() { return 0; }
                @Override public float getSpeed() { return 0; }
                @Override public float getAttackDamageBonus() { return 665.0F; }
                @Override public int getLevel() { return 0; }
                @Override public int getEnchantmentValue() { return 0; }
                @Override public Ingredient getRepairIngredient() { return Ingredient.EMPTY; }
            }, 0, -3.0F, new Item.Properties().stacksTo(1)) {
                public boolean isDamageable() { return false; }
                @Override public boolean isBarVisible(ItemStack stack) { return false; }
            });

    public static final RegistryObject<Item> CAJADO_SATA = ITEMS.register("cajado_sata",
            () -> new SwordItem(new Tier() {
                @Override public int getUses() { return 0; }
                @Override public float getSpeed() { return 0; }
                @Override public float getAttackDamageBonus() { return 665.0F; }
                @Override public int getLevel() { return 0; }
                @Override public int getEnchantmentValue() { return 0; }
                @Override public Ingredient getRepairIngredient() { return Ingredient.EMPTY; }
            }, 0, -3.0F, new Item.Properties().stacksTo(1)) {
                public boolean isDamageable() { return false; }
                @Override public boolean isBarVisible(ItemStack stack) { return false; }
            });

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}