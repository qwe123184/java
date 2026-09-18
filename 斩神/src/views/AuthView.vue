<script setup>
import LorePanel from '../components/LorePanel.vue'
import AuthCard from '../components/AuthCard.vue'
</script>

<template>
  <main class="stage">
    <LorePanel />
    <AuthCard />
  </main>
</template>

<style scoped>
.stage {
  position: relative; z-index: 2;
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.1fr .9fr;
  align-items: center;
  gap: 40px;
  padding: 48px clamp(28px, 6vw, 110px);
}

@media (max-width: 880px) {
  .stage {
    grid-template-columns: 1fr;
    gap: 30px;
    padding: 60px 22px 40px;
    text-align: center;
  }
}
</style>
