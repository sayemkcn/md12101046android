package net.toracode.moviebuzz.entity;

/**
 * Created by sayemkcn on 8/21/16.
 */
public class PreferenceSingleItem {
    private String name;
    private boolean activated;

    public PreferenceSingleItem() {
    }

    public PreferenceSingleItem(String name, boolean activated) {
        this.name = name;
        this.activated = activated;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    @Override
    public String toString() {
        return "PreferenceSingleItem{" +
                "name='" + name + '\'' +
                ", activated=" + activated +
                '}';
    }
}
