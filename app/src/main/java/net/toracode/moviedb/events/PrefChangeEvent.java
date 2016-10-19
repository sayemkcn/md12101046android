package net.toracode.moviedb.events;

import android.widget.CompoundButton;

/**
 * Created by sayemkcn on 8/22/16.
 */
public class PrefChangeEvent {
    private CompoundButton button;
    private int sourceId;
    private boolean checked;

    public PrefChangeEvent(CompoundButton button, int sourceId, boolean checked) {
        this.button = button;
        this.sourceId = sourceId;
        this.checked = checked;
    }

    public CompoundButton getButton() {
        return button;
    }

    public void setButton(CompoundButton button) {
        this.button = button;
    }

    public int getSourceId() {
        return sourceId;
    }

    public void setSourceId(int sourceId) {
        this.sourceId = sourceId;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    @Override
    public String toString() {
        return "PrefChangeEvent{" +
                "button=" + button +
                ", sourceId=" + sourceId +
                ", checked=" + checked +
                '}';
    }
}
