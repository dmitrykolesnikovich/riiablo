package com.riiablo.engine.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.riiablo.Riiablo;
import com.riiablo.codec.Animation;
import com.riiablo.codec.COF;
import com.riiablo.codec.DC;
import com.riiablo.engine.Dirty;
import com.riiablo.engine.Engine;
import com.riiablo.engine.SystemPriority;
import com.riiablo.engine.component.AnimationComponent;
import com.riiablo.engine.component.CofComponent;
import com.riiablo.engine.component.cof.AlphaUpdate;
import com.riiablo.engine.component.cof.TransformUpdate;

@DependsOn(CofLoaderSystem.class)
public class AnimationLoaderSystem extends IteratingSystem {
  private static final String TAG = "AnimationLoaderSystem";

  private static final boolean DEBUG      = true;
  private static final boolean DEBUG_LOAD = DEBUG && !true;

  private final ComponentMapper<CofComponent> cofComponent = ComponentMapper.getFor(CofComponent.class);
  private final ComponentMapper<AnimationComponent> animComponent = ComponentMapper.getFor(AnimationComponent.class);

  private final ComponentMapper<TransformUpdate> transformUpdate = ComponentMapper.getFor(TransformUpdate.class);
  private final ComponentMapper<AlphaUpdate> alphaUpdate = ComponentMapper.getFor(AlphaUpdate.class);

  public AnimationLoaderSystem() {
    super(Family.all(CofComponent.class, AnimationComponent.class).get(), SystemPriority.AnimationLoaderSystem);
  }

  @Override
  public void update(float delta) {
    //Riiablo.assets.update();
    super.update(delta);
  }

  @Override
  protected void processEntity(Entity entity, float delta) {
    AnimationComponent animComponent = this.animComponent.get(entity);
    CofComponent cofComponent = this.cofComponent.get(entity);

    Animation anim = animComponent.animation;

    boolean changed = false;
    COF cof = cofComponent.cof;
    if (cof == null) return;
    // FIXME: logic here needs to be looked into -- should below operations be performed when cof didn't change?
    boolean newCof = anim.setCOF(cof);
    if (newCof && cofComponent.speed != CofComponent.SPEED_NULL) {
      anim.setFrameDelta(cofComponent.speed);
    }
    TransformUpdate transformUpdate = null;
    AlphaUpdate alphaUpdate = null;
    for (int l = 0, numLayers = cof.getNumLayers(); l < numLayers; l++) {
      COF.Layer layer = cof.getLayer(l);
      if (!Dirty.isDirty(cofComponent.load, layer.component)) continue;
      int flag = (1 << layer.component);
      if (cofComponent.component[layer.component] == CofComponent.COMPONENT_NIL) {
        cofComponent.load &= ~flag;
        anim.setLayer(layer, null, false);

        transformUpdate = this.transformUpdate.get(entity);
        if (transformUpdate != null) {
          transformUpdate.flags &= ~flag;
          if (transformUpdate.flags == Dirty.NONE) entity.remove(TransformUpdate.class);
        }

        alphaUpdate = this.alphaUpdate.get(entity);
        if (alphaUpdate != null) {
          alphaUpdate.flags &= ~flag;
          if (alphaUpdate.flags == Dirty.NONE) entity.remove(AlphaUpdate.class);
        }

        changed = true;
        continue;
      }

      AssetDescriptor<? extends DC> descriptor = cofComponent.layer[layer.component];
      if (Riiablo.assets.isLoaded(descriptor)) {
        cofComponent.load &= ~flag;
        if (DEBUG_LOAD) Gdx.app.debug(TAG, "finished loading " + descriptor);
        DC dc = Riiablo.assets.get(descriptor);
        anim.setLayer(layer, dc, false);

        if (transformUpdate == null) transformUpdate = Engine.getOrCreateComponent(entity, getEngine(), TransformUpdate.class, this.transformUpdate);
        transformUpdate.flags |= flag;

        if (alphaUpdate == null) alphaUpdate = Engine.getOrCreateComponent(entity, getEngine(), AlphaUpdate.class, this.alphaUpdate);
        alphaUpdate.flags |= flag;

        changed = true;
      }
    }

    if (changed) anim.updateBox();
    if (DEBUG_LOAD) Gdx.app.debug(TAG, "load layers: " + Dirty.toString(cofComponent.load));
  }
}