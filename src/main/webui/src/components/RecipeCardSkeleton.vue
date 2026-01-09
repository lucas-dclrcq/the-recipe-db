<script setup lang="ts">
</script>

<template>
  <div class="card-pop p-5 relative overflow-hidden">
    <!-- Decorative corner -->
    <div class="absolute top-0 right-0 w-10 h-10 bg-secondary/30" style="clip-path: polygon(100% 0, 0 0, 100% 100%);" aria-hidden="true"></div>

    <div class="flex justify-between items-start mb-3 pr-6">
      <div class="h-6 skeleton-artistic w-3/4"></div>
      <div class="h-7 skeleton-artistic w-14 ml-3 rounded-lg"></div>
    </div>

    <div class="h-4 skeleton-artistic w-1/2 mb-4"></div>

    <div class="flex flex-wrap gap-2">
      <div class="h-7 skeleton-artistic w-20 rounded-full"></div>
      <div class="h-7 skeleton-artistic w-24 rounded-full"></div>
      <div class="h-7 skeleton-artistic w-16 rounded-full"></div>
    </div>

    <!-- Bottom accent line -->
    <div class="absolute bottom-0 left-0 right-0 h-1 bg-gradient-to-r from-primary via-secondary to-accent opacity-30" aria-hidden="true"></div>
  </div>
</template>
