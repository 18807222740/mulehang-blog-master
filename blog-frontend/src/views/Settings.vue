<template>
  <div class="space-y-8">
    <div class="mx-auto w-full max-w-4xl space-y-8">
      <h1 class="font-serif text-4xl font-medium text-ink">账户设置</h1>

      <!-- 基本信息 -->
      <Card class="border-ink/10 bg-paper-card shadow-soft">
        <CardHeader>
          <CardTitle class="font-serif text-2xl text-ink">基本信息</CardTitle>
        </CardHeader>
        <CardContent>
          <form @submit.prevent="handleSubmit" class="space-y-6">
            <!-- 头像 -->
            <div class="flex flex-col gap-4 md:flex-row md:items-start">
              <Avatar class="h-20 w-20">
                <AvatarImage :src="form.avatar" :alt="form.nickname" />
                <AvatarFallback class="text-xl">{{
                  form.nickname?.charAt(0) || "U"
                }}</AvatarFallback>
              </Avatar>
              <div class="flex-1 space-y-3">
                <ImageUpload
                  v-model="form.avatar"
                  label="上传头像"
                  helper="支持拖拽或点击上传"
                  drop-text="点击或拖拽上传头像"
                  upload-text="选择图片"
                  remove-text="移除"
                  preview-alt="头像预览"
                />
                <div>
                  <label class="block text-sm font-medium text-ink mb-2">
                    头像 URL（可手动填写）
                  </label>
                  <input
                    v-model="form.avatar"
                    type="url"
                    placeholder="https://example.com/avatar.jpg"
                    class="w-full rounded-xl border border-ink/10 bg-paper-card px-3 py-2 text-sm text-ink placeholder:text-ink-lighter focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-clay/40"
                  />
                </div>
              </div>
            </div>

            <!-- 用户名（只读） -->
            <div>
              <label class="block text-sm font-medium text-ink mb-2">用户名</label>
              <input
                :value="userStore.userInfo?.username"
                type="text"
                disabled
                class="w-full rounded-xl border border-ink/10 bg-paper-dark px-3 py-2 text-sm text-ink-light cursor-not-allowed"
              />
              <p class="text-xs text-ink-light mt-1">用户名不可修改</p>
            </div>

            <!-- 昵称 -->
            <div>
              <label class="block text-sm font-medium text-ink mb-2">昵称 *</label>
              <input
                v-model="form.nickname"
                type="text"
                required
                maxlength="50"
                placeholder="请输入昵称"
                class="w-full rounded-xl border border-ink/10 bg-paper-card px-3 py-2 text-sm text-ink placeholder:text-ink-lighter focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-clay/40"
              />
            </div>

            <!-- 邮箱 -->
            <div>
              <label class="block text-sm font-medium text-ink mb-2">邮箱</label>
              <input
                v-model="form.email"
                type="email"
                placeholder="your@email.com"
                class="w-full rounded-xl border border-ink/10 bg-paper-card px-3 py-2 text-sm text-ink placeholder:text-ink-lighter focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-clay/40"
              />
            </div>

            <!-- 个人简介 -->
            <div>
              <label class="block text-sm font-medium text-ink mb-2">个人简介</label>
              <textarea
                v-model="form.profile"
                rows="4"
                maxlength="200"
                placeholder="介绍一下自己吧..."
                class="w-full resize-none rounded-xl border border-ink/10 bg-paper-card px-3 py-2 text-sm text-ink placeholder:text-ink-lighter focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-clay/40"
              ></textarea>
              <p class="text-xs text-ink-light mt-1">
                {{ form.profile?.length || 0 }} / 200
              </p>
            </div>

            <!-- 提交按钮 -->
            <div class="flex gap-4">
              <Button
                type="submit"
                :disabled="saving"
                class="rounded-xl bg-ink text-white hover:bg-clay"
              >
                {{ saving ? "保存中..." : "保存修改" }}
              </Button>
              <Button
                type="button"
                variant="outline"
                class="rounded-xl border-ink/20 text-ink hover:bg-paper-dark"
                @click="resetForm"
              >
                取消
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      <!-- 修改密码 -->
      <Card class="border-ink/10 bg-paper-card shadow-soft">
        <CardHeader>
          <CardTitle class="font-serif text-2xl text-ink">修改密码</CardTitle>
        </CardHeader>
        <CardContent>
          <form @submit.prevent="handleChangePassword" class="space-y-6">
            <!-- 当前密码 -->
            <div>
              <label class="block text-sm font-medium text-ink mb-2">当前密码 *</label>
              <input
                v-model="passwordForm.currentPassword"
                type="password"
                required
                placeholder="请输入当前密码"
                class="w-full rounded-xl border border-ink/10 bg-paper-card px-3 py-2 text-sm text-ink placeholder:text-ink-lighter focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-clay/40"
              />
            </div>

            <!-- 新密码 -->
            <div>
              <label class="block text-sm font-medium text-ink mb-2">新密码 *</label>
              <input
                v-model="passwordForm.newPassword"
                type="password"
                required
                minlength="6"
                placeholder="请输入新密码（至少6位）"
                class="w-full rounded-xl border border-ink/10 bg-paper-card px-3 py-2 text-sm text-ink placeholder:text-ink-lighter focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-clay/40"
              />
            </div>

            <!-- 确认新密码 -->
            <div>
              <label class="block text-sm font-medium text-ink mb-2">确认新密码 *</label>
              <input
                v-model="passwordForm.confirmPassword"
                type="password"
                required
                minlength="6"
                placeholder="请再次输入新密码"
                class="w-full rounded-xl border border-ink/10 bg-paper-card px-3 py-2 text-sm text-ink placeholder:text-ink-lighter focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-clay/40"
              />
            </div>

            <!-- 提交按钮 -->
            <Button
              type="submit"
              variant="default"
              :disabled="changingPassword"
              class="rounded-xl bg-ink text-white hover:bg-clay"
            >
              {{ changingPassword ? "修改中..." : "修改密码" }}
            </Button>
          </form>
        </CardContent>
      </Card>

      <!-- 危险操作 -->
      <Card class="border-destructive bg-paper-card shadow-soft">
        <CardHeader>
          <CardTitle class="text-destructive">危险操作</CardTitle>
        </CardHeader>
        <CardContent>
          <div class="space-y-4">
            <div
              class="flex items-center justify-between p-4 border border-destructive/50 rounded-xl"
            >
              <div>
                <h3 class="font-medium text-destructive">删除账户</h3>
                <p class="text-sm text-ink-light mt-1">
                  删除账户后，您的所有数据将被永久删除且无法恢复
                </p>
              </div>
              <Button
                variant="destructive"
                :disabled="deletingAccount"
                @click="handleDeleteAccount"
              >
                删除账户
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>

    <AlertDialog :open="deleteDialogOpen" @update:open="handleDeleteDialogOpen">
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>确认删除账户</AlertDialogTitle>
          <AlertDialogDescription>
            删除账户后将无法恢复，确定要继续吗？
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel @click="closeDeleteDialog">取消</AlertDialogCancel>
          <AlertDialogAction
            :disabled="deletingAccount"
            @click="confirmDeleteAccount"
          >
            {{ deletingAccount ? "删除中..." : "确认删除" }}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { toast } from "vue-sonner";
import { useUserStore } from "@/stores/user";
import { userApi } from "@/api/user";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Avatar, AvatarImage, AvatarFallback } from "@/components/ui/avatar";
import ImageUpload from "@/components/Upload/ImageUpload.vue";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";

const router = useRouter();
const userStore = useUserStore();

const saving = ref(false);
const changingPassword = ref(false);
const deletingAccount = ref(false);
const deleteDialogOpen = ref(false);

// 基本信息表单
const form = ref({
  nickname: "",
  email: "",
  avatar: "",
  profile: "",
});

// 密码修改表单
const passwordForm = ref({
  currentPassword: "",
  newPassword: "",
  confirmPassword: "",
});

/**
 * 初始化表单
 */
function initForm() {
  if (userStore.userInfo) {
    form.value = {
      nickname: userStore.userInfo.nickname || "",
      email: userStore.userInfo.email || "",
      avatar: userStore.userInfo.avatar || "",
      profile: userStore.userInfo.profile || "",
    };
  }
}

/**
 * 重置表单
 */
function resetForm() {
  initForm();
}

/**
 * 提交基本信息修改
 */
async function handleSubmit() {
  if (!form.value.nickname.trim()) {
    toast.error("昵称不能为空");
    return;
  }

  saving.value = true;
  try {
    const payload = {
      nickname: form.value.nickname.trim(),
      email: form.value.email.trim() || undefined,
      avatar: form.value.avatar.trim() || undefined,
      profile: form.value.profile.trim() || undefined,
    };
    const updated = await userApi.updateProfile(payload);
    userStore.setUserInfo(updated);

    toast.success("保存成功");
  } catch (err: any) {
    console.error("保存失败:", err);
    toast.error("保存失败", {
      description: err.message || "请稍后重试",
    });
  } finally {
    saving.value = false;
  }
}

/**
 * 修改密码
 */
async function handleChangePassword() {
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    toast.error("两次输入的新密码不一致");
    return;
  }

  if (passwordForm.value.newPassword.length < 6) {
    toast.error("新密码至少需 6 位");
    return;
  }

  changingPassword.value = true;
  try {
    await userApi.changePassword({
      currentPassword: passwordForm.value.currentPassword,
      newPassword: passwordForm.value.newPassword,
    });

    toast.success("密码修改成功", {
      description: "请重新登录",
    });

    // 清空表单
    passwordForm.value = {
      currentPassword: "",
      newPassword: "",
      confirmPassword: "",
    };

    // 退出登录
    await userStore.logout();
    router.replace("/login");
  } catch (err: any) {
    console.error("修改密码失败:", err);
    toast.error("修改密码失败", {
      description: err.message || "请稍后重试",
    });
  } finally {
    changingPassword.value = false;
  }
}

/**
 * 删除账户
 */
function handleDeleteAccount() {
  deleteDialogOpen.value = true;
}

/**
 * 处理删除账号对话框开关
 */
function handleDeleteDialogOpen(open: boolean) {
  if (!open) {
    closeDeleteDialog();
    return;
  }
  deleteDialogOpen.value = true;
}

/**
 * 关闭删除账号对话框
 */
function closeDeleteDialog() {
  deleteDialogOpen.value = false;
}

/**
 * 确认删除账号
 */
async function confirmDeleteAccount() {
  if (deletingAccount.value) return;
  deletingAccount.value = true;
  try {
    await userApi.deleteCurrent();
    toast.success("账号已删除", { description: "即将退出登录" });
    await userStore.logout();
    router.replace("/login");
  } catch (err: any) {
    console.error("删除账号失败:", err);
    toast.error("删除账号失败", {
      description: err.message || "请稍后重试",
    });
  } finally {
    deletingAccount.value = false;
    closeDeleteDialog();
  }
}

onMounted(() => {
  // 如果未登录，跳转到登录页
  if (!userStore.isLoggedIn) {
    router.push("/login");
    return;
  }

  userApi
    .getCurrent()
    .then((user) => {
      userStore.setUserInfo(user);
      initForm();
    })
    .catch((err: any) => {
      console.error("获取用户信息失败:", err);
      initForm();
    });
});
</script>
