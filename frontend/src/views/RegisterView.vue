<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/api/request'

const router = useRouter()

// 注册表单：手机号、密码、昵称
const form = reactive({
  phone: '',
  password: '',
  nickname: '',
})

const loading = ref(false)

// 后端错误码 -> 中文提示
const ERROR_MESSAGES = {
  USERNAME_TAKEN: '该用户名已被注册',
  PHONE_TAKEN: '该手机号已被注册',
  EMAIL_TAKEN: '该邮箱已被注册',
  INVALID_PHONE: '手机号格式不正确',
  INVALID_REQUEST: '请填写完整的注册信息',
}

// 11 位国内手机号校验
function validatePhone(value) {
  return /^1\d{10}$/.test(value)
}

async function onSubmit() {
  loading.value = true
  try {
    await request.post('/api/user/register', {
      phone: form.phone,
      password: form.password,
      nickname: form.nickname,
    })
    showSuccessToast('注册成功')
    router.replace('/login')
  } catch (error) {
    const code = error.response?.data?.error
    const message = ERROR_MESSAGES[code] || '注册失败，请稍后重试'
    showFailToast(message)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="register">
    <van-nav-bar title="注册" left-arrow @click-left="$router.back()" />
    <van-form class="register__form" @submit="onSubmit">
      <van-cell-group inset>
        <van-field
          v-model="form.phone"
          name="phone"
          label="手机号"
          placeholder="请输入 11 位手机号"
          type="tel"
          maxlength="11"
          :rules="[
            { required: true, message: '请填写手机号' },
            { validator: validatePhone, message: '请输入正确的 11 位手机号' },
          ]"
        />
        <van-field
          v-model="form.password"
          name="password"
          label="密码"
          type="password"
          placeholder="请输入密码"
          :rules="[{ required: true, message: '请填写密码' }]"
        />
        <van-field
          v-model="form.nickname"
          name="nickname"
          label="昵称"
          placeholder="请输入昵称"
          :rules="[{ required: true, message: '请填写昵称' }]"
        />
      </van-cell-group>
      <div class="register__actions">
        <van-button
          round
          block
          type="primary"
          native-type="submit"
          :loading="loading"
        >
          注册
        </van-button>
        <div class="register__login-link">
          已有账号？<router-link to="/login">去登录</router-link>
        </div>
      </div>
    </van-form>
  </div>
</template>

<style scoped>
.register__form {
  margin-top: 16px;
}

.register__actions {
  padding: 16px;
}

.register__login-link {
  margin-top: 16px;
  text-align: center;
  color: #969799;
  font-size: 14px;
}
</style>
