<template>
  <el-container class="layout-container">
    <el-aside width="200px">
      <div class="el-aside__logo"></div>
      <el-menu active-text-color="#ffd04b" background-color="#232323" :default-active="$route.path" text-color="#fff" router>

        <el-menu-item index="/article/category">
          <el-icon><Management /></el-icon>
          <span>分类</span>
        </el-menu-item>
        <el-menu-item index="/article/get">
          <el-icon><Promotion /></el-icon>
          <span>新品文章</span>
        </el-menu-item>
        <el-menu-item index="/article/edit">
          <el-icon><Promotion /></el-icon>
          <span>博客文章</span>
        </el-menu-item>
        <el-sub-menu index="/user">
          <template #title>
            <el-icon><UserFilled /></el-icon>
            <span>个人中心</span>
          </template>
          <el-menu-item index="/user/profile">
            <el-icon><User /></el-icon>
            <span>基本资料</span>
          </el-menu-item>
          <el-menu-item index="/user/avatar">
            <el-icon><Crop /></el-icon>
            <span>更换头像</span>
          </el-menu-item>
          <el-menu-item index="/user/password">
            <el-icon><EditPen /></el-icon>
            <span>重置密码</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header>
        <!-- header外层flex，实现左右分离 -->
        <div class="header-container">
          <div class="header-left"></div>
          <!-- 右侧：头像下拉 + 横向菜单 -->
          <div class="header-right">
            <el-dropdown placement="bottom-end" @command="handleCommand">
              <span class="avatar-wrap">
                <!-- 头像 src：后端返回的是文件名，需拼接 /api/local?fileName= 完整 URL；为空时用本地默认头像兜底 -->
                <el-avatar :src="avatarUrl" :size="50" />
                <el-icon><CaretBottom /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="avatar">更换头像</el-dropdown-item>
                  <el-dropdown-item command="password">重置密码</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>

            <el-menu
                mode="horizontal"
                background-color="#545c64"
                text-color="#fff"
                active-text-color="#ffd04b"
                :default-active="activeIndex"
                @select="handleSelect"
            >
              <el-menu-item index="1">首页</el-menu-item>
              <el-menu-item index="2">用户信息</el-menu-item>
              <el-menu-item index="3">退出登录</el-menu-item>
            </el-menu>
          </div>
        </div>
      </el-header>
      <el-main><router-view/></el-main>
      <el-footer>tea</el-footer>
    </el-container>
  </el-container>
</template>
<script setup>
import {
  Management,
  Promotion,
  UserFilled,
  User,
  Crop,
  EditPen,
  CaretBottom
} from '@element-plus/icons-vue'
import avatar from '@/assets/default.png'
import { useUserStore } from '@/stores/index.js'
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
onMounted(() => {
  userStore.getUser()
})

const router = useRouter()
const handleCommand = async (key) => {
  // 跳转操作
  router.push(`/user/${key}`)
}
</script>
<style lang="scss" scoped>
.layout-container {
  height: 100vh;
  .el-aside {
    background-color: #232323;
    &__logo {
      height: 120px;
      background: url('@/assets/logo1.png') no-repeat center / 240px auto;
    }
    .el-menu {
      border-right: none;
    }
  }
  .el-header {
    background-color: #fff;
    display: flex;
    align-items: center;
    justify-content: space-between;
    .el-dropdown__box {
      display: flex;
      align-items: center;
      .el-icon {
        color: #999;
        margin-left: 10px;
      }

      &:active,
      &:focus {
        outline: none;
      }
    }
  }
  .el-footer {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    color: #666;
  }
}
</style>
