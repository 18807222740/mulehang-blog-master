<template>
  <div class="space-y-10">
    <header class="space-y-3">
      <h1 class="font-serif text-4xl font-medium text-ink">管理控制台</h1>
      <p class="text-ink-light">用于索引、邮件与 WebSocket 测试的管理入口。</p>
    </header>

    <div class="grid grid-cols-1 gap-6 lg:grid-cols-2">
      <Card class="border-ink/10 bg-paper-card shadow-soft">
        <CardHeader>
          <CardTitle class="font-serif text-2xl text-ink">搜索索引</CardTitle>
        </CardHeader>
        <CardContent class="space-y-4">
          <p class="text-sm text-ink-light">
            重建 Elasticsearch 索引（全量同步已发布文章）。
          </p>
          <Button
            class="rounded-xl bg-ink text-white hover:bg-clay"
            :disabled="rebuildLoading"
            @click="handleRebuildIndex"
          >
            {{ rebuildLoading ? "处理中..." : "重建索引" }}
          </Button>
          <p v-if="rebuildCount !== null" class="text-sm text-ink-light">
            已同步 {{ rebuildCount }} 篇文章。
          </p>
        </CardContent>
      </Card>

      <Card class="border-ink/10 bg-paper-card shadow-soft">
        <CardHeader>
          <CardTitle class="font-serif text-2xl text-ink">延迟邮件测试</CardTitle>
        </CardHeader>
        <CardContent class="space-y-4">
          <Input v-model="emailForm.to" placeholder="收件人邮箱" />
          <Input v-model="emailForm.subject" placeholder="邮件主题" />
          <Textarea v-model="emailForm.content" placeholder="邮件内容" class="min-h-24" />
          <Input
            v-model.number="emailForm.delaySeconds"
            type="number"
            min="1"
            placeholder="延迟秒数"
          />
          <Button
            class="rounded-xl bg-ink text-white hover:bg-clay"
            :disabled="emailLoading"
            @click="handleSendTestEmail"
          >
            {{ emailLoading ? "发送中..." : "发送测试邮件" }}
          </Button>
        </CardContent>
      </Card>

      <Card class="border-ink/10 bg-paper-card shadow-soft lg:col-span-2">
        <CardHeader>
          <CardTitle class="font-serif text-2xl text-ink">用户列表</CardTitle>
          <CardAction>
            <Button
              variant="outline"
              class="rounded-xl border-ink/20 text-ink"
              :disabled="usersLoading"
              @click="fetchUsers"
            >
              {{ usersLoading ? "刷新中..." : "刷新" }}
            </Button>
          </CardAction>
        </CardHeader>
        <CardContent class="space-y-4">
          <p class="text-sm text-ink-light">共 {{ users.length }} 位用户</p>

          <p v-if="usersLoading" class="text-sm text-ink-light">加载中...</p>
          <p v-else-if="users.length === 0" class="text-sm text-ink-light">
            暂无用户
          </p>

          <div v-else class="space-y-3">
            <div
              v-for="user in users"
              :key="user.id"
              class="flex flex-col gap-4 rounded-2xl border border-ink/10 bg-paper-dark/30 p-4 md:flex-row md:items-center md:justify-between"
            >
              <div class="flex items-center gap-4">
                <Avatar class="h-12 w-12">
                  <AvatarImage
                    :src="user.avatar || ''"
                    :alt="user.nickname || user.username || ''"
                  />
                  <AvatarFallback>{{
                    (user.nickname || user.username || "?").charAt(0)
                  }}</AvatarFallback>
                </Avatar>
                <div class="space-y-1">
                  <p class="text-base font-semibold text-ink">
                    {{ user.nickname || user.username }}
                  </p>
                  <p class="text-xs text-ink-light">
                    @{{ user.username }} · ID {{ user.id }}
                  </p>
                  <p class="text-xs text-ink-light">
                    {{ user.email || "—" }}
                  </p>
                  <p
                    v-if="user.profile"
                    class="text-xs text-ink-light line-clamp-1"
                  >
                    {{ user.profile }}
                  </p>
                </div>
              </div>

              <div class="flex flex-wrap items-center gap-2">
                <Badge
                  v-for="role in user.roles || []"
                  :key="role"
                  variant="secondary"
                  class="rounded-full bg-paper-dark text-ink"
                >
                  {{ role }}
                </Badge>
                <span
                  v-if="!user.roles?.length"
                  class="text-xs text-ink-light"
                >
                  暂无角色
                </span>
              </div>

              <div class="text-xs text-ink-light md:text-right">
                <p>最近登录</p>
                <p class="text-sm text-ink">
                  {{ user.lastLoginTime || "—" }}
                </p>
              </div>
            </div>
          </div>

          <p v-if="usersError" class="text-sm text-destructive">
            {{ usersError }}
          </p>
        </CardContent>
      </Card>
    </div>

    <Card class="border-ink/10 bg-paper-card shadow-soft">
      <CardHeader>
        <CardTitle class="font-serif text-2xl text-ink">WebSocket 测试</CardTitle>
      </CardHeader>
      <CardContent class="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <div class="space-y-4">
          <h3 class="text-sm font-semibold text-ink">发送通知给指定用户</h3>
          <Input v-model.number="wsSendForm.userId" type="number" placeholder="用户 ID" />
          <Textarea
            v-model="wsSendForm.message"
            placeholder="通知内容"
            class="min-h-24"
          />
          <Button
            class="rounded-xl bg-ink text-white hover:bg-clay"
            :disabled="wsSendLoading"
            @click="handleSendNotification"
          >
            {{ wsSendLoading ? "发送中..." : "发送通知" }}
          </Button>
        </div>

        <div class="space-y-4">
          <h3 class="text-sm font-semibold text-ink">广播通知</h3>
          <Textarea
            v-model="wsBroadcastMessage"
            placeholder="广播内容"
            class="min-h-24"
          />
          <Button
            class="rounded-xl bg-ink text-white hover:bg-clay"
            :disabled="wsBroadcastLoading"
            @click="handleBroadcastNotification"
          >
            {{ wsBroadcastLoading ? "发送中..." : "广播通知" }}
          </Button>
        </div>

        <div class="space-y-4">
          <h3 class="text-sm font-semibold text-ink">在线用户统计</h3>
          <div class="flex items-center gap-3">
            <span class="text-3xl font-semibold text-clay">{{
              onlineCount ?? "--"
            }}</span>
            <Button
              variant="outline"
              class="rounded-xl border-ink/20 text-ink"
              :disabled="onlineLoading"
              @click="refreshOnlineCount"
            >
              刷新
            </Button>
          </div>
        </div>

        <div class="space-y-4">
          <h3 class="text-sm font-semibold text-ink">检查用户在线状态</h3>
          <Input
            v-model.number="wsCheckUserId"
            type="number"
            placeholder="用户 ID"
          />
          <Button
            variant="outline"
            class="rounded-xl border-ink/20 text-ink"
            :disabled="wsCheckLoading"
            @click="handleCheckUserOnline"
          >
            {{ wsCheckLoading ? "查询中..." : "查询状态" }}
          </Button>
          <p v-if="onlineStatus !== null" class="text-sm text-ink-light">
            {{ onlineStatus ? "用户在线" : "用户离线" }}
          </p>
        </div>
      </CardContent>
    </Card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { toast } from "vue-sonner";
import { articleApi } from "@/api/article";
import { userApi } from "@/api/user";
import { wsApi } from "@/api/ws";
import type { UserInfo } from "@/types/api";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardAction,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";

const rebuildLoading = ref(false);
const rebuildCount = ref<number | null>(null);

const emailLoading = ref(false);
const emailForm = ref({
  to: "",
  subject: "Test",
  content: "Hello",
  delaySeconds: 5,
});

const wsSendLoading = ref(false);
const wsSendForm = ref({
  userId: undefined as number | undefined,
  message: "",
});

const wsBroadcastLoading = ref(false);
const wsBroadcastMessage = ref("");

const onlineLoading = ref(false);
const onlineCount = ref<number | null>(null);

const wsCheckLoading = ref(false);
const wsCheckUserId = ref<number | undefined>();
const onlineStatus = ref<boolean | null>(null);

const usersLoading = ref(false);
const users = ref<UserInfo[]>([]);
const usersError = ref("");

/**
 * 重建文章搜索索引。
 */
async function handleRebuildIndex() {
  rebuildLoading.value = true;
  try {
    const count = await articleApi.rebuildSearchIndex();
    rebuildCount.value = count;
    toast.success("索引重建完成", {
      description: `成功同步 ${count} 篇文章`,
    });
  } catch (error: any) {
    toast.error("索引重建失败", {
      description: error?.message || "请稍后重试",
    });
  } finally {
    rebuildLoading.value = false;
  }
}

/**
 * 发送测试邮件。
 */
async function handleSendTestEmail() {
  if (!emailForm.value.to.trim()) {
    toast.error("请输入收件人邮箱");
    return;
  }
  emailLoading.value = true;
  try {
    await articleApi.sendTestEmail({
      to: emailForm.value.to.trim(),
      subject: emailForm.value.subject.trim() || "Test",
      content: emailForm.value.content.trim() || "Hello",
      delaySeconds: emailForm.value.delaySeconds || 5,
    });
    toast.success("测试邮件已投递");
  } catch (error: any) {
    toast.error("邮件发送失败", {
      description: error?.message || "请稍后重试",
    });
  } finally {
    emailLoading.value = false;
  }
}

/**
 * 发送 WebSocket 通知给指定用户。
 */
async function handleSendNotification() {
  if (wsSendForm.value.userId == null || !wsSendForm.value.message.trim()) {
    toast.error("请输入用户 ID 和通知内容");
    return;
  }
  wsSendLoading.value = true;
  try {
    await wsApi.sendToUser({
      userId: wsSendForm.value.userId,
      message: wsSendForm.value.message.trim(),
    });
    toast.success("通知已发送");
  } catch (error: any) {
    toast.error("发送失败", {
      description: error?.message || "请稍后重试",
    });
  } finally {
    wsSendLoading.value = false;
  }
}

/**
 * 广播 WebSocket 通知。
 */
async function handleBroadcastNotification() {
  if (!wsBroadcastMessage.value.trim()) {
    toast.error("请输入广播内容");
    return;
  }
  wsBroadcastLoading.value = true;
  try {
    await wsApi.broadcast(wsBroadcastMessage.value.trim());
    toast.success("广播已发送");
  } catch (error: any) {
    toast.error("广播失败", {
      description: error?.message || "请稍后重试",
    });
  } finally {
    wsBroadcastLoading.value = false;
  }
}

/**
 * 刷新在线用户数量。
 */
async function refreshOnlineCount() {
  onlineLoading.value = true;
  try {
    onlineCount.value = await wsApi.getOnlineCount();
  } catch (error: any) {
    toast.error("获取在线人数失败", {
      description: error?.message || "请稍后重试",
    });
  } finally {
    onlineLoading.value = false;
  }
}

/**
 * 检查指定用户是否在线。
 */
async function handleCheckUserOnline() {
  if (!wsCheckUserId.value) {
    toast.error("请输入用户 ID");
    return;
  }
  wsCheckLoading.value = true;
  try {
    onlineStatus.value = await wsApi.isOnline(wsCheckUserId.value);
  } catch (error: any) {
    toast.error("查询失败", {
      description: error?.message || "请稍后重试",
    });
  } finally {
    wsCheckLoading.value = false;
  }
}

/**
 * 获取用户列表。
 */
async function fetchUsers() {
  usersLoading.value = true;
  usersError.value = "";
  try {
    users.value = await userApi.listAll();
  } catch (error: any) {
    usersError.value = error?.message || "获取用户列表失败";
  } finally {
    usersLoading.value = false;
  }
}

onMounted(() => {
  refreshOnlineCount();
  fetchUsers();
});
</script>
