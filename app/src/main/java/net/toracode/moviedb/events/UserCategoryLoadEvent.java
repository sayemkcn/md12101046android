package net.toracode.moviedb.events;

import java.util.List;

import net.toracode.moviedb.entity.CategoryEntity;

/**
 * Created by sayemkcn on 8/10/16.
 */
public class UserCategoryLoadEvent {
    List<CategoryEntity> categoryList;

    public UserCategoryLoadEvent(List<CategoryEntity> categoryList) {
        this.categoryList = categoryList;
    }

    public List<CategoryEntity> getCategoryList() {
        return categoryList;
    }

}
