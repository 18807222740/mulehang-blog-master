<template>
  <div class="min-h-screen bg-transparent py-16">
    <div class="mx-auto w-full max-w-5xl space-y-8 px-6">
      <header class="space-y-3 text-center">
        <div class="flex justify-center">
          <Badge
            variant="secondary"
            class="rounded-full bg-paper-dark text-xs text-ink"
          >
            真·官方公告(note: 此BYD非BYD，B和YD分开看)
          </Badge>
        </div>
        <h1
          class="font-serif text-3xl font-medium leading-tight text-ink md:text-4xl"
        >
          《原神》关于 5.0 版本“BYD”角色实装及数值平衡调整公告
        </h1>
        <p class="text-ink-light">
          BYD，启动！—— 5.0 版本限定角色卡池现已开启
        </p>
        <p class="text-ink-light">关于我转生成为 BYD 这件事</p>
      </header>

      <div class="grid grid-cols-1 gap-6 lg:grid-cols-3">
        <Card class="lg:col-span-1">
          <CardHeader>
            <CardTitle class="font-serif text-2xl text-ink">
              角色基本信息
            </CardTitle>
          </CardHeader>
          <CardContent class="space-y-4 text-sm text-ink-light">
            <div class="space-y-2">
              <div class="flex items-center justify-between">
                <span>角色名称</span>
                <span class="font-medium text-ink">BYD</span>
              </div>
              <div class="flex items-center justify-between">
                <span>称号</span>
                <span class="font-medium text-ink"
                  >提瓦特首席划水导师</span
                >
              </div>
              <div class="flex items-center justify-between">
                <span>星级</span>
                <span class="font-medium text-ink">☆☆☆☆☆☆</span>
              </div>
              <div class="flex items-center justify-between">
                <span>神之眼</span>
                <span class="font-medium text-ink">鸽元素</span>
              </div>
              <div class="flex items-center justify-between">
                <span>命之座</span>
                <span class="font-medium text-ink">破防座</span>
              </div>
              <div class="flex items-center justify-between">
                <span>武器类型</span>
                <span class="font-medium text-ink">法器（手机）</span>
              </div>
            </div>
            <div class="rounded-xl border border-ink/10 bg-paper-dark/40 p-3">
              “据说在蒙德德酒馆和璃月的茶馆里，都流传着关于他的传说。
              他并不追求神之眼的力量，他唯一的愿望是让全世界的鸽子都飞到他的技能里。
              每当夜晚降临，他就会化身为提瓦特的幽灵，在各个秘境门口疯狂刮痧。”
            </div>
          </CardContent>
        </Card>

        <div class="space-y-6 lg:col-span-2">
          <Card>
            <CardHeader>
              <CardTitle class="font-serif text-2xl text-ink"
                >天赋技能</CardTitle
              >
            </CardHeader>
            <CardContent class="space-y-4 text-sm text-ink-light">
              <div class="space-y-2">
                <p class="font-medium text-ink">普通攻击·【这就启动】</p>
                <p>
                  进行至多五段的键盘敲击，造成微弱的物理伤害。由于长期处于失重状态，
                  最后一段攻击有概率导致自己手机没电。
                </p>
              </div>
              <div class="space-y-2">
                <p class="font-medium text-ink">
                  元素战技（E）·【瞬身闪避（指回消息）】
                </p>
                <p>
                  BYD 进入“隐身”状态，持续时间随机。在此期间，任何来自现实世界的组队邀请、
                  工作消息、社交催促都无法命中。“不好意思，刚才没看到。”
                </p>
              </div>
              <div class="space-y-2">
                <p class="font-medium text-ink">元素爆发（Q）·【大祥之兆·全保底】</p>
                <p>
                  BYD 释放积攒已久的非气，使全场队友的暴击率降低 100%，
                  并在接下来的 10 连抽中强行锁定蓝天白云。“只要我够非，就没人能伤害我。”
                </p>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle class="font-serif text-2xl text-ink"
                >命之座</CardTitle
              >
            </CardHeader>
            <CardContent class="space-y-3 text-sm text-ink-light">
              <p><span class="font-medium text-ink">第 1 层【起床困难症】</span>：体力回复速度降低 50%，但睡眠质量提升 200%。</p>
              <p><span class="font-medium text-ink">第 2 层【资深垫子选手】</span>：每当看到别人出金，BYD 的红眼病抗性降低 100%。</p>
              <p><span class="font-medium text-ink">第 4 层【这就是我的逃跑路线】</span>：当血量低于 50% 时，会自动退出游戏并打开 B 站。</p>
              <p><span class="font-medium text-ink">第 6 层【真·BYD 形态】</span>：BYD 完全解放本性，通过谐音梗对敌方心理造成不可逆的真实伤害。</p>
            </CardContent>
          </Card>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle class="font-serif text-2xl text-ink">
            加载界面小贴士
          </CardTitle>
          <CardDescription class="text-ink-light">冷知识流</CardDescription>
        </CardHeader>
        <CardContent class="grid gap-3 text-sm text-ink-light md:grid-cols-2">
          <div class="rounded-xl border border-ink/10 bg-paper-dark/40 p-3">
            提示：由于 BYD 太过强大，他在进入多人游戏时，有 50% 的概率导致全队网络延迟
           （Ping 值）升至 999。
          </div>
          <div class="rounded-xl border border-ink/10 bg-paper-dark/40 p-3">
            提示：BYD 最喜欢的食物是“画的大饼”，因为这种食物不消耗任何食材。
          </div>
          <div class="rounded-xl border border-ink/10 bg-paper-dark/40 p-3">
            提示：在提瓦特大陆，BYD 是唯一一个不需要消耗体力就能原地“躺平”的角色。
          </div>
          <div class="rounded-xl border border-ink/10 bg-paper-dark/40 p-3">
            提示：当 BYD 对你发“在吗”的时候，他可能并不是真的在找你，只是想炫耀他刚抽到的单抽紫色武器。
          </div>
        </CardContent>
      </Card>

      <Card class="border-dashed border-ink/20 bg-paper-card">
        <CardHeader>
          <CardTitle class="font-serif text-2xl text-ink">
            官方补偿公告
          </CardTitle>
          <CardDescription class="text-ink-light">
            提瓦特大陆最后一位受害者：BYD
          </CardDescription>
        </CardHeader>
        <CardContent class="space-y-4 text-sm text-ink-light">
          <p>
            为维护提瓦特生态平衡，现对“BYD”角色进行如下调整：提升摸鱼效率、
            降低回消息概率、强化“蓝天白云”被动触发。
          </p>
          <div class="flex flex-wrap items-center gap-3">
            <Button
              class="rounded-xl bg-ink text-white hover:bg-clay dark:bg-clay dark:text-paper-bg"
              @click="handleCompensation"
            >
              立即领取补偿：原石 ×1600
            </Button>
            <Button variant="ghost" @click="router.push('/register')">
              返回注册页
            </Button>
            <Button variant="ghost" @click="router.push('/')">
              返回首页
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from "vue-router";
import { toast } from "vue-sonner";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

const router = useRouter();

/**
 * 处理“领取补偿”按钮点击。
 */
function handleCompensation() {
  toast.error("想得美，BYD！", {
    description: "补偿已被他拿去抽卡了。",
  });
}
</script>
