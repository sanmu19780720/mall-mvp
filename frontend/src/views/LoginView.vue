<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/api/request'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 登录表单：账号（用户名/手机号/邮箱）、密码
const form = reactive({
  identifier: '',
  password: '',
})

const loading = ref(false)

async function onSubmit() {
  loading.value = true
  try {
    const { data } = await request.post('/api/auth/login', {
      identifier: form.identifier,
      password: form.password,
    })
    userStore.setToken(data.token)
    router.replace('/')
  } catch (error) {
    // 401 INVALID_CREDENTIALS 或其它错误统一提示账号或密码错误
    showFailToast('账号或密码错误')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login">
    <van-nav-bar title="登录" left-arrow @click-left="$router.back()" />
    <van-form class="login__form" @submit="onSubmit">
      <van-cell-group inset>
        <van-field
          v-model="form.identifier"
          name="identifier"
          label="账号"
          placeholder="用户名/手机号/邮箱"
          :rules="[{ required: true, message: '请填写账号' }]"
        />
        <van-field
          v-model="form.password"
          name="password"
          label="密码"
          type="password"
          placeholder="请输入密码"
          :rules="[{ required: true, message: '请填写密码' }]"
        />
      </van-cell-group>
      <div class="login__actions">
        <van-button
          round
          block
          type="primary"
          native-type="submit"
          :loading="loading"
        >
          登录
        </van-button>
        <p class="login__register-link">
          没有账号？<router-link to="/register">去注册</router-link>
        </p>
      </div>
    </van-form>
  </div>
</template>

<style scoped>
.login__form {
  margin-top: 16px;
}

.login__actions {
  padding: 16px;
}

.login__register-link {
  margin-top: 16px;
  text-align: center;
  color: #969799;
  font-size: 14px;
}
</style>
