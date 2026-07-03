<script setup>
// 登录页占位，实际登录逻辑由后续 change 实现
</script>

<template>
  <div class="login">
    <van-nav-bar title="登录" left-arrow @click-left="$router.back()" />
    <div class="login__body">
      <p>登录页占位。</p>
    </div>
  </div>
</template>

<style scoped>
.login__body {
  padding: 16px;
  text-align: center;
}
</style>
