package net.shune.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shune.fluid.ModFluidos;

@OnlyIn(Dist.CLIENT)
public class ParticulaVaporCriogenico extends TextureSheetParticle {
    private static final int DURACAO_POUSO = 50;
    private static final float VELOCIDADE_SUBIDA = 0.005f;
    private static final float VELOCIDADE_DESCIDA = 0.003f;
    private static final float ALTURA_MAXIMA = 3.0f / 16.0f;
    private static final float DESLOCAMENTO_MAX_LATERAL = 6.0f / 16.0f;
    private boolean pousada = false;
    private int ticksPouso = 0;
    private double yPouso = 0;
    private float anguloDeriva;
    private float velocidadeDeriva;
    private boolean subindo = true;
    private float alturaSubida = 0;
    private float xInicial;
    private float zInicial;

    protected ParticulaVaporCriogenico(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z);
        this.pickSprite(sprites);

        this.lifetime = 200;
        this.anguloDeriva = random.nextFloat() * (float) Math.PI * 2f;
        this.velocidadeDeriva = (random.nextFloat() - 0.5f) * 0.04f;
        this.xInicial = (float) x;
        this.zInicial = (float) z;

        this.xd = 0;
        this.yd = VELOCIDADE_SUBIDA;
        this.zd = 0;

        this.quadSize = 0.05f + random.nextFloat() * 0.1f;
        this.alpha = 0.0f;

        this.rCol = 0.0f;
        this.gCol = 0.95f;
        this.bCol = 1.0f;

        this.gravity = 0;
        this.hasPhysics = false;
    }

    private boolean deveParar(double yAtual) {
        BlockPos posAtual = BlockPos.containing(this.x, yAtual, this.z);
        BlockPos posAbaixo = BlockPos.containing(this.x, yAtual - 0.05, this.z);

        FluidState fluidoAtual = level.getFluidState(posAtual);
        FluidState fluidoAbaixo = level.getFluidState(posAbaixo);

        boolean ehLiquido = fluidoAtual.getType() == ModFluidos.LIQUIDO_CRIOGENICO_FONTE.get()
                || fluidoAtual.getType() == ModFluidos.LIQUIDO_CRIOGENICO_CORRENTEZA.get()
                || fluidoAbaixo.getType() == ModFluidos.LIQUIDO_CRIOGENICO_FONTE.get()
                || fluidoAbaixo.getType() == ModFluidos.LIQUIDO_CRIOGENICO_CORRENTEZA.get();
        if (ehLiquido) return true;

        BlockState blocoAbaixo = level.getBlockState(posAbaixo);
        return !blocoAbaixo.isAir() && level.getFluidState(posAbaixo).isEmpty();
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (pousada) {
            ticksPouso++;
            this.y = yPouso;
            this.xd = 0;
            this.yd = 0;
            this.zd = 0;

            int fadeInicio = (int)(DURACAO_POUSO * 0.6f);
            if (ticksPouso < fadeInicio) {
                this.alpha = Math.min(0.75f, this.alpha + 0.04f);
            } else {
                this.alpha -= 0.75f / (DURACAO_POUSO - fadeInicio);
            }

            if (this.alpha <= 0 || ticksPouso >= DURACAO_POUSO) {
                this.remove();
            }
            return;
        }

        this.anguloDeriva += this.velocidadeDeriva;
        this.velocidadeDeriva += (random.nextFloat() - 0.5f) * 0.003f;
        this.velocidadeDeriva = Math.max(-0.05f, Math.min(0.05f, this.velocidadeDeriva));

        double deslocX = this.x - this.xInicial;
        double deslocZ = this.z - this.zInicial;
        double distancia = Math.sqrt(deslocX * deslocX + deslocZ * deslocZ);

        if (distancia < DESLOCAMENTO_MAX_LATERAL) {
            this.xd += Math.sin(this.anguloDeriva) * 0.0008f;
            this.zd += Math.cos(this.anguloDeriva) * 0.0008f;
        } else {
            this.xd -= deslocX * 0.01f;
            this.zd -= deslocZ * 0.01f;
        }

        this.xd *= 0.96f;
        this.zd *= 0.96f;

        if (subindo) {
            alturaSubida += VELOCIDADE_SUBIDA;
            this.yd = VELOCIDADE_SUBIDA;
            if (alturaSubida >= ALTURA_MAXIMA) {
                subindo = false;
                this.yd = 0;
            }
        } else {
            this.yd = -VELOCIDADE_DESCIDA;
        }

        if (!subindo && deveParar(this.y + this.yd)) {
            pousar(this.y);
            return;
        }

        this.x += this.xd;
        this.y += this.yd;
        this.z += this.zd;

        if (this.alpha < 0.75f) {
            this.alpha = Math.min(0.75f, this.alpha + 0.05f);
        }

        if (this.age >= this.lifetime) {
            this.remove();
        }
    }

    private void pousar(double y) {
        this.yPouso = y;
        this.pousada = true;
        this.lifetime = Integer.MAX_VALUE;
        this.xd = 0;
        this.yd = 0;
        this.zd = 0;
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
            return new ParticulaVaporCriogenico(level, x, y, z, sprites);
        }
    }
}