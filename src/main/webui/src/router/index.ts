import {createRouter, createWebHistory} from 'vue-router'

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            name: 'home',
            redirect: { name: 'recipes' },
        },
        {
            path: '/ingredients',
            name: 'ingredients',
            component: () => import('../views/IngredientsPage.vue'),
        },
        {
            path: '/ingredients/:id',
            name: 'ingredient-detail',
            component: () => import('../views/IngredientDetailPage.vue'),
        },
        {
            path: '/recipes/:id',
            name: 'recipe-detail',
            component: () => import('../views/RecipeDetailPage.vue'),
        },
        {
            path: '/recipes',
            name: 'recipes',
            component: () => import('../views/RecipesPage.vue'),
        },
        {
            path: '/cookbooks/:id',
            name: 'cookbook-detail',
            component: () => import('../views/CookbookDetailPage.vue'),
        },
        {
            path: '/cookbooks',
            name: 'cookbooks',
            component: () => import('../views/CookbooksPage.vue'),
        },
        {
            path: '/import',
            name: 'import',
            component: () => import('../views/ImportCookbookPage.vue'),
        },
        {
            path: '/import/success',
            name: 'import-success',
            component: () => import('../views/ImportSuccessPage.vue'),
        },
        {
            path: '/cookbooks/:id/review',
            name: 'cookbook-review',
            component: () => import('../views/CookbookReviewPage.vue'),
        },
    ],
})

export default router
