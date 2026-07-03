<script setup>
// 首页占位页面，业务内容由后续 change 实现
</script>

<template>
  <div class="home">
    <van-nav-bar title="Mall 用户端" />
    <div class="home__body">
      <p>用户端脚手架已就绪。</p>
      <van-button type="primary" to="/login">前往登录</van-button>
    </div>
  </div>
</template>

<style scoped>
.home__body {
  padding: 16px;
  text-align: center;
}
</style>
