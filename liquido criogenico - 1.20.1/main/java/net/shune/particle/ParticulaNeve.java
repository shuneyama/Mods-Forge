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
    private static final int TICKS_VIDA = 100;
    private final float tamanhoInicial;
    private final float tamanhoFinal;

    protected ParticulaNeve(ClientLevel level, double x, double y, double z,
                            double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z);
        this.pickSprite(sprites);

        this.lifetime = TICKS_VIDA;
        this.angulo = random.nextFloat() * 360f;
        this.velocidadeAngulo = (random.nextFloat() - 0.5f) * 0.15f;

        this.tamanhoInicial = 0.02f + random.nextFloat() * 0.02f;
        this.tamanhoFinal = tamanhoInicial * 3.0f;
        this.quadSize = tamanhoInicial;

        this.xd = xSpeed != 0 ? xSpeed + (random.nextFloat() - 0.5f) * 0.02f : (random.nextFloat() - 0.5f) * 0.04f;
        this.zd = zSpeed != 0 ? zSpeed + (random.nextFloat() - 0.5f) * 0.02f : (random.nextFloat() - 0.5f) * 0.04f;
        this.yd = (random.nextFloat() - 0.5f) * 0.02f;
        this.gravity = 0.002f;

        this.alpha = 0.9f;
        this.rCol = 0.0f;
        this.gCol = 0.9f;
        this.bCol = 1.0f;
        this.hasPhysics = false;
    }

    private boolean ehBlocoSolido(BlockPos pos) {
        BlockState estado = level.getBlockState(pos);
        if (estado.isAir()) return false;
        if (estado.is(BlockTags.IMPERMEABLE)) return false;
        FluidState fluido = level.getFluidState(pos);
        if (!fluido.isEmpty()) return false;
        return true;
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

        this.xd += Math.sin(this.angulo) * 0.001f;
        this.zd += Math.cos(this.angulo) * 0.001f;
        this.xd *= 0.97f;
        this.zd *= 0.97f;
        this.yd -= this.gravity;

        float progresso = (float) this.age / TICKS_VIDA;
        this.quadSize = tamanhoInicial + (tamanhoFinal - tamanhoInicial) * progresso;

        double xNovo = this.x + this.xd;
        double yNovo = this.y + this.yd;
        double zNovo = this.z + this.zd;

        BlockPos posNova = BlockPos.containing(xNovo, yNovo, zNovo);
        if (ehBlocoSolido(posNova)) {
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

        int fadeInicio = TICKS_VIDA - 20;
        if (this.age > fadeInicio) {
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
            return new ParticulaNeve(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        }
    }
}