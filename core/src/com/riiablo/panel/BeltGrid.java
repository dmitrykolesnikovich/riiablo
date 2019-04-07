package com.riiablo.panel;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.SnapshotArray;
import com.riiablo.Keys;
import com.riiablo.Riiablo;
import com.riiablo.item.Item;
import com.riiablo.item.Type;
import com.riiablo.key.MappedKey;
import com.riiablo.screen.GameScreen;
import com.riiablo.widget.Label;

public class BeltGrid extends ItemGrid {
  private static final String TAG = "BeltGrid";

  final Rectangle visibleArea = new Rectangle();
  final Label[] hotkeys;

  TextureRegion background;
  boolean hidden;
  int rows;

  public BeltGrid(GameScreen gameScreen, int width, int height, int boxWidth, int boxHeight) {
    super(gameScreen, width, height, boxWidth, boxHeight);
    visibleArea.set(0, 0, width * boxWidth, boxHeight - 1);
    setRows(1);
    setHidden(true);

    hotkeys = new Label[width];
    for (int i = 0; i < width; i++) {
      final Label hotkey = hotkeys[i] = new Label("", Riiablo.fonts.font16, Riiablo.colors.gold);
      Keys.Belt[i].addAssignmentListener(new MappedKey.AssignmentListener() {
        @Override
        public void onAssigned(MappedKey key, int assignment, int keycode) {
          hotkey.setText(Input.Keys.toString(key.getPrimaryAssignment()));
        }

        @Override
        public void onUnassigned(MappedKey key, int assignment, int keycode) {
          hotkey.setText("");
        }

        @Override
        public void onFirstAssignment(MappedKey key, int assignment, int keycode) {
          onAssigned(key, assignment, keycode);
        }
      });
    }
  }

  public void setBackground(TextureRegion background) {
    this.background = background;
  }

  @Override
  protected boolean accept(Item item) {
    return item != null && item.type.is(Type.POTI);
  }

  public void setRows(int rows) {
    if (this.rows != rows) {
      this.rows = rows;
      if (!hidden) setHeight(rows * boxHeight);
    }
  }

  public void setHidden(boolean b) {
    if (hidden != b) {
      hidden = b;
      setCullingArea(hidden ? visibleArea : null);
      setHeight(hidden ? boxHeight : rows * boxHeight);
      Touchable touchable = hidden ? Touchable.disabled : Touchable.enabled;
      SnapshotArray<Actor> snapshot = getChildren();
      Actor[] children = snapshot.begin();
      for (Actor child : children) {
        StoredItem item = (StoredItem) child;
        if (item == null) continue;
        if (item.getY() >= boxHeight) item.setTouchable(touchable);
      }
      snapshot.end();
    }
  }

  @Override
  public void draw(Batch batch, float parentAlpha) {
    setHidden(!clickListener.isOver());
    if (!hidden) {
      float x = getX() - 1;
      float y = getY() + boxHeight;
      for (int i = 1; i < rows; i++) {
        batch.draw(background, x, y);
        y += boxHeight;
      }
    }
    super.draw(batch, parentAlpha);

    final float PADDING = 2;
    float x = getX() + PADDING;
    float y = getY() + PADDING;
    for (int i = 0; i < width; i++, x += boxWidth) {
      Label hotkey = hotkeys[i];
      hotkey.setPosition(x, y);
      hotkey.draw(batch, 1);
    }
  }
}
