package org.ldclrcq.dto;

import java.util.List;

public class ConfirmImportRequest {

    private List<ConfirmedRecipe> recipes;

    public ConfirmImportRequest() {}

    public ConfirmImportRequest(List<ConfirmedRecipe> recipes) {
        this.recipes = recipes;
    }

    public List<ConfirmedRecipe> getRecipes() {
        return recipes;
    }

    public void setRecipes(List<ConfirmedRecipe> recipes) {
        this.recipes = recipes;
    }

    public static record ConfirmedRecipe(
            String recipeName,
            int pageNumber,
            String ingredient,
            boolean keep
    ) {}
}
