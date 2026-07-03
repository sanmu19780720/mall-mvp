<script setup>
// 登录页占位，实际登录逻辑由后续 change 实现
</script>

<template>
  <div class="login">
    <el-card class="login__card">
      <template #header>登录</template>
      <p>登录页占位。</p>
      <el-button @click="$router.push('/')">返回首页</el-button>
    </el-card>
  </div>
</template>

<style scoped>
.login {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
}

.login__card {
  width: 360px;
}
</style>
