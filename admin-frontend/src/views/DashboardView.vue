<script setup>
// 仪表盘占位页面，业务内容由后续 change 实现
</script>

<template>
  <div class="dashboard">
    <el-container>
      <el-header>Mall 管理后台</el-header>
      <el-main>
        <p>管理后台脚手架已就绪。</p>
        <el-button type="primary" @click="$router.push('/login')">前往登录</el-button>
      </el-main>
    </el-container>
  </div>
</template>

<style scoped>
.el-header {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
  background: #545c64;
  color: #fff;
}
</style>
