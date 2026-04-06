package com.abysthea.addon;

import com.abysthea.addon.rede.RedeAbysthea;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;

public class AbystheaCommands {

    private static final Map<UUID, BreathData> activeBreaths = new ConcurrentHashMap<>();
    private static final Map<UUID, PhaseData> activePhasers = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> activeFluidForm = new ConcurrentHashMap<>();
    private static final Map<UUID, PounceData> activePounces = new ConcurrentHashMap<>();
    private static final Map<UUID, VampireBiteData> activeVampireBites = new ConcurrentHashMap<>();
    private static final Map<UUID, PoisonBiteData> activePoisonBites = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> activeBackstabs = new ConcurrentHashMap<>();
    private static final Map<UUID, PredatorChargeData> activePredatorCharges = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> sunburnEnabled = new ConcurrentHashMap<>();
    private static final String TAG_SLIME_PHASING = AbystheaAddon.MODID + ":slime_phasing";
    private static final String TAG_FLUID_FORM = AbystheaAddon.MODID + ":fluid_form";

    private static final UUID NATURE_RESIST_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    static {
        MinecraftForge.EVENT_BUS.register(AbystheaCommands.class);
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("conebreath")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("duration", IntegerArgumentType.integer(1, 200))
                        .then(Commands.argument("range", FloatArgumentType.floatArg(1.0f, 20.0f))
                                .then(Commands.argument("angle", FloatArgumentType.floatArg(10.0f, 90.0f))
                                        .then(Commands.argument("damage", FloatArgumentType.floatArg(0.0f, 50.0f))
                                                .then(Commands.argument("fire_ticks", IntegerArgumentType.integer(0, 200))
                                                        .then(Commands.argument("particle", StringArgumentType.word())
                                                                .executes(context -> {
                                                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                                                    int duration = IntegerArgumentType.getInteger(context, "duration");
                                                                    float range = FloatArgumentType.getFloat(context, "range");
                                                                    float angle = FloatArgumentType.getFloat(context, "angle");
                                                                    float damage = FloatArgumentType.getFloat(context, "damage");
                                                                    int fireTicks = IntegerArgumentType.getInteger(context, "fire_ticks");
                                                                    String particleName = StringArgumentType.getString(context, "particle");

                                                                    startBreath(player, duration, range, angle, damage, fireTicks, particleName);
                                                                    return 1;
                                                                }))))))));

        dispatcher.register(Commands.literal("floraboost")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("radius", IntegerArgumentType.integer(1, 20))
                        .then(Commands.argument("intensity", IntegerArgumentType.integer(1, 10))
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    int radius = IntegerArgumentType.getInteger(context, "radius");
                                    int intensity = IntegerArgumentType.getInteger(context, "intensity");

                                    executeFloraBoost(player, radius, intensity);
                                    return 1;
                                }))));

        dispatcher.register(Commands.literal("slimephase")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("duration", IntegerArgumentType.integer(1, 200))
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            int duration = IntegerArgumentType.getInteger(context, "duration");

                            startSlimePhase(player, duration);
                            return 1;
                        })));

        dispatcher.register(Commands.literal("fluidform")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("duration", IntegerArgumentType.integer(1, 600))
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            int duration = IntegerArgumentType.getInteger(context, "duration");

                            startFluidForm(player, duration);
                            return 1;
                        })));

        dispatcher.register(Commands.literal("natureresist")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("value", FloatArgumentType.floatArg(-1.0f, 1.0f))
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            float value = FloatArgumentType.getFloat(context, "value");

                            applyNatureResist(player, value);
                            return 1;
                        })));

        dispatcher.register(Commands.literal("pounce")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("duration", IntegerArgumentType.integer(1, 100))
                        .then(Commands.argument("slowness_duration", IntegerArgumentType.integer(1, 200))
                                .then(Commands.argument("slowness_amplifier", IntegerArgumentType.integer(0, 5))
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            int duration = IntegerArgumentType.getInteger(context, "duration");
                                            int slowDur = IntegerArgumentType.getInteger(context, "slowness_duration");
                                            int slowAmp = IntegerArgumentType.getInteger(context, "slowness_amplifier");

                                            startPounce(player, duration, slowDur, slowAmp);
                                            return 1;
                                        })))));

        dispatcher.register(Commands.literal("vampirebite")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("duration", IntegerArgumentType.integer(1, 200))
                        .then(Commands.argument("food", IntegerArgumentType.integer(1, 20))
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    int duration = IntegerArgumentType.getInteger(context, "duration");
                                    int food = IntegerArgumentType.getInteger(context, "food");

                                    startVampireBite(player, duration, food);
                                    return 1;
                                }))));

        dispatcher.register(Commands.literal("poisonbite")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("duration", IntegerArgumentType.integer(1, 200))
                        .then(Commands.argument("poison_duration", IntegerArgumentType.integer(1, 1200))
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    int duration = IntegerArgumentType.getInteger(context, "duration");
                                    int poisonDur = IntegerArgumentType.getInteger(context, "poison_duration");

                                    startPoisonBite(player, duration, poisonDur);
                                    return 1;
                                }))));

        dispatcher.register(Commands.literal("backstab")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("duration", IntegerArgumentType.integer(1, 600))
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            int duration = IntegerArgumentType.getInteger(context, "duration");

                            startBackstab(player, duration);
                            return 1;
                        })));

        dispatcher.register(Commands.literal("predatorcharge")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("duration", IntegerArgumentType.integer(1, 100))
                        .then(Commands.argument("bleed_duration", IntegerArgumentType.integer(1, 600))
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    int duration = IntegerArgumentType.getInteger(context, "duration");
                                    int bleedDur = IntegerArgumentType.getInteger(context, "bleed_duration");

                                    startPredatorCharge(player, duration, bleedDur);
                                    return 1;
                                }))));

        dispatcher.register(Commands.literal("sunburn")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("enabled", IntegerArgumentType.integer(0, 1))
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            int enabled = IntegerArgumentType.getInteger(context, "enabled");

                            if (enabled == 1) {
                                sunburnEnabled.put(player.getUUID(), true);
                            } else {
                                sunburnEnabled.remove(player.getUUID());
                            }
                            return 1;
                        })));
    }

    public static void startFluidForm(ServerPlayer player, int durationTicks) {
        ServerLevel level = player.serverLevel();

        activeFluidForm.put(player.getUUID(), durationTicks);
        player.getPersistentData().putBoolean(TAG_FLUID_FORM, true);

        player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, durationTicks, 1, false, true, true));

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_SPLASH, SoundSource.PLAYERS, 1.0f, 1.5f);

        level.sendParticles(ParticleTypes.SPLASH,
                player.getX(), player.getY() + 1, player.getZ(),
                30, 1.0, 1.0, 1.0, 0.1);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (!activeFluidForm.containsKey(player.getUUID())) return;

        if (event.getSource().isIndirect() || event.getSource().getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile) {
            event.setCanceled(true);

            ServerLevel level = player.serverLevel();
            level.sendParticles(ParticleTypes.SPLASH,
                    player.getX(), player.getY() + 1, player.getZ(),
                    10, 0.5, 0.5, 0.5, 0.1);

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_SPLASH, SoundSource.PLAYERS, 0.5f, 2.0f);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        PhaseData phaseData = activePhasers.get(player.getUUID());
        if (phaseData != null) {
            phaseData.ticksRemaining--;

            ServerLevel level = player.serverLevel();
            if (phaseData.ticksRemaining % 5 == 0) {
                level.sendParticles(ParticleTypes.ITEM_SLIME,
                        player.getX(), player.getY() + 1, player.getZ(),
                        3, 0.3, 0.5, 0.3, 0.02);
            }

            if (phaseData.ticksRemaining <= 0) {
                endSlimePhase(player);
            }
        }

        Integer fluidTicks = activeFluidForm.get(player.getUUID());
        if (fluidTicks != null) {
            fluidTicks--;

            ServerLevel level = player.serverLevel();
            if (fluidTicks % 10 == 0) {
                level.sendParticles(ParticleTypes.DRIPPING_WATER,
                        player.getX(), player.getY() + 1.5, player.getZ(),
                        3, 0.3, 0.3, 0.3, 0.0);
            }

            if (fluidTicks <= 0) {
                endFluidForm(player);
            } else {
                activeFluidForm.put(player.getUUID(), fluidTicks);
            }
        }
    }

    private static void endFluidForm(ServerPlayer player) {
        activeFluidForm.remove(player.getUUID());
        player.getPersistentData().putBoolean(TAG_FLUID_FORM, false);

        ServerLevel level = player.serverLevel();

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_SPLASH, SoundSource.PLAYERS, 1.0f, 0.8f);

        level.sendParticles(ParticleTypes.SPLASH,
                player.getX(), player.getY() + 1, player.getZ(),
                20, 1.0, 1.0, 1.0, 0.1);
    }

    public static void startSlimePhase(ServerPlayer player, int durationTicks) {
        ServerLevel level = player.serverLevel();

        activePhasers.put(player.getUUID(), new PhaseData(durationTicks, player.blockPosition()));

        player.getPersistentData().putBoolean(TAG_SLIME_PHASING, true);

        RedeAbysthea.enviarSlimePhase(player, true);

        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, durationTicks, 0, false, true, true));

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SLIME_SQUISH, SoundSource.PLAYERS, 1.0f, 1.2f);

        level.sendParticles(ParticleTypes.ITEM_SLIME,
                player.getX(), player.getY() + 1, player.getZ(),
                20, 0.5, 0.5, 0.5, 0.1);
    }

    private static void endSlimePhase(ServerPlayer player) {
        activePhasers.remove(player.getUUID());
        player.getPersistentData().putBoolean(TAG_SLIME_PHASING, false);

        RedeAbysthea.enviarSlimePhase(player, false);

        ServerLevel level = player.serverLevel();

        if (isInsideBlock(player, level)) {
            BlockPos safePos = findSafePosition(player, level);
            if (safePos != null) {
                player.teleportTo(safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5);
            }
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SLIME_SQUISH, SoundSource.PLAYERS, 1.0f, 0.8f);

        level.sendParticles(ParticleTypes.ITEM_SLIME,
                player.getX(), player.getY() + 1, player.getZ(),
                20, 0.5, 0.5, 0.5, 0.1);
    }

    private static boolean isInsideBlock(ServerPlayer player, ServerLevel level) {
        BlockPos pos = player.blockPosition();
        return !level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isAir();
    }

    private static BlockPos findSafePosition(ServerPlayer player, ServerLevel level) {
        BlockPos playerPos = player.blockPosition();

        for (int radius = 1; radius <= 10; radius++) {
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        BlockPos check = playerPos.offset(x, y, z);
                        if (level.getBlockState(check).isAir() && level.getBlockState(check.above()).isAir()) {
                            return check;
                        }
                    }
                }
            }
        }
        return null;
    }

    public static void executeFloraBoost(ServerPlayer player, int radius, int intensity) {
        ServerLevel level = player.serverLevel();
        BlockPos center = player.blockPosition();
        ItemStack fakeBonemeal = new ItemStack(Items.BONE_MEAL);

        int blocksAffected = 0;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = center.offset(x, y, z);

                    for (int i = 0; i < intensity; i++) {
                        if (BoneMealItem.growCrop(fakeBonemeal.copy(), level, pos)) {
                            blocksAffected++;
                            level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                    5, 0.3, 0.3, 0.3, 0.0);

                            BlockPos above = pos.above();
                            if (level.getBlockState(above).is(Blocks.TALL_GRASS)) {
                                if (level.random.nextFloat() < 0.6f) {
                                    level.setBlock(above, Blocks.GRASS.defaultBlockState(), 3);
                                    level.removeBlock(above.above(), false);
                                }
                            }
                        }
                    }

                    for (int i = 0; i < intensity; i++) {
                        if (BoneMealItem.growWaterPlant(fakeBonemeal.copy(), level, pos, null)) {
                            blocksAffected++;
                        }
                    }
                }
            }
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BONE_MEAL_USE, SoundSource.PLAYERS, 1.0f, 1.0f);

        if (blocksAffected > 5) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.GRASS_BREAK, SoundSource.PLAYERS, 0.5f, 1.2f);
        }

        level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                player.getX(), player.getY() + 1, player.getZ(),
                50, radius * 0.5, 1.0, radius * 0.5, 0.0);
    }

    public static void startBreath(ServerPlayer player, int duration, float range, float angle, float damage, int fireTicks, String particleName) {
        BreathData data = new BreathData(duration, range, angle, damage, fireTicks, particleName);
        activeBreaths.put(player.getUUID(), data);

        ServerLevel level = player.serverLevel();
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0f, 0.8f);

        tickBreath(player, data);
    }

    private static void tickBreath(ServerPlayer player, BreathData data) {
        if (data.ticksRemaining <= 0 || !player.isAlive()) {
            activeBreaths.remove(player.getUUID());
            return;
        }

        ServerLevel level = player.serverLevel();
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();

        SimpleParticleType particleType = getParticleType(data.particleName);

        if (data.ticksRemaining % 10 == 0) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 1.0f, 1.0f);
        }

        float halfAngle = data.angle / 2.0f;
        int particleCount = 30;

        for (int i = 0; i < particleCount; i++) {
            float distance = level.random.nextFloat() * data.range;
            float yawOffset = (level.random.nextFloat() - 0.5f) * 2.0f * halfAngle;
            float pitchOffset = (level.random.nextFloat() - 0.5f) * 2.0f * halfAngle;

            double yaw = Math.atan2(lookVec.x, lookVec.z) + Math.toRadians(yawOffset);
            double pitch = -Math.asin(lookVec.y) + Math.toRadians(pitchOffset);

            double dx = Math.sin(yaw) * Math.cos(pitch) * distance;
            double dy = -Math.sin(pitch) * distance;
            double dz = Math.cos(yaw) * Math.cos(pitch) * distance;

            double px = eyePos.x + dx;
            double py = eyePos.y + dy;
            double pz = eyePos.z + dz;

            level.sendParticles(particleType, px, py, pz, 1, 0.1, 0.1, 0.1, 0.02);
        }

        AABB searchBox = new AABB(
                eyePos.x - data.range, eyePos.y - data.range, eyePos.z - data.range,
                eyePos.x + data.range, eyePos.y + data.range, eyePos.z + data.range
        );

        List<Entity> entities = level.getEntities(player, searchBox, entity -> entity instanceof LivingEntity && entity != player);

        for (Entity entity : entities) {
            if (isInCone(eyePos, lookVec, entity.position().add(0, entity.getBbHeight() / 2, 0), data.range, halfAngle)) {
                LivingEntity living = (LivingEntity) entity;

                if (data.damage > 0 && data.ticksRemaining % 10 == 0) {
                    living.hurt(level.damageSources().onFire(), data.damage);
                }

                if (data.fireTicks > 0) {
                    living.setSecondsOnFire(data.fireTicks);
                }
            }
        }

        data.ticksRemaining--;

        player.getServer().execute(() -> {
            if (activeBreaths.containsKey(player.getUUID())) {
                tickBreath(player, data);
            }
        });
    }

    private static boolean isInCone(Vec3 origin, Vec3 direction, Vec3 target, float range, float halfAngle) {
        Vec3 toTarget = target.subtract(origin);
        double distance = toTarget.length();

        if (distance > range || distance < 0.1) {
            return false;
        }

        Vec3 normalizedToTarget = toTarget.normalize();
        Vec3 normalizedDirection = direction.normalize();

        double dot = normalizedDirection.dot(normalizedToTarget);
        double angleRad = Math.acos(Math.min(1.0, Math.max(-1.0, dot)));
        double angleDeg = Math.toDegrees(angleRad);

        return angleDeg <= halfAngle;
    }

    private static SimpleParticleType getParticleType(String name) {
        return switch (name.toLowerCase()) {
            case "soul_flame", "soul_fire_flame" -> ParticleTypes.SOUL_FIRE_FLAME;
            case "smoke", "large_smoke" -> ParticleTypes.LARGE_SMOKE;
            case "snowflake" -> ParticleTypes.SNOWFLAKE;
            case "dragon_breath" -> ParticleTypes.DRAGON_BREATH;
            case "campfire_cosy_smoke" -> ParticleTypes.CAMPFIRE_COSY_SMOKE;
            default -> ParticleTypes.FLAME;
        };
    }

    private static class BreathData {
        int ticksRemaining;
        final float range;
        final float angle;
        final float damage;
        final int fireTicks;
        final String particleName;

        BreathData(int duration, float range, float angle, float damage, int fireTicks, String particleName) {
            this.ticksRemaining = duration;
            this.range = range;
            this.angle = angle;
            this.damage = damage;
            this.fireTicks = fireTicks;
            this.particleName = particleName;
        }
    }

    private static class PhaseData {
        int ticksRemaining;
        final BlockPos startPos;

        PhaseData(int duration, BlockPos startPos) {
            this.ticksRemaining = duration;
            this.startPos = startPos;
        }
    }

    public static void applyNatureResist(ServerPlayer player, float value) {
        if (!ModList.get().isLoaded("irons_spellbooks")) {
            return;
        }

        try {
            ResourceLocation attrLoc = new ResourceLocation("irons_spellbooks", "nature_magic_resist");
            Attribute attr = ForgeRegistries.ATTRIBUTES.getValue(attrLoc);

            if (attr != null) {
                AttributeInstance inst = player.getAttribute(attr);
                if (inst != null) {
                    inst.removeModifier(NATURE_RESIST_UUID);
                    if (value != 0) {
                        inst.addPermanentModifier(new AttributeModifier(
                                NATURE_RESIST_UUID,
                                "abysthea.silvaris.nature_resist",
                                value,
                                AttributeModifier.Operation.ADDITION
                        ));
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static void startPounce(ServerPlayer player, int durationTicks, int slowDur, int slowAmp) {
        ServerLevel level = player.serverLevel();

        Vec3 look = player.getLookAngle();
        player.setDeltaMovement(look.x * 2.0, 0, look.z * 2.0);
        player.hurtMarked = true;

        activePounces.put(player.getUUID(), new PounceData(durationTicks, slowDur, slowAmp));

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.CAT_HISS, SoundSource.PLAYERS, 1.0f, 1.2f);
    }

    @SubscribeEvent
    public static void onPlayerTickPounce(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        PounceData data = activePounces.get(player.getUUID());
        if (data == null) return;

        data.ticksRemaining--;

        ServerLevel level = player.serverLevel();

        level.sendParticles(ParticleTypes.CRIT,
                player.getX(), player.getY() + 0.5, player.getZ(),
                3, 0.2, 0.2, 0.2, 0.05);

        AABB hitbox = player.getBoundingBox().inflate(0.5);
        List<Entity> entities = level.getEntities(player, hitbox, e -> e instanceof LivingEntity && e != player);

        if (!entities.isEmpty()) {
            LivingEntity target = (LivingEntity) entities.get(0);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, data.slownessDuration, data.slownessAmplifier, false, true, true));

            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 1.0f, 1.0f);

            level.sendParticles(ParticleTypes.CRIT,
                    target.getX(), target.getY() + 1, target.getZ(),
                    15, 0.5, 0.5, 0.5, 0.1);

            activePounces.remove(player.getUUID());
            return;
        }

        if (data.ticksRemaining <= 0 || player.onGround()) {
            activePounces.remove(player.getUUID());
        }
    }

    private static class PounceData {
        int ticksRemaining;
        final int slownessDuration;
        final int slownessAmplifier;

        PounceData(int duration, int slowDur, int slowAmp) {
            this.ticksRemaining = duration;
            this.slownessDuration = slowDur;
            this.slownessAmplifier = slowAmp;
        }
    }

    public static void startVampireBite(ServerPlayer player, int durationTicks, int food) {
        ServerLevel level = player.serverLevel();

        activeVampireBites.put(player.getUUID(), new VampireBiteData(durationTicks, food));

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BAT_AMBIENT, SoundSource.PLAYERS, 1.0f, 0.8f);

        level.sendParticles(ParticleTypes.DAMAGE_INDICATOR,
                player.getX(), player.getY() + 1, player.getZ(),
                5, 0.3, 0.3, 0.3, 0.0);
    }

    @SubscribeEvent
    public static void onVampireBiteHit(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;

        VampireBiteData data = activeVampireBites.get(player.getUUID());
        if (data == null) return;

        ServerLevel level = player.serverLevel();

        player.heal(5);
        player.getFoodData().eat(data.food, 3.0f);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 1.0f, 1.5f);

        level.playSound(null, event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(),
                SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 1.0f, 0.8f);

        level.sendParticles(ParticleTypes.DAMAGE_INDICATOR,
                event.getEntity().getX(), event.getEntity().getY() + 1, event.getEntity().getZ(),
                10, 0.3, 0.5, 0.3, 0.1);

        activeVampireBites.remove(player.getUUID());
    }

    @SubscribeEvent
    public static void onVampireBiteTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        VampireBiteData data = activeVampireBites.get(player.getUUID());
        if (data == null) return;

        data.ticksRemaining--;

        if (data.ticksRemaining <= 0) {
            activeVampireBites.remove(player.getUUID());
        }
    }

    private static class VampireBiteData {
        int ticksRemaining;
        final int food;

        VampireBiteData(int duration, int food) {
            this.ticksRemaining = duration;
            this.food = food;
        }
    }

    public static void startPoisonBite(ServerPlayer player, int durationTicks, int poisonDur) {
        ServerLevel level = player.serverLevel();

        activePoisonBites.put(player.getUUID(), new PoisonBiteData(durationTicks, poisonDur));

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SPIDER_AMBIENT, SoundSource.PLAYERS, 1.0f, 1.2f);

        level.sendParticles(ParticleTypes.ITEM_SLIME,
                player.getX(), player.getY() + 1, player.getZ(),
                10, 0.3, 0.3, 0.3, 0.05);
    }

    @SubscribeEvent
    public static void onPoisonBiteHit(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;

        PoisonBiteData data = activePoisonBites.get(player.getUUID());
        if (data == null) return;

        LivingEntity target = event.getEntity();

        target.addEffect(new MobEffectInstance(MobEffects.POISON, data.poisonDuration, 0, false, true, true));

        ServerLevel level = player.serverLevel();

        level.playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.SPIDER_HURT, SoundSource.PLAYERS, 1.0f, 1.0f);

        level.sendParticles(ParticleTypes.ITEM_SLIME,
                target.getX(), target.getY() + 1, target.getZ(),
                15, 0.4, 0.5, 0.4, 0.1);

        activePoisonBites.remove(player.getUUID());
    }

    @SubscribeEvent
    public static void onPoisonBiteTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        PoisonBiteData data = activePoisonBites.get(player.getUUID());
        if (data == null) return;

        data.ticksRemaining--;

        if (data.ticksRemaining <= 0) {
            activePoisonBites.remove(player.getUUID());
        }
    }

    private static class PoisonBiteData {
        int ticksRemaining;
        final int poisonDuration;

        PoisonBiteData(int duration, int poisonDur) {
            this.ticksRemaining = duration;
            this.poisonDuration = poisonDur;
        }
    }

    public static void startBackstab(ServerPlayer player, int durationTicks) {
        ServerLevel level = player.serverLevel();

        activeBackstabs.put(player.getUUID(), durationTicks);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5f, 2.0f);

        level.sendParticles(ParticleTypes.SMOKE,
                player.getX(), player.getY() + 1, player.getZ(),
                20, 0.5, 0.5, 0.5, 0.02);
    }

    @SubscribeEvent
    public static void onBackstabHit(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;

        Integer ticks = activeBackstabs.get(player.getUUID());
        if (ticks == null || ticks <= 0) return;

        LivingEntity target = event.getEntity();

        Vec3 playerLook = player.getLookAngle().normalize();
        Vec3 targetLook = target.getLookAngle().normalize();

        double dot = playerLook.x * targetLook.x + playerLook.z * targetLook.z;

        if (dot > 0.5) {
            event.setAmount(event.getAmount() * 2.5f);

            ServerLevel level = player.serverLevel();

            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0f, 0.8f);

            level.sendParticles(ParticleTypes.CRIT,
                    target.getX(), target.getY() + 1, target.getZ(),
                    20, 0.5, 0.5, 0.5, 0.2);
        }
    }

    @SubscribeEvent
    public static void onBackstabTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        Integer ticks = activeBackstabs.get(player.getUUID());
        if (ticks == null) return;

        ticks--;
        if (ticks <= 0) {
            activeBackstabs.remove(player.getUUID());
        } else {
            activeBackstabs.put(player.getUUID(), ticks);
        }
    }

    public static void startPredatorCharge(ServerPlayer player, int durationTicks, int bleedDur) {
        ServerLevel level = player.serverLevel();

        Vec3 look = player.getLookAngle();
        player.setDeltaMovement(look.x * 2.5, 0.3, look.z * 2.5);
        player.hurtMarked = true;

        activePredatorCharges.put(player.getUUID(), new PredatorChargeData(durationTicks, bleedDur));

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.WOLF_GROWL, SoundSource.PLAYERS, 1.5f, 1.0f);

        level.sendParticles(ParticleTypes.CRIT,
                player.getX(), player.getY() + 0.5, player.getZ(),
                10, 0.3, 0.3, 0.3, 0.1);
    }

    @SubscribeEvent
    public static void onPredatorChargeTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        PredatorChargeData data = activePredatorCharges.get(player.getUUID());
        if (data == null) return;

        data.ticksRemaining--;

        ServerLevel level = player.serverLevel();

        level.sendParticles(ParticleTypes.CRIT,
                player.getX(), player.getY() + 0.5, player.getZ(),
                3, 0.2, 0.2, 0.2, 0.05);

        AABB hitbox = player.getBoundingBox().inflate(0.5);
        List<Entity> entities = level.getEntities(player, hitbox, e -> e instanceof LivingEntity && e != player);

        if (!entities.isEmpty()) {
            LivingEntity target = (LivingEntity) entities.get(0);

            target.hurt(player.damageSources().playerAttack(player), 4.0f);
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, data.bleedDuration, 0, false, true, true));

            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0f, 0.8f);

            level.sendParticles(ParticleTypes.DAMAGE_INDICATOR,
                    target.getX(), target.getY() + 1, target.getZ(),
                    15, 0.5, 0.5, 0.5, 0.1);

            activePredatorCharges.remove(player.getUUID());
            return;
        }

        if (data.ticksRemaining <= 0 || player.onGround()) {
            activePredatorCharges.remove(player.getUUID());
        }
    }

    private static class PredatorChargeData {
        int ticksRemaining;
        final int bleedDuration;

        PredatorChargeData(int duration, int bleedDur) {
            this.ticksRemaining = duration;
            this.bleedDuration = bleedDur;
        }
    }

    @SubscribeEvent
    public static void onSunburnTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        if (!sunburnEnabled.containsKey(player.getUUID())) return;

        ResourceLocation umbrellaId = new ResourceLocation("artifacts", "umbrella");

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        ItemStack helmet = player.getInventory().armor.get(3);

        boolean hasUmbrella = false;
        boolean hasHelmet = !helmet.isEmpty();

        if (!mainHand.isEmpty()) {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(mainHand.getItem());
            if (umbrellaId.equals(id)) hasUmbrella = true;
        }

        if (!hasUmbrella && !offHand.isEmpty()) {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(offHand.getItem());
            if (umbrellaId.equals(id)) hasUmbrella = true;
        }

        if (hasUmbrella || hasHelmet) return;

        ServerLevel level = player.serverLevel();
        boolean exposedToSun = level.isDay()
                && !level.isRaining()
                && level.canSeeSky(player.blockPosition().above());

        if (exposedToSun) {
            player.setSecondsOnFire(2);
        }
    }
}