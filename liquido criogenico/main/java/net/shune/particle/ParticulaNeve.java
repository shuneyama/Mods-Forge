package net.shune.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shune.fluid.ModFluidos;

@OnlyIn(Dist.CLIENT)
public class ParticulaNeve extends TextureSheetParticle {
    private float angulo;
    private float velocidadeAngulo;
    private boolean pousada = false;
    private int ticksPousada = 0;
    private static final int TICKS_POUSADA = 60;
    private static final int TICKS_SEM_CHAO = 100;
    private final boolean dentroDoLiquido;
    private final float xInicial;
    private final float zInicial;
    private static final float DESLOCAMENTO_MAX_INTERNO = 5.0f / 16.0f;

    protected ParticulaNeve(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z);
        this.pickSprite(sprites);

        this.lifetime = TICKS_SEM_CHAO;
        this.angulo = random.nextFloat() * 360f;
        this.velocidadeAngulo = (random.nextFloat() - 0.5f) * 0.15f;
        this.xInicial = (float) x;
        this.zInicial = (float) z;

        BlockPos spawnPos = BlockPos.containing(x, y, z);
        FluidState spawnFluid = level.getFluidState(spawnPos);
        this.dentroDoLiquido = spawnFluid.getType() == ModFluidos.LIQUIDO_CRIOGENICO_FONTE.get()
                || spawnFluid.getType() == ModFluidos.LIQUIDO_CRIOGENICO_CORRENTEZA.get();

        float direcao = random.nextFloat() * (float) Math.PI * 2f;
        float velocidade = 0.04f + random.nextFloat() * 0.04f;

        if (dentroDoLiquido) {
            this.xd = Math.cos(direcao) * velocidade * 0.3f;
            this.zd = Math.sin(direcao) * velocidade * 0.3f;
            this.yd = (random.nextFloat() - 0.5f) * 0.01f;
            this.gravity = 0.001f;
        } else {
            this.xd = Math.cos(direcao) * velocidade;
            this.zd = Math.sin(direcao) * velocidade;
            this.yd = (random.nextFloat() - 0.5f) * 0.02f;
            this.gravity = 0.002f;
        }

        this.quadSize = 0.02f + random.nextFloat() * 0.02f;
        this.alpha = 0.9f;
        this.rCol = 0.0f;
        this.gCol = 0.9f;
        this.bCol = 1.0f;
        this.hasPhysics = false;
    }

    private boolean deveIgnorar(BlockPos pos) {
        BlockState estado = level.getBlockState(pos);
        if (estado.isAir()) return true;
        if (estado.is(BlockTags.IMPERMEABLE)) return true;
        FluidState fluido = level.getFluidState(pos);
        if (fluido.getType() == ModFluidos.LIQUIDO_CRIOGENICO_FONTE.get()) return true;
        if (fluido.getType() == ModFluidos.LIQUIDO_CRIOGENICO_CORRENTEZA.get()) return true;
        return false;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (pousada) {
            ticksPousada++;
            int fadeInicio = TICKS_POUSADA / 2;
            if (ticksPousada > fadeInicio) {
                this.alpha -= 1.0f / fadeInicio;
            }
            if (this.alpha <= 0 || ticksPousada >= TICKS_POUSADA) {
                this.remove();
            }
            return;
        }

        this.angulo += this.velocidadeAngulo;
        this.velocidadeAngulo += (random.nextFloat() - 0.5f) * 0.02f;
        this.velocidadeAngulo = Math.max(-0.2f, Math.min(0.2f, this.velocidadeAngulo));

        if (dentroDoLiquido) {
            double deslocX = this.x - this.xInicial;
            double deslocZ = this.z - this.zInicial;
            double distancia = Math.sqrt(deslocX * deslocX + deslocZ * deslocZ);

            if (distancia < DESLOCAMENTO_MAX_INTERNO) {
                this.xd += Math.sin(this.angulo) * 0.0003f;
                this.zd += Math.cos(this.angulo) * 0.0003f;
            } else {
                this.xd -= deslocX * 0.05f;
                this.zd -= deslocZ * 0.05f;
            }
            this.xd *= 0.95f;
            this.zd *= 0.95f;
        } else {
            this.xd += Math.sin(this.angulo) * 0.001f;
            this.zd += Math.cos(this.angulo) * 0.001f;
            this.xd *= 0.97f;
            this.zd *= 0.97f;
        }

        this.yd -= this.gravity;

        double xNovo = this.x + this.xd;
        double yNovo = this.y + this.yd;
        double zNovo = this.z + this.zd;

        BlockPos posNova = BlockPos.containing(xNovo, yNovo, zNovo);
        if (!deveIgnorar(posNova)) {
            pousada = true;
            this.lifetime = Integer.MAX_VALUE;
            this.xd = 0;
            this.yd = 0;
            this.zd = 0;
            return;
        }

        this.x = xNovo;
        this.y = yNovo;
        this.z = zNovo;

        int metadeVida = TICKS_SEM_CHAO - 20;
        if (this.age > metadeVida) {
            this.alpha -= 1.0f / 20;
        }

        if (this.alpha <= 0 || this.age >= this.lifetime) {
            this.remove();
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new ParticulaNeve(level, x, y, z, sprites);
        }
    }
}